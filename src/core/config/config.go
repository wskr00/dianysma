package config

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"maps"
	"net"
	"net/url"
	"os"
	"strconv"
	"strings"
	"time"

	awsconfig "github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/secretsmanager"
	"github.com/caarlos0/env/v11"
	"github.com/joho/godotenv"
)

// Config is loaded once at startup. Precedence is process environment, .env, Secrets Manager, then defaults.
type Config struct {
	Port                       int    `env:"SERVER_PORT" envDefault:"8000"`
	Endpoint                   string `env:"MCP_ENDPOINT_PATH" envDefault:"/mcp"`
	DisableLocalhostProtection bool   `env:"MCP_DISABLE_LOCALHOST_PROTECTION" envDefault:"false"`
	AutoReindex                bool   `env:"KNOWLEDGE_AUTO_REINDEX" envDefault:"true"`
	DatabaseHost               string `env:"DATABASE_HOST" envDefault:"127.0.0.1"`
	DatabasePort               int    `env:"DATABASE_PORT" envDefault:"5432"`
	DatabaseName               string `env:"DATABASE_NAME" envDefault:"dianysma"`
	DatabaseUser               string `env:"DATABASE_USER" envDefault:"dianysma"`
	DatabasePassword           string `env:"DATABASE_PASSWORD"`
	DatabaseSSLMode            string `env:"DATABASE_SSL_MODE" envDefault:"verify-full"`
	DatabaseMigrationHost      string `env:"DATABASE_MIGRATION_HOST"`
	DatabasePoolMax            int    `env:"DATABASE_POOL_MAX_SIZE" envDefault:"4"`
	GeminiAPIKey               string `env:"GEMINI_API_KEY"`
	EmbeddingModel             string `env:"GEMINI_EMBEDDING_MODEL" envDefault:"gemini-embedding-2"`
	EmbeddingDimensions        int    `env:"GEMINI_EMBEDDING_DIMENSIONS" envDefault:"1536"`
}

func Load(ctx context.Context) (Config, error) {
	values := map[string]string{}
	file, err := godotenv.Read(".env")
	if err != nil && !errors.Is(err, os.ErrNotExist) {
		return Config{}, fmt.Errorf("ler .env: %w", err)
	}
	maps.Copy(values, file)
	for _, entry := range os.Environ() {
		k, v, _ := strings.Cut(entry, "=")
		values[k] = v
	}
	if id := values["AWS_SECRET_ID"]; id != "" {
		ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
		defer cancel()
		var options []func(*awsconfig.LoadOptions) error
		if region := values["AWS_REGION"]; region != "" {
			options = append(options, awsconfig.WithRegion(region))
		}
		aws, err := awsconfig.LoadDefaultConfig(ctx, options...)
		if err != nil {
			return Config{}, fmt.Errorf("configurar AWS: %w", err)
		}
		secret, err := secretsmanager.NewFromConfig(aws).GetSecretValue(ctx, &secretsmanager.GetSecretValueInput{SecretId: &id})
		if err != nil {
			return Config{}, fmt.Errorf("obter segredo da aplicação: %w", err)
		}
		if secret.SecretString == nil {
			return Config{}, errors.New("o segredo deve ser um objeto JSON de strings")
		}
		var secretValues map[string]any
		if err := json.Unmarshal([]byte(*secret.SecretString), &secretValues); err != nil {
			return Config{}, errors.New("o segredo deve ser um objeto JSON de strings")
		}
		if secretValues == nil {
			return Config{}, errors.New("o segredo deve ser um objeto JSON de strings")
		}
		for key, value := range secretValues {
			text, ok := value.(string)
			if !ok {
				return Config{}, errors.New("o segredo deve ser um objeto JSON de strings")
			}
			if _, exists := values[key]; !exists {
				values[key] = text
			}
		}
	}
	var c Config
	if err := env.ParseWithOptions(&c, env.Options{Environment: values}); err != nil {
		// Parser errors may contain values; never include them in logs or returned errors.
		return Config{}, errors.New("configuração inválida: verifique os tipos das variáveis de ambiente")
	}
	return c, c.Validate()
}

func (c Config) Validate() error {
	switch {
	case strings.TrimSpace(c.DatabaseHost) == "" || strings.TrimSpace(c.DatabaseName) == "" || strings.TrimSpace(c.DatabaseUser) == "":
		return errors.New("DATABASE_HOST, DATABASE_NAME e DATABASE_USER são obrigatórios")
	case c.Port < 1 || c.Port > 65535 || c.DatabasePort < 1 || c.DatabasePort > 65535:
		return errors.New("as portas devem estar entre 1 e 65535")
	case !strings.HasPrefix(c.Endpoint, "/") || c.Endpoint == "/" || strings.ContainsAny(c.Endpoint, "?#{}\\ \t\r\n") || strings.Contains(c.Endpoint, ".."):
		return errors.New("MCP_ENDPOINT_PATH deve ser um caminho HTTP absoluto válido")
	case c.DatabasePoolMax < 1:
		return errors.New("DATABASE_POOL_MAX_SIZE deve ser positivo")
	case c.EmbeddingDimensions < 1 || c.EmbeddingDimensions > 2000:
		return errors.New("as dimensões devem estar entre 1 e 2000 para o índice HNSW vector")
	case strings.TrimSpace(c.EmbeddingModel) == "":
		return errors.New("configure GEMINI_EMBEDDING_MODEL")
	case len(c.EmbeddingModel) > 80:
		return errors.New("GEMINI_EMBEDDING_MODEL deve ter até 80 bytes")
	}
	switch c.DatabaseSSLMode {
	case "verify-full", "verify-ca", "require", "disable":
	default:
		return errors.New("DATABASE_SSL_MODE deve ser verify-full, verify-ca, require ou disable")
	}
	return nil
}

func (c Config) DSN(migration bool) string {
	host := c.DatabaseHost
	if migration && c.DatabaseMigrationHost != "" {
		host = c.DatabaseMigrationHost
	}
	u := url.URL{Scheme: "postgres", Host: net.JoinHostPort(host, strconv.Itoa(c.DatabasePort)), Path: "/" + c.DatabaseName, User: url.UserPassword(c.DatabaseUser, c.DatabasePassword)}
	q := url.Values{"sslmode": {c.DatabaseSSLMode}, "connect_timeout": {"10"}, "application_name": {"dianysma"}}
	u.RawQuery = q.Encode()
	return u.String()
}
