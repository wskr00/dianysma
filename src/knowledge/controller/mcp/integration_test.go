//go:build integration

package mcpcontroller_test

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"net/url"
	"sort"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"
	"unicode"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/google/uuid"
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	pgcontainer "github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/wskr00/dianysma/src/core/config"
	"github.com/wskr00/dianysma/src/core/database"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	mcpcontroller "github.com/wskr00/dianysma/src/knowledge/controller/mcp"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
	"google.golang.org/genai"
	"gorm.io/gorm"
)

type lockedBuffer struct {
	sync.Mutex
	bytes.Buffer
}

func (b *lockedBuffer) Write(p []byte) (int, error) {
	b.Lock()
	defer b.Unlock()
	return b.Buffer.Write(p)
}
func (b *lockedBuffer) text() string { b.Lock(); defer b.Unlock(); return b.Buffer.String() }

// The synthetic provider implements Gemini's actual SDK wire format over TLS.
// All MCP, ORM, migration and pgvector code remains the production implementation.
type embeddingProvider struct {
	t     *testing.T
	calls atomic.Int64
	mode  atomic.Int64
}

func (p *embeddingProvider) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	p.calls.Add(1)
	w.Header().Set("Content-Type", "application/json")
	if r.Method != http.MethodPost || !strings.HasSuffix(r.URL.Path, ":batchEmbedContents") || r.Header.Get("x-goog-api-key") != "local-test-key" {
		http.Error(w, "unexpected SDK request", http.StatusBadRequest)
		return
	}
	if p.mode.Load() == 1 {
		w.WriteHeader(400)
		if _, err := io.WriteString(w, `{"error":{"code":400,"message":"synthetic provider failure","status":"INVALID_ARGUMENT"}}`); err != nil {
			p.t.Errorf("write provider error fixture: %v", err)
		}
		return
	}
	if p.mode.Load() == 3 {
		select {
		case <-r.Context().Done():
		case <-time.After(time.Second):
		}
		return
	}
	var body struct {
		Requests []struct {
			Content struct {
				Parts []struct {
					Text string `json:"text"`
				} `json:"parts"`
			} `json:"content"`
			Dimensions int `json:"outputDimensionality"`
		} `json:"requests"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		http.Error(w, err.Error(), 400)
		return
	}
	out := make([]map[string]any, 0, len(body.Requests))
	for _, request := range body.Requests {
		if request.Dimensions != 1536 {
			http.Error(w, "unexpected dimensionality", 400)
			return
		}
		values := make([]float32, request.Dimensions)
		if p.mode.Load() == 2 {
			values = values[:1535]
		}
		for _, part := range request.Content.Parts {
			for _, word := range strings.FieldsFunc(strings.ToLower(part.Text), func(r rune) bool { return !unicode.IsLetter(r) && !unicode.IsNumber(r) }) {
				hash := sha256.Sum256([]byte(word))
				values[int(binary.BigEndian.Uint32(hash[:4]))%len(values)]++
			}
		}
		out = append(out, map[string]any{"values": values})
	}
	if err := json.NewEncoder(w).Encode(map[string]any{"embeddings": out}); err != nil {
		p.t.Errorf("encode provider fixture response: %v", err)
	}
}

type fixture struct {
	t        *testing.T
	db       *gorm.DB
	repo     *repository.Store
	services *service.Services
	provider *embeddingProvider
	client   *mcp.ClientSession
	schemas  map[string]*jsonschema.Resolved
	called   map[string]bool
	logs     *lockedBuffer
	url      string
}

func newFixture(t *testing.T, db *gorm.DB) *fixture {
	t.Helper()
	require.NoError(t, db.Exec(`TRUNCATE knowledge_chunk, vector_store, claim_skill, claim, language, participation, education, experience, project, skill, profile, organization CASCADE`).Error)
	provider := &embeddingProvider{t: t}
	api := httptest.NewTLSServer(provider)
	t.Cleanup(api.Close)
	client, err := genai.NewClient(t.Context(), &genai.ClientConfig{Backend: genai.BackendGeminiAPI, APIKey: "local-test-key", HTTPClient: api.Client(), HTTPOptions: genai.HTTPOptions{BaseURL: api.URL}})
	require.NoError(t, err)
	repo := repository.New(db)
	vectors := vector.NewStore(vector.NewGoogleEmbedderWithClient(client, "gemini-embedding-2", 1536), "gemini-embedding-2", 1536)
	services, err := service.New(repo, vector.NewIndexer(vectors), vectors)
	require.NoError(t, err)
	logs := &lockedBuffer{}
	logger := slog.New(slog.NewJSONHandler(logs, nil))
	server := mcpcontroller.NewServer(services, logger, "test", 5*time.Second)
	mux := http.NewServeMux()
	mux.Handle("/mcp", http.NewCrossOriginProtection().Handler(mcpcontroller.NewHandler(server, logger)))
	httpServer := httptest.NewServer(mux)
	t.Cleanup(httpServer.Close)
	session, err := mcp.NewClient(&mcp.Implementation{Name: "dianysma-integration", Version: "1"}, nil).Connect(t.Context(), &mcp.StreamableClientTransport{Endpoint: httpServer.URL + "/mcp"}, nil)
	require.NoError(t, err)
	t.Cleanup(func() {
		if err := session.Close(); err != nil {
			t.Errorf("close MCP session: %v", err)
		}
	})
	list, err := session.ListTools(t.Context(), nil)
	require.NoError(t, err)
	require.Len(t, list.Tools, 41)
	schemas := make(map[string]*jsonschema.Resolved)
	for _, tool := range list.Tools {
		require.NotEmpty(t, tool.Description, tool.Name)
		require.NotNil(t, tool.Annotations, tool.Name)
		data, err := json.Marshal(tool.OutputSchema)
		require.NoError(t, err)
		var schema jsonschema.Schema
		require.NoError(t, json.Unmarshal(data, &schema))
		resolved, err := schema.Resolve(nil)
		require.NoError(t, err)
		schemas[tool.Name] = resolved
	}
	return &fixture{t: t, db: db, repo: repo, services: services, provider: provider, client: session, schemas: schemas, called: make(map[string]bool), logs: logs, url: httpServer.URL}
}

func (f *fixture) call(name string, args map[string]any, success bool) map[string]any {
	f.t.Helper()
	f.called[name] = true
	response, err := f.client.CallTool(f.t.Context(), &mcp.CallToolParams{Name: name, Arguments: args})
	require.NoError(f.t, err, name)
	require.Equal(f.t, !success, response.IsError, "%s: %+v", name, response.Content)
	if !success {
		return nil
	}
	data, err := json.Marshal(response.StructuredContent)
	require.NoError(f.t, err)
	var output map[string]any
	require.NoError(f.t, json.Unmarshal(data, &output))
	require.NoError(f.t, f.schemas[name].Validate(output), name)
	return output
}

func (f *fixture) count(table string) int64 {
	f.t.Helper()
	var count int64
	require.NoError(f.t, f.db.Table(table).Count(&count).Error)
	return count
}

func TestIntegration(t *testing.T) {
	container, err := pgcontainer.Run(t.Context(), "pgvector/pgvector:0.8.7-pg18-trixie", pgcontainer.WithDatabase("dianysma_go_test"), pgcontainer.WithUsername("dianysma_test"), pgcontainer.WithPassword("local-test-only"), pgcontainer.BasicWaitStrategies())
	testcontainers.CleanupContainer(t, container)
	require.NoError(t, err)
	dsn, err := container.ConnectionString(t.Context(), "sslmode=disable")
	require.NoError(t, err)
	databaseURL, err := url.Parse(dsn)
	require.NoError(t, err)
	port, err := strconv.Atoi(databaseURL.Port())
	require.NoError(t, err)
	require.NotNil(t, databaseURL.User)
	password, hasPassword := databaseURL.User.Password()
	require.True(t, hasPassword)
	databaseConfig := config.Config{
		DatabaseHost:     databaseURL.Hostname(),
		DatabasePort:     port,
		DatabaseName:     strings.TrimPrefix(databaseURL.Path, "/"),
		DatabaseUser:     databaseURL.User.Username(),
		DatabasePassword: password,
		DatabaseSSLMode:  "disable",
		DatabasePoolMax:  4,
	}
	db, err := database.Open(t.Context(), databaseConfig)
	require.NoError(t, err)
	pool, err := db.DB()
	require.NoError(t, err)
	t.Cleanup(func() {
		if err := pool.Close(); err != nil {
			t.Errorf("close PostgreSQL pool: %v", err)
		}
	})
	require.NoError(t, database.Migrate(t.Context(), databaseConfig))
	profileID, vectorID := uuid.New(), uuid.New()
	require.NoError(t, db.Exec(`INSERT INTO profile (id, created_at, updated_at, name) VALUES (?, now(), now(), ?)`, profileID, "Preservado").Error)
	require.NoError(t, db.Exec(`INSERT INTO vector_store (id, content) VALUES (?, ?)`, vectorID, "vetor preservado").Error)
	require.NoError(t, db.Exec(`DROP TABLE goose_db_version`).Error)
	require.NoError(t, database.Migrate(t.Context(), databaseConfig), "executar novamente o SQL-base deve preservar objetos e dados existentes")
	var preservedProfile, preservedVector int64
	require.NoError(t, db.Table("profile").Where("id = ? AND name = ?", profileID, "Preservado").Count(&preservedProfile).Error)
	require.NoError(t, db.Table("vector_store").Where("id = ? AND content = ?", vectorID, "vetor preservado").Count(&preservedVector).Error)
	require.EqualValues(t, 1, preservedProfile)
	require.EqualValues(t, 1, preservedVector)
	require.NoError(t, database.CheckDimensions(t.Context(), db, 1536))
	require.Error(t, database.CheckDimensions(t.Context(), db, 768))

	t.Run("postgres_constraints_protect_knowledge_invariants", func(t *testing.T) {
		require.NoError(t, db.Exec(`TRUNCATE profile CASCADE`).Error)
		require.NoError(t, db.Exec(`INSERT INTO profile (id, created_at, updated_at, name) VALUES (?, now(), now(), ?)`, uuid.New(), "Único").Error)
		require.Error(t, db.Exec(`INSERT INTO profile (id, created_at, updated_at, name) VALUES (?, now(), now(), ?)`, uuid.New(), "Duplicado").Error)

		require.NoError(t, db.Exec(`INSERT INTO skill (id, created_at, updated_at, name) VALUES (?, now(), now(), ?)`, uuid.New(), "Go").Error)
		require.Error(t, db.Exec(`INSERT INTO skill (id, created_at, updated_at, name) VALUES (?, now(), now(), ?)`, uuid.New(), "go").Error)

		require.Error(t, db.Exec(`INSERT INTO participation (id, created_at, updated_at) VALUES (?, now(), now())`, uuid.New()).Error)

		claimID, orphanChunkID := uuid.New(), uuid.New()
		require.NoError(t, db.Exec(`INSERT INTO claim (id, created_at, updated_at, content) VALUES (?, now(), now(), ?)`, claimID, "Fato válido").Error)
		require.NoError(t, db.Exec(`INSERT INTO vector_store (id) VALUES (?)`, uuid.New()).Error)
		require.Error(t, db.Exec(`INSERT INTO knowledge_chunk (id, claim_id, indexed_at, model, hash) VALUES (?, ?, now(), ?, ?)`, orphanChunkID, claimID, "test-model", "test-hash").Error)
	})

	t.Run("all_tools_and_professional_knowledge_lifecycle", func(t *testing.T) {
		f := newFixture(t, db)
		f.call("get_profile", nil, false)
		f.call("create_language", map[string]any{"name": "Português", "proficiency": "NATIVE"}, false)
		profile := f.call("create_profile", map[string]any{"name": "Pessoa de teste"}, true)
		require.Equal(t, uuid.Version(7), uuid.MustParse(profile["id"].(string)).Version())
		for _, key := range []string{"email", "phone", "city", "state", "country", "linkedinUrl", "githubUrl", "portfolioUrl"} {
			require.Nil(t, profile[key], key)
		}
		f.call("create_profile", map[string]any{"name": "Duplicado"}, false)
		updatedProfile := f.call("update_profile", map[string]any{"city": "Belém"}, true)
		require.Nil(t, updatedProfile["name"])
		require.Equal(t, "Belém", f.call("get_profile", nil, true)["city"])
		f.call("update_profile", map[string]any{"email": ""}, true)
		language := f.call("create_language", map[string]any{"name": "Português", "proficiency": "NATIVE"}, true)
		f.call("get_language", map[string]any{"languageId": language["id"]}, true)
		f.call("list_languages", nil, true)
		f.call("update_language", map[string]any{"languageId": language["id"], "name": "Inglês", "proficiency": "ADVANCED"}, true)
		org := f.call("create_organization", map[string]any{"name": "Alfa Serviços"}, true)
		f.call("get_organization", map[string]any{"organizationId": org["id"]}, true)
		orgs := f.call("search_organizations", map[string]any{"query": "ALFA"}, true)
		require.Len(t, orgs["organizations"], 1)
		experience := f.call("create_experience", map[string]any{"employerId": org["id"], "role": "Engenheiro de software", "employmentType": "CLT", "startedAt": "2024-01-01"}, true)
		f.call("get_experience", map[string]any{"experienceId": experience["id"]}, true)
		f.call("list_experiences", nil, true)
		project := f.call("create_project", map[string]any{"name": "Portal IA", "organizationId": org["id"]}, true)
		f.call("get_project", map[string]any{"projectId": project["id"]}, true)
		f.call("list_projects", nil, true)
		participation := f.call("create_participation", map[string]any{"experienceId": experience["id"], "projectId": project["id"], "description": "Implementei autenticação e autorização com revisão de acessos."}, true)
		f.call("get_participation", map[string]any{"participationId": participation["id"]}, true)
		f.call("list_participations", nil, true)
		f.call("create_participation", map[string]any{"experienceId": experience["id"], "projectId": project["id"]}, false)
		skill := f.call("create_skill", map[string]any{"name": " Go ", "aliases": []string{"Golang", " golang ", "GO"}}, true)
		reused := f.call("create_skill", map[string]any{"name": "go", "aliases": []string{"Ignorar novo alias"}}, true)
		require.Equal(t, skill, reused)
		f.call("search_skills", map[string]any{"query": "golang"}, true)
		claim := f.call("create_claim", map[string]any{"content": "Automatizei faturamento e reconciliação financeira, reduzindo em 35% o processamento.", "participationId": participation["id"], "skillIds": []any{skill["id"], skill["id"]}}, true)
		require.Len(t, claim["skills"], 1)
		f.call("get_claim", map[string]any{"claimId": claim["id"]}, true)
		f.call("list_claims", map[string]any{"participationId": participation["id"], "limit": 1}, true)
		before := f.provider.calls.Load()
		f.call("update_claim", map[string]any{"claimId": claim["id"], "content": claim["content"]}, true)
		require.Equal(t, before, f.provider.calls.Load(), "unchanged index must reuse embedding")
		loaded := f.call("get_claim", map[string]any{"claimId": claim["id"]}, true)
		require.Equal(t, participation["id"], loaded["participationId"])
		require.Len(t, loaded["skills"], 1)
		education := f.call("create_education", map[string]any{"institution": "Instituto de teste", "type": "COURSE", "title": "Segurança de aplicações", "degree": "Extensão", "workloadHours": 0, "startedAt": "2024-01-01", "completedAt": "2024-02-01", "expected": false}, true)
		f.call("get_education", map[string]any{"educationId": education["id"]}, true)
		f.call("list_education", nil, true)
		updatedEducation := f.call("update_education", map[string]any{"educationId": education["id"], "institution": "Instituto de teste", "type": "COURSE", "title": "Segurança de aplicações"}, true)
		require.Nil(t, updatedEducation["completedAt"])
		require.Nil(t, updatedEducation["expected"])
		require.Nil(t, updatedEducation["workloadHours"])
		search := f.call("search_knowledge", map[string]any{"query": "faturamento reconciliação financeira", "limit": 3}, true)
		hits := search["results"].([]any)
		require.Len(t, hits, 3)
		require.Equal(t, claim["id"], hits[0].(map[string]any)["sourceId"])
		require.Equal(t, "claim", hits[0].(map[string]any)["sourceType"])
		for tool, idField := range map[string]string{"delete_organization": "organizationId", "delete_experience": "experienceId", "delete_project": "projectId", "delete_participation": "participationId"} {
			ids := map[string]any{"organizationId": org["id"], "experienceId": experience["id"], "projectId": project["id"], "participationId": participation["id"]}
			f.call(tool, map[string]any{idField: ids[idField]}, false)
		}
		f.call("update_organization", map[string]any{"organizationId": org["id"], "name": "Alfa Tecnologia"}, true)
		f.call("update_experience", map[string]any{"experienceId": experience["id"], "employerId": org["id"], "role": "Engenheiro Go", "employmentType": "PJ"}, true)
		f.call("update_project", map[string]any{"projectId": project["id"], "name": "Portal de dados"}, true)
		var indexed domain.VectorDocument
		require.NoError(t, db.Where("id = ?", participation["id"]).First(&indexed).Error)
		require.Contains(t, indexed.Content, "Engenheiro Go at Alfa Tecnologia")
		require.Contains(t, indexed.Content, "Portal de dados")
		f.call("update_claim", map[string]any{"claimId": claim["id"], "content": claim["content"], "clearParticipation": true, "skillIds": []any{}}, true)
		require.Zero(t, f.count("claim_skill"))
		f.call("update_participation", map[string]any{"participationId": participation["id"], "experienceId": experience["id"], "projectId": project["id"]}, true)
		require.EqualValues(t, 2, f.count("vector_store"), "clearing participation description must remove its vector")
		f.call("delete_claim", map[string]any{"claimId": claim["id"]}, true)
		again := f.call("delete_claim", map[string]any{"claimId": claim["id"]}, true)
		require.Equal(t, false, again["deleted"])
		f.call("delete_participation", map[string]any{"participationId": participation["id"]}, true)
		f.call("delete_experience", map[string]any{"experienceId": experience["id"]}, true)
		f.call("delete_project", map[string]any{"projectId": project["id"]}, true)
		f.call("delete_organization", map[string]any{"organizationId": org["id"]}, true)
		f.call("delete_education", map[string]any{"educationId": education["id"]}, true)
		f.call("delete_language", map[string]any{"languageId": language["id"]}, true)
		require.Zero(t, f.count("vector_store"))
		require.Zero(t, f.count("knowledge_chunk"))
		require.Equal(t, []any{}, f.call("list_claims", nil, true)["claims"])
		missing := []string{}
		for tool := range f.schemas {
			if !f.called[tool] {
				missing = append(missing, tool)
			}
		}
		sort.Strings(missing)
		require.Empty(t, missing, "every published tool must be exercised")
	})

	t.Run("invalid_inputs_never_persist", func(t *testing.T) {
		f := newFixture(t, db)
		f.call("create_profile", map[string]any{"email": "not-an-email"}, false)
		f.call("create_organization", map[string]any{"name": "  \t"}, false)
		f.call("create_claim", map[string]any{"content": " "}, false)
		f.call("create_claim", map[string]any{"content": "Fato válido", "skillIds": []string{uuid.NewString()}}, false)
		f.call("create_participation", map[string]any{"description": "Sem contexto"}, false)
		f.call("create_education", map[string]any{"institution": "Instituto", "title": "Curso", "type": "COURSE", "startedAt": "2025-01-01", "completedAt": "2024-01-01"}, false)
		f.call("create_education", map[string]any{"institution": "Instituto", "title": "Curso", "type": "COURSE", "workloadHours": -1}, false)
		f.call("search_skills", map[string]any{"query": "Go", "limit": 0}, false)
		f.call("update_claim", map[string]any{"claimId": uuid.NewString(), "content": "Fato", "participationId": uuid.NewString(), "clearParticipation": true}, false)
		for _, table := range []string{"profile", "organization", "claim", "participation", "education", "knowledge_chunk", "vector_store"} {
			require.Zero(t, f.count(table), table)
		}
		require.Zero(t, f.provider.calls.Load(), "validation must happen before embedding")
	})

	t.Run("provider_failure_rolls_back_canonical_and_index_changes", func(t *testing.T) {
		f := newFixture(t, db)
		claim := f.call("create_claim", map[string]any{"content": "Conteúdo original confirmado"}, true)
		f.provider.mode.Store(1)
		f.call("create_claim", map[string]any{"content": "Não deve persistir"}, false)
		failure, err := f.client.CallTool(t.Context(), &mcp.CallToolParams{Name: "create_education", Arguments: map[string]any{"institution": "Teste", "title": "Teste", "type": "COURSE"}})
		require.NoError(t, err)
		require.True(t, failure.IsError)
		require.Nil(t, failure.StructuredContent)
		require.Contains(t, failure.Content[0].(*mcp.TextContent).Text, "falha interna")
		require.NotContains(t, failure.Content[0].(*mcp.TextContent).Text, "synthetic provider")
		f.call("update_claim", map[string]any{"claimId": claim["id"], "content": "Não deve substituir"}, false)
		require.EqualValues(t, 1, f.count("claim"))
		require.EqualValues(t, 1, f.count("knowledge_chunk"))
		require.EqualValues(t, 1, f.count("vector_store"))
		require.Equal(t, claim["content"], f.call("get_claim", map[string]any{"claimId": claim["id"]}, true)["content"])
		f.provider.mode.Store(2)
		f.call("create_claim", map[string]any{"content": "Dimensão incorreta"}, false)
		require.EqualValues(t, 1, f.count("claim"))
		f.provider.mode.Store(0)
		f.call("update_claim", map[string]any{"claimId": claim["id"], "content": "Alteração confirmada"}, true)
	})

	t.Run("cancelled_embedding_rolls_back_the_transaction", func(t *testing.T) {
		f := newFixture(t, db)
		f.provider.mode.Store(3)
		ctx, cancel := context.WithTimeout(t.Context(), 30*time.Millisecond)
		defer cancel()
		_, err := f.services.Claim.Create(ctx, command.CreateClaim{Content: "Não confirmar após cancelamento"})
		require.Error(t, err)
		require.Zero(t, f.count("claim"))
		require.Zero(t, f.count("knowledge_chunk"))
		require.Zero(t, f.count("vector_store"))
	})

	t.Run("related_context_updates_are_atomic", func(t *testing.T) {
		f := newFixture(t, db)
		org := f.call("create_organization", map[string]any{"name": "Organização original"}, true)
		experience := f.call("create_experience", map[string]any{"employerId": org["id"], "role": "Desenvolvedor", "employmentType": "CLT"}, true)
		participation := f.call("create_participation", map[string]any{"experienceId": experience["id"], "description": "Desenvolvi a conciliação de pagamentos."}, true)
		var original domain.KnowledgeChunk
		require.NoError(t, db.First(&original).Error)
		f.provider.mode.Store(1)
		f.call("update_organization", map[string]any{"organizationId": org["id"], "name": "Alteração não confirmada"}, false)
		require.Equal(t, org["name"], f.call("get_organization", map[string]any{"organizationId": org["id"]}, true)["name"])
		var unchanged domain.KnowledgeChunk
		require.NoError(t, db.First(&unchanged).Error)
		require.Equal(t, original.Hash, unchanged.Hash)
		f.provider.mode.Store(0)
		f.call("update_organization", map[string]any{"organizationId": org["id"], "name": "Nome confirmado"}, true)
		var document domain.VectorDocument
		require.NoError(t, db.Where("id = ?", participation["id"]).First(&document).Error)
		require.Contains(t, document.Content, "Nome confirmado")
	})

	t.Run("oversized_combined_document_is_rejected_before_embedding", func(t *testing.T) {
		f := newFixture(t, db)
		ids := make([]string, 0, 40)
		for n := range 40 {
			skill, err := f.services.Skill.Create(t.Context(), command.CreateSkill{Name: fmt.Sprintf("%02d-%s", n, strings.Repeat("a", 77))})
			require.NoError(t, err)
			ids = append(ids, skill.ID)
		}
		_, err := f.services.Claim.Create(t.Context(), command.CreateClaim{Content: strings.Repeat("a", 4000), SkillIDs: ids})
		require.ErrorContains(t, err, "limite seguro")
		require.Zero(t, f.count("claim"))
		require.Zero(t, f.count("claim_skill"))
		require.Zero(t, f.count("vector_store"))
		require.Zero(t, f.provider.calls.Load())
	})

	t.Run("concurrent_singleton_and_canonical_skill", func(t *testing.T) {
		f := newFixture(t, db)
		var group sync.WaitGroup
		var profiles atomic.Int64
		ids := make(chan string, 8)
		errors := make(chan error, 16)
		for range 8 {
			group.Go(func() {
				if _, err := f.services.Profile.Create(t.Context(), command.CreateProfile{Name: new("Concurrent")}); err == nil {
					profiles.Add(1)
				} else if message, expected := exception.PublicMessage(err); !expected || !strings.Contains(message, "Já existe") {
					errors <- fmt.Errorf("unexpected concurrent profile error: %w", err)
				}
				skill, err := f.services.Skill.Create(t.Context(), command.CreateSkill{Name: " Go ", Aliases: []string{"Golang"}})
				if err != nil {
					errors <- err
					return
				}
				ids <- skill.ID
			})
		}
		group.Wait()
		close(ids)
		close(errors)
		require.Len(t, ids, 8)
		for err := range errors {
			require.NoError(t, err)
		}
		var canonical string
		for id := range ids {
			if canonical == "" {
				canonical = id
			}
			require.Equal(t, canonical, id)
		}
		require.EqualValues(t, 1, profiles.Load())
		require.EqualValues(t, 1, f.count("profile"))
		require.EqualValues(t, 1, f.count("skill"))
	})

	t.Run("reindex_is_cached_and_recovers_missing_vectors", func(t *testing.T) {
		f := newFixture(t, db)
		claim := f.call("create_claim", map[string]any{"content": "Fato profissional para reindexação"}, true)
		calls := f.provider.calls.Load()
		embedder := &constantEmbedder{}
		v := vector.NewStore(embedder, "gemini-embedding-2", 1536)
		runner := vector.NewReindexer(f.repo, vector.NewIndexer(v), slog.New(slog.NewTextHandler(io.Discard, nil)))
		require.NoError(t, runner.Run(t.Context(), true))
		require.Equal(t, calls, f.provider.calls.Load())
		require.Zero(t, embedder.calls.Load(), "reindex must reuse unchanged content")
		require.NoError(t, f.repo.DeleteVector(t.Context(), uuid.MustParse(claim["id"].(string))))
		require.Zero(t, f.count("knowledge_chunk"))
		require.NoError(t, runner.Run(t.Context(), true))
		require.EqualValues(t, 1, f.count("knowledge_chunk"))
		require.EqualValues(t, 1, embedder.calls.Load(), "missing vector must be regenerated")
		changed := vector.NewStore(&constantEmbedder{}, "next-model", 1536)
		require.NoError(t, vector.NewReindexer(f.repo, vector.NewIndexer(changed), slog.Default()).Run(t.Context(), false))
		var chunk domain.KnowledgeChunk
		require.NoError(t, db.First(&chunk).Error)
		require.Equal(t, "next-model:1536", chunk.Model)
	})

	t.Run("http_limits_and_legacy_protocol", func(t *testing.T) {
		f := newFixture(t, db)
		// The 2026-07-28 protocol removed ping; verify it only for clients that explicitly negotiate 2025-11-25.
		legacy, err := mcp.NewClient(&mcp.Implementation{Name: "dianysma-legacy-test", Version: "1"}, nil).Connect(
			t.Context(), &mcp.StreamableClientTransport{Endpoint: f.url + "/mcp"},
			&mcp.ClientSessionOptions{ProtocolVersion: "2025-11-25"},
		)
		require.NoError(t, err)
		t.Cleanup(func() { _ = legacy.Close() })
		require.NoError(t, legacy.Ping(t.Context(), nil))

		request, err := http.NewRequestWithContext(t.Context(), http.MethodPost, f.url+"/mcp", strings.NewReader(`{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"legacy","version":"1"}}}`))
		require.NoError(t, err)
		request.Header.Set("Content-Type", "application/json")
		request.Header.Set("Accept", "application/json, text/event-stream")
		response, err := http.DefaultClient.Do(request)
		require.NoError(t, err)
		defer func() {
			if err := response.Body.Close(); err != nil {
				t.Errorf("close legacy response body: %v", err)
			}
		}()
		require.Equal(t, 200, response.StatusCode)
		var result map[string]any
		require.NoError(t, json.NewDecoder(response.Body).Decode(&result))
		require.Equal(t, "2025-11-25", result["result"].(map[string]any)["protocolVersion"])
		badOrigin, err := http.NewRequestWithContext(t.Context(), http.MethodPost, f.url+"/mcp", strings.NewReader(`{}`))
		require.NoError(t, err)
		badOrigin.Header.Set("Origin", "https://untrusted.invalid")
		badOrigin.Header.Set("Sec-Fetch-Site", "cross-site")
		r, err := http.DefaultClient.Do(badOrigin)
		require.NoError(t, err)
		if err := r.Body.Close(); err != nil {
			t.Errorf("close denied response body: %v", err)
		}
		require.Equal(t, 403, r.StatusCode)
		large, err := http.NewRequestWithContext(t.Context(), http.MethodPost, f.url+"/mcp", strings.NewReader(strings.Repeat(" ", (1<<20)+1)))
		require.NoError(t, err)
		large.Header.Set("Content-Type", "application/json")
		large.Header.Set("Accept", "application/json, text/event-stream")
		r, err = http.DefaultClient.Do(large)
		require.NoError(t, err)
		if err := r.Body.Close(); err != nil {
			t.Errorf("close oversized response body: %v", err)
		}
		require.Equal(t, 413, r.StatusCode)
	})

	t.Run("logs_have_only_start_and_completion_without_payload", func(t *testing.T) {
		f := newFixture(t, db)
		f.call("create_profile", map[string]any{"name": "CONFIDENTIAL_TEST_NAME"}, true)
		f.call("create_profile", map[string]any{"name": "CONFIDENTIAL_DUPLICATE"}, false)
		logs := f.logs.text()
		require.NotContains(t, logs, "CONFIDENTIAL")
		var messages []map[string]any
		for line := range strings.SplitSeq(strings.TrimSpace(logs), "\n") {
			var entry map[string]any
			require.NoError(t, json.Unmarshal([]byte(line), &entry))
			if strings.HasPrefix(fmt.Sprint(entry["msg"]), "tool: ") {
				messages = append(messages, entry)
			}
		}
		require.Len(t, messages, 4)
		require.Equal(t, "tool: create_profile", messages[0]["msg"])
		require.EqualValues(t, 200, messages[1]["status"])
		require.Equal(t, "success", messages[1]["event.outcome"])
		require.Equal(t, "failure", messages[3]["event.outcome"])
		require.NotEmpty(t, messages[1]["http.request.id"])
	})
}

type constantEmbedder struct{ calls atomic.Int64 }

func (e *constantEmbedder) Embed(context.Context, string) ([]float32, error) {
	e.calls.Add(1)
	v := make([]float32, 1536)
	v[0] = 1
	return v, nil
}
