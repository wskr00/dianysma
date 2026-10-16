package repository

import (
	"context"
	"errors"

	"github.com/google/uuid"
	"github.com/pgvector/pgvector-go"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"gorm.io/gorm/clause"
)

func (s *Store) Chunk(ctx context.Context, id uuid.UUID) (domain.KnowledgeChunk, error) {
	var chunk domain.KnowledgeChunk
	err := s.db.WithContext(ctx).Where("id = ?", id).First(&chunk).Error
	return chunk, err
}

func (s *Store) PutVector(ctx context.Context, document *domain.VectorDocument, chunk *domain.KnowledgeChunk) error {
	if err := s.db.WithContext(ctx).Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "id"}}, DoUpdates: clause.AssignmentColumns([]string{"content", "metadata", "embedding"}),
	}).Create(document).Error; err != nil {
		return err
	}
	return s.db.WithContext(ctx).Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "id"}}, DoUpdates: clause.AssignmentColumns([]string{"hash", "model", "indexed_at"}),
	}).Create(chunk).Error
}

func (s *Store) DeleteVector(ctx context.Context, id uuid.UUID) error {
	// The vector FK cascades to KnowledgeChunk; both changes use the caller's transaction.
	return s.db.WithContext(ctx).Where("id = ?", id).Delete(&domain.VectorDocument{}).Error
}

type VectorHit struct {
	SourceID   uuid.UUID
	SourceType string
	Content    string
	Similarity float64
}

func (s *Store) SearchVectors(ctx context.Context, embedding []float32, limit int) ([]VectorHit, error) {
	items := make([]VectorHit, 0)
	value := pgvector.NewVector(embedding)
	err := s.db.WithContext(ctx).Raw(`SELECT COALESCE(c.claim_id, c.participation_id, c.education_id) AS source_id,
		CASE WHEN c.claim_id IS NOT NULL THEN 'claim' WHEN c.participation_id IS NOT NULL THEN 'participation' ELSE 'education' END AS source_type,
		v.content, 1 - (v.embedding <=> ?) AS similarity
		FROM vector_store v JOIN knowledge_chunk c ON c.id = v.id
		ORDER BY v.embedding <=> ? LIMIT ?`, value, value, limit).Scan(&items).Error
	return items, err
}

func (s *Store) HasDifferentModel(ctx context.Context, model string) (bool, error) {
	var exists bool
	err := s.db.WithContext(ctx).Raw("SELECT EXISTS (SELECT 1 FROM knowledge_chunk WHERE model <> ?)", model).Scan(&exists).Error
	return exists, err
}

// SourceIDs fetches a bounded page for resumable reindexing. The table comes only from the fixed source map.
func (s *Store) SourceIDs(ctx context.Context, source string, after uuid.UUID, limit int) ([]uuid.UUID, error) {
	ids := make([]uuid.UUID, 0)
	model := map[string]any{"claim": &domain.Claim{}, "participation": &domain.Participation{}, "education": &domain.Education{}}[source]
	if model == nil {
		return nil, errors.New("tipo de fonte vetorial inválido")
	}
	err := s.db.WithContext(ctx).Model(model).Where("id > ?", after).Order("id").Limit(limit).Pluck("id", &ids).Error
	return ids, err
}
