package service

import (
	"context"
	"strings"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type KnowledgeSearchService struct {
	inputValidation
	repo    *repository.Store
	vectors *vector.Store
}

func (s *KnowledgeSearchService) Search(ctx context.Context, c command.SearchKnowledge) (result.KnowledgeSearchResult, error) {
	if err := s.validate(c); err != nil {
		return result.KnowledgeSearchResult{}, err
	}
	hits, err := s.vectors.Search(ctx, s.repo, strings.TrimSpace(c.Query), limit(c.Limit, 5))
	if err != nil {
		return result.KnowledgeSearchResult{}, err
	}
	out := result.KnowledgeSearchResult{Results: make([]result.KnowledgeSearchHit, 0, len(hits))}
	for _, hit := range hits {
		out.Results = append(out.Results, result.KnowledgeSearchHit{SourceID: hit.SourceID.String(), SourceType: hit.SourceType,
			Excerpt: excerpt(hit.Content), Similarity: hit.Similarity})
	}
	return out, nil
}

func excerpt(text string) string {
	runes := []rune(text)
	if len(runes) <= 500 {
		return text
	}
	return strings.TrimRight(string(runes[:499]), " \t\r\n") + "…"
}
