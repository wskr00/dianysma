package vector

import (
	"context"
	"crypto/sha256"
	"fmt"
	"sort"
	"strings"
	"unicode/utf8"

	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
)

const maxIndexedCharacters = 6000

type Indexer struct{ vectors *Store }

func NewIndexer(vectors *Store) *Indexer { return &Indexer{vectors: vectors} }

func (i *Indexer) IndexClaim(ctx context.Context, tx *repository.Store, claim domain.Claim) error {
	names := make([]string, 0, len(claim.Skills))
	for _, skill := range claim.Skills {
		names = append(names, skill.Name)
	}
	sort.Slice(names, func(a, b int) bool { return strings.ToLower(names[a]) < strings.ToLower(names[b]) })
	title := "none"
	if len(names) > 0 {
		title = strings.Join(names, ", ")
	}
	text := fmt.Sprintf("title: %s | text: %s", title, claim.Content)
	return i.index(ctx, tx, "claim", text, domain.KnowledgeChunk{ID: claim.ID, ClaimID: &claim.ID})
}

func (i *Indexer) IndexEducation(ctx context.Context, tx *repository.Store, value domain.Education) error {
	text := fmt.Sprintf("title: %s | text: institution: %s | type: %s | degree: %s | description: %s",
		value.Title, value.Institution, value.Type, valueOrNone(value.Degree), valueOrNone(value.Description))
	return i.index(ctx, tx, "education", text, domain.KnowledgeChunk{ID: value.ID, EducationID: &value.ID})
}

func (i *Indexer) IndexParticipation(ctx context.Context, tx *repository.Store, id uuid.UUID) error {
	p, err := tx.DetailedParticipation(ctx, id)
	if err != nil {
		return err
	}
	if p.Description == nil || strings.TrimSpace(*p.Description) == "" {
		return tx.DeleteVector(ctx, id)
	}
	experience, project := "none", "none"
	if p.Experience != nil {
		experience = p.Experience.Role + " at " + p.Experience.Employer.Name
	}
	if p.Project != nil {
		project = p.Project.Name
		if p.Project.Organization != nil {
			project += " at " + p.Project.Organization.Name
		}
	}
	text := fmt.Sprintf("title: %s | project: %s | text: %s", experience, project, strings.TrimSpace(*p.Description))
	return i.index(ctx, tx, "participation", text, domain.KnowledgeChunk{ID: id, ParticipationID: &id})
}

func (i *Indexer) ReindexParticipations(ctx context.Context, tx *repository.Store, ids []uuid.UUID) error {
	for _, id := range ids {
		if _, err := tx.Participations.Lock(ctx, id); err != nil {
			return err
		}
		if err := i.IndexParticipation(ctx, tx, id); err != nil {
			return err
		}
	}
	return nil
}

func (i *Indexer) index(ctx context.Context, tx *repository.Store, source, text string, chunk domain.KnowledgeChunk) error {
	if utf8.RuneCountInString(text) > maxIndexedCharacters {
		return exception.Invalid("O texto combinado ultrapassa o limite seguro para indexação semântica. Reduza o conteúdo ou a quantidade de competências associadas.")
	}
	chunk.Model = i.vectors.ModelSignature()
	chunk.Hash = fmt.Sprintf("%x", sha256.Sum256([]byte(chunk.Model+"\n"+text)))
	existing, err := tx.Chunk(ctx, chunk.ID)
	if err != nil && !repository.IsNotFound(err) {
		return err
	}
	if err == nil && existing.Hash == chunk.Hash {
		return nil
	}
	document := domain.VectorDocument{ID: chunk.ID, Content: text,
		Metadata: map[string]string{"sourceType": source, "sourceId": chunk.ID.String()}}
	return i.vectors.Add(ctx, tx, &document, &chunk)
}

func valueOrNone(value *string) string {
	if value == nil || strings.TrimSpace(*value) == "" {
		return "none"
	}
	return strings.TrimSpace(*value)
}
