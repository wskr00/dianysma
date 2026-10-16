package service

import (
	"context"
	"sort"
	"strings"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
)

type SkillService struct {
	inputValidation
	repo *repository.Store
}

func (s *SkillService) Create(ctx context.Context, c command.CreateSkill) (result.SkillDetails, error) {
	if err := s.validate(c); err != nil {
		return result.SkillDetails{}, err
	}
	aliases := make([]string, 0, len(c.Aliases))
	seen := make(map[string]bool, len(c.Aliases))
	for _, alias := range c.Aliases {
		alias = strings.TrimSpace(alias)
		key := strings.ToLower(alias)
		if !seen[key] {
			seen[key] = true
			aliases = append(aliases, alias)
		}
	}
	sort.Slice(aliases, func(a, b int) bool { return strings.ToLower(aliases[a]) < strings.ToLower(aliases[b]) })
	value := domain.Skill{Name: strings.TrimSpace(c.Name), Aliases: aliases}
	if err := s.repo.CreateCanonicalSkill(ctx, &value); err != nil {
		return result.SkillDetails{}, err
	}
	return toSkillDetails(value), nil
}

func (s *SkillService) Search(ctx context.Context, c command.SearchSkills) (result.SkillSearchResult, error) {
	if err := s.validate(c); err != nil {
		return result.SkillSearchResult{}, err
	}
	values, err := s.repo.SearchSkills(ctx, strings.TrimSpace(c.Query), limit(c.Limit, 10))
	if err != nil {
		return result.SkillSearchResult{}, err
	}
	return result.SkillSearchResult{Skills: mapSlice(values, toSkillDetails)}, nil
}
