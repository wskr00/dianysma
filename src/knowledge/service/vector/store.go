package vector

import (
	"context"
	"errors"
	"fmt"
	"math"

	"github.com/pgvector/pgvector-go"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
)

type Store struct {
	embedder   Embedder
	dimensions int
	model      string
}

func NewStore(embedder Embedder, model string, dimensions int) *Store {
	return &Store{embedder: embedder, model: model, dimensions: dimensions}
}

func (s *Store) ModelSignature() string { return fmt.Sprintf("%s:%d", s.model, s.dimensions) }

func (s *Store) Add(ctx context.Context, tx *repository.Store, document *domain.VectorDocument, chunk *domain.KnowledgeChunk) error {
	embedding, err := s.embed(ctx, document.Content)
	if err != nil {
		return err
	}
	document.Embedding = pgvector.NewVector(embedding)
	return tx.PutVector(ctx, document, chunk)
}

func (s *Store) Search(ctx context.Context, repo *repository.Store, query string, limit int) ([]repository.VectorHit, error) {
	embedding, err := s.embed(ctx, "task: search result | query: "+query)
	if err != nil {
		return nil, err
	}
	return repo.SearchVectors(ctx, embedding, limit)
}

func (s *Store) embed(ctx context.Context, text string) ([]float32, error) {
	values, err := s.embedder.Embed(ctx, text)
	if err != nil {
		return nil, fmt.Errorf("gerar embedding: %w", err)
	}
	if len(values) != s.dimensions {
		return nil, fmt.Errorf("o embedding retornou %d dimensões; esperadas %d", len(values), s.dimensions)
	}
	nonzero := false
	for _, v := range values {
		if math.IsNaN(float64(v)) || math.IsInf(float64(v), 0) {
			return nil, errors.New("o embedding contém um valor não finito")
		}
		nonzero = nonzero || v != 0
	}
	if !nonzero {
		return nil, errors.New("o embedding retornado é um vetor nulo")
	}
	return values, nil
}
