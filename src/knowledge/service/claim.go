package service

import (
	"context"

	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type ClaimService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *ClaimService) Get(ctx context.Context, c command.GetClaim) (result.ClaimDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ClaimDetails{}, err
	}
	id, err := parseID(c.ClaimID)
	if err != nil {
		return result.ClaimDetails{}, err
	}
	value, err := s.repo.Claim(ctx, id)
	if err != nil {
		return result.ClaimDetails{}, missing(err, "Fato profissional não encontrado.")
	}
	return toClaimDetails(value), nil
}

func (s *ClaimService) List(ctx context.Context, c command.ListClaims) (result.ClaimList, error) {
	if err := s.validate(c); err != nil {
		return result.ClaimList{}, err
	}
	participationID, err := optionalUUID(c.ParticipationID)
	if err != nil {
		return result.ClaimList{}, err
	}
	values, err := s.repo.ListClaims(ctx, participationID, limit(c.Limit, 20))
	if err != nil {
		return result.ClaimList{}, err
	}
	return result.ClaimList{Claims: mapSlice(values, toClaimDetails)}, nil
}

func (s *ClaimService) Delete(ctx context.Context, c command.DeleteClaim) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.ClaimID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Claims.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if err := tx.DeleteVector(ctx, id); err != nil {
			return err
		}
		deleted, err := tx.Claims.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func resolveSkills(ctx context.Context, tx *repository.Store, values []string) ([]domain.Skill, error) {
	ids := make([]uuid.UUID, 0, len(values))
	seen := make(map[uuid.UUID]bool, len(values))
	for _, value := range values {
		id, err := parseID(value)
		if err != nil {
			return nil, err
		}
		if !seen[id] {
			seen[id] = true
			ids = append(ids, id)
		}
	}
	skills, err := tx.SkillsByID(ctx, ids)
	if err != nil {
		return nil, err
	}
	if len(skills) != len(ids) {
		return nil, exception.NotFound("Um ou mais IDs de competência informados não existem.")
	}
	return skills, nil
}

func (s *ClaimService) Create(ctx context.Context, c command.CreateClaim) (result.ClaimDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ClaimDetails{}, err
	}
	var out result.ClaimDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		participation, err := optionalReference(ctx, tx.Participations, c.ParticipationID, "Participação não encontrada.")
		if err != nil {
			return err
		}
		skills, err := resolveSkills(ctx, tx, c.SkillIDs)
		if err != nil {
			return err
		}
		value := domain.Claim{ParticipationID: participation, Content: c.Content, Skills: skills}
		if err := tx.Claims.Create(ctx, &value); err != nil {
			return err
		}
		if err := tx.ReplaceClaimSkills(ctx, &value, skills); err != nil {
			return err
		}
		if err := s.indexer.IndexClaim(ctx, tx, value); err != nil {
			return err
		}
		out = toClaimDetails(value)
		return nil
	})
	return out, integrityError(err)
}

func (s *ClaimService) Update(ctx context.Context, c command.UpdateClaim) (result.ClaimDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ClaimDetails{}, err
	}
	if c.ClearParticipation != nil && *c.ClearParticipation && c.ParticipationID != nil {
		return result.ClaimDetails{}, exception.Invalid("Informe participationId para associar uma participação ou clearParticipation=true para desassociá-la; não informe ambos.")
	}
	id, err := parseID(c.ClaimID)
	if err != nil {
		return result.ClaimDetails{}, err
	}
	var out result.ClaimDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Claims.Lock(ctx, id); err != nil {
			return missing(err, "Fato profissional não encontrado.")
		}
		value, err := tx.Claim(ctx, id)
		if err != nil {
			return err
		}
		value.Content = c.Content
		if c.ParticipationID != nil {
			value.ParticipationID, err = optionalReference(ctx, tx.Participations, c.ParticipationID, "Participação não encontrada.")
			if err != nil {
				return err
			}
		} else if c.ClearParticipation != nil && *c.ClearParticipation {
			value.ParticipationID = nil
		}
		if c.SkillIDs != nil {
			value.Skills, err = resolveSkills(ctx, tx, c.SkillIDs)
			if err != nil {
				return err
			}
			if err := tx.ReplaceClaimSkills(ctx, &value, value.Skills); err != nil {
				return err
			}
		}
		if err := tx.Claims.Update(ctx, id, &value); err != nil {
			return err
		}
		if err := s.indexer.IndexClaim(ctx, tx, value); err != nil {
			return err
		}
		out = toClaimDetails(value)
		return nil
	})
	return out, integrityError(err)
}
