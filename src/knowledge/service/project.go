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

type ProjectService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *ProjectService) Get(ctx context.Context, c command.GetProject) (result.ProjectDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ProjectDetails{}, err
	}
	id, err := parseID(c.ProjectID)
	if err != nil {
		return result.ProjectDetails{}, err
	}
	value, err := s.repo.Projects.Get(ctx, id)
	if err != nil {
		return result.ProjectDetails{}, missing(err, "Projeto não encontrado.")
	}
	return toProjectDetails(value), nil
}

func (s *ProjectService) List(ctx context.Context, c command.ListProjects) (result.ProjectList, error) {
	if err := s.validate(c); err != nil {
		return result.ProjectList{}, err
	}
	values, err := s.repo.Projects.List(ctx, limit(c.Limit, 20))
	if err != nil {
		return result.ProjectList{}, err
	}
	return result.ProjectList{Projects: mapSlice(values, toProjectDetails)}, nil
}

func (s *ProjectService) Delete(ctx context.Context, c command.DeleteProject) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.ProjectID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Projects.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if linked, err := tx.Participations.Has(ctx, "project_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		deleted, err := tx.Projects.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func projectFrom(ctx context.Context, tx *repository.Store, c command.CreateProject) (domain.Project, error) {
	id, err := optionalReference(ctx, tx.Organizations, c.OrganizationID, "A organização do projeto não existe.")
	return domain.Project{OrganizationID: id, Name: strings.TrimSpace(c.Name), Description: c.Description}, err
}

func (s *ProjectService) Create(ctx context.Context, c command.CreateProject) (result.ProjectDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ProjectDetails{}, err
	}
	var out result.ProjectDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		value, err := projectFrom(ctx, tx, c)
		if err != nil {
			return err
		}
		if err := tx.Projects.Create(ctx, &value); err != nil {
			return err
		}
		out = toProjectDetails(value)
		return nil
	})
	return out, integrityError(err)
}

func (s *ProjectService) Update(ctx context.Context, c command.UpdateProject) (result.ProjectDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ProjectDetails{}, err
	}
	id, err := parseID(c.ProjectID)
	if err != nil {
		return result.ProjectDetails{}, err
	}
	var out result.ProjectDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		previous, err := tx.Projects.Lock(ctx, id)
		if err != nil {
			return missing(err, "Projeto não encontrado.")
		}
		value, err := projectFrom(ctx, tx, c.CreateProject)
		if err != nil {
			return err
		}
		value.Auditable = previous.Auditable
		if err := tx.Projects.Update(ctx, id, &value); err != nil {
			return err
		}
		ids, err := tx.ParticipationIDsForProject(ctx, id)
		if err != nil {
			return err
		}
		if err := s.indexer.ReindexParticipations(ctx, tx, ids); err != nil {
			return err
		}
		out = toProjectDetails(value)
		return nil
	})
	return out, integrityError(err)
}
