package service

import (
	"context"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type ParticipationService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *ParticipationService) Get(ctx context.Context, c command.GetParticipation) (result.ParticipationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ParticipationDetails{}, err
	}
	id, err := parseID(c.ParticipationID)
	if err != nil {
		return result.ParticipationDetails{}, err
	}
	value, err := s.repo.Participations.Get(ctx, id)
	if err != nil {
		return result.ParticipationDetails{}, missing(err, "Participação não encontrada.")
	}
	return toParticipationDetails(value), nil
}

func (s *ParticipationService) List(ctx context.Context, c command.ListParticipations) (result.ParticipationList, error) {
	if err := s.validate(c); err != nil {
		return result.ParticipationList{}, err
	}
	values, err := s.repo.Participations.List(ctx, limit(c.Limit, 20))
	if err != nil {
		return result.ParticipationList{}, err
	}
	return result.ParticipationList{Participations: mapSlice(values, toParticipationDetails)}, nil
}

func (s *ParticipationService) Delete(ctx context.Context, c command.DeleteParticipation) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.ParticipationID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Participations.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if linked, err := tx.Claims.Has(ctx, "participation_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		if err := tx.DeleteVector(ctx, id); err != nil {
			return err
		}
		deleted, err := tx.Participations.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func participationFrom(ctx context.Context, tx *repository.Store, c command.CreateParticipation) (domain.Participation, error) {
	experience, err := optionalReference(ctx, tx.Experiences, c.ExperienceID, "A experiência informada para a participação não existe.")
	if err != nil {
		return domain.Participation{}, err
	}
	project, err := optionalReference(ctx, tx.Projects, c.ProjectID, "O projeto informado para a participação não existe.")
	if err != nil {
		return domain.Participation{}, err
	}
	return domain.Participation{ExperienceID: experience, ProjectID: project, Description: c.Description}, nil
}

func (s *ParticipationService) Create(ctx context.Context, c command.CreateParticipation) (result.ParticipationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ParticipationDetails{}, err
	}
	var out result.ParticipationDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		value, err := participationFrom(ctx, tx, c)
		if err != nil {
			return err
		}
		if err := tx.Participations.Create(ctx, &value); err != nil {
			return err
		}
		if err := s.indexer.IndexParticipation(ctx, tx, value.ID); err != nil {
			return err
		}
		out = toParticipationDetails(value)
		return nil
	})
	return out, integrityError(err)
}

func (s *ParticipationService) Update(ctx context.Context, c command.UpdateParticipation) (result.ParticipationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ParticipationDetails{}, err
	}
	id, err := parseID(c.ParticipationID)
	if err != nil {
		return result.ParticipationDetails{}, err
	}
	var out result.ParticipationDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		previous, err := tx.Participations.Lock(ctx, id)
		if err != nil {
			return missing(err, "Participação não encontrada.")
		}
		value, err := participationFrom(ctx, tx, c.CreateParticipation)
		if err != nil {
			return err
		}
		value.Auditable = previous.Auditable
		if err := tx.Participations.Update(ctx, id, &value); err != nil {
			return err
		}
		if err := s.indexer.IndexParticipation(ctx, tx, id); err != nil {
			return err
		}
		out = toParticipationDetails(value)
		return nil
	})
	return out, integrityError(err)
}
