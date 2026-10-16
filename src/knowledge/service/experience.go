package service

import (
	"context"
	"strings"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type ExperienceService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *ExperienceService) Get(ctx context.Context, c command.GetExperience) (result.ExperienceDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ExperienceDetails{}, err
	}
	id, err := parseID(c.ExperienceID)
	if err != nil {
		return result.ExperienceDetails{}, err
	}
	value, err := s.repo.Experiences.Get(ctx, id)
	if err != nil {
		return result.ExperienceDetails{}, missing(err, "Experiência não encontrada.")
	}
	return toExperienceDetails(value), nil
}

func (s *ExperienceService) List(ctx context.Context, c command.ListExperiences) (result.ExperienceList, error) {
	if err := s.validate(c); err != nil {
		return result.ExperienceList{}, err
	}
	values, err := s.repo.Experiences.List(ctx, limit(c.Limit, 20))
	if err != nil {
		return result.ExperienceList{}, err
	}
	return result.ExperienceList{Experiences: mapSlice(values, toExperienceDetails)}, nil
}

func (s *ExperienceService) Delete(ctx context.Context, c command.DeleteExperience) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.ExperienceID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Experiences.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if linked, err := tx.Participations.Has(ctx, "experience_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		deleted, err := tx.Experiences.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func experienceFrom(ctx context.Context, tx *repository.Store, c command.CreateExperience) (domain.Experience, error) {
	var value domain.Experience
	if err := dateRange(c.StartedAt, c.EndedAt); err != nil {
		return value, err
	}
	employer, err := reference(ctx, tx.Organizations, c.EmployerID, "A organização empregadora não existe.")
	if err != nil {
		return value, err
	}
	client, err := optionalReference(ctx, tx.Organizations, c.ClientID, "A organização cliente não existe.")
	if err != nil {
		return value, err
	}
	start, err := date(c.StartedAt)
	if err != nil {
		return value, err
	}
	end, err := date(c.EndedAt)
	if err != nil {
		return value, err
	}
	return domain.Experience{EmployerID: employer, ClientID: client, Role: strings.TrimSpace(c.Role),
		EmploymentType: strings.TrimSpace(c.EmploymentType), Description: c.Description, StartedAt: start, EndedAt: end}, nil
}

func (s *ExperienceService) Create(ctx context.Context, c command.CreateExperience) (result.ExperienceDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ExperienceDetails{}, err
	}
	var out result.ExperienceDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		value, err := experienceFrom(ctx, tx, c)
		if err != nil {
			return err
		}
		if err := tx.Experiences.Create(ctx, &value); err != nil {
			return err
		}
		out = toExperienceDetails(value)
		return nil
	})
	return out, integrityError(err)
}

func (s *ExperienceService) Update(ctx context.Context, c command.UpdateExperience) (result.ExperienceDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ExperienceDetails{}, err
	}
	id, err := parseID(c.ExperienceID)
	if err != nil {
		return result.ExperienceDetails{}, err
	}
	var out result.ExperienceDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		previous, err := tx.Experiences.Lock(ctx, id)
		if err != nil {
			return missing(err, "Experiência não encontrada.")
		}
		value, err := experienceFrom(ctx, tx, c.CreateExperience)
		if err != nil {
			return err
		}
		value.Auditable = previous.Auditable
		if err := tx.Experiences.Update(ctx, id, &value); err != nil {
			return err
		}
		ids, err := tx.ParticipationIDsForExperience(ctx, id)
		if err != nil {
			return err
		}
		if err := s.indexer.ReindexParticipations(ctx, tx, ids); err != nil {
			return err
		}
		out = toExperienceDetails(value)
		return nil
	})
	return out, integrityError(err)
}
