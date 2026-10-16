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

type OrganizationService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *OrganizationService) Get(ctx context.Context, c command.GetOrganization) (result.OrganizationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.OrganizationDetails{}, err
	}
	id, err := parseID(c.OrganizationID)
	if err != nil {
		return result.OrganizationDetails{}, err
	}
	value, err := s.repo.Organizations.Get(ctx, id)
	if err != nil {
		return result.OrganizationDetails{}, missing(err, "Organização não encontrada.")
	}
	return toOrganizationDetails(value), nil
}

func (s *OrganizationService) Delete(ctx context.Context, c command.DeleteOrganization) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.OrganizationID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Organizations.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if linked, err := tx.Experiences.Has(ctx, "employer_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		if linked, err := tx.Experiences.Has(ctx, "client_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		if linked, err := tx.Projects.Has(ctx, "organization_id", id); err != nil {
			return err
		} else if linked {
			return exception.Invalid("Há registros vinculados. Atualize ou remova esses vínculos antes de excluir.")
		}
		deleted, err := tx.Organizations.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func (s *OrganizationService) Search(ctx context.Context, c command.SearchOrganizations) (result.OrganizationSearchResult, error) {
	if err := s.validate(c); err != nil {
		return result.OrganizationSearchResult{}, err
	}
	values, err := s.repo.SearchOrganizations(ctx, strings.TrimSpace(c.Query), limit(c.Limit, 10))
	if err != nil {
		return result.OrganizationSearchResult{}, err
	}
	return result.OrganizationSearchResult{Organizations: mapSlice(values, toOrganizationDetails)}, nil
}

func (s *OrganizationService) Create(ctx context.Context, c command.CreateOrganization) (result.OrganizationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.OrganizationDetails{}, err
	}
	value := domain.Organization{Name: strings.TrimSpace(c.Name)}
	if err := s.repo.Organizations.Create(ctx, &value); err != nil {
		return result.OrganizationDetails{}, err
	}
	return toOrganizationDetails(value), nil
}

func (s *OrganizationService) Update(ctx context.Context, c command.UpdateOrganization) (result.OrganizationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.OrganizationDetails{}, err
	}
	id, err := parseID(c.OrganizationID)
	if err != nil {
		return result.OrganizationDetails{}, err
	}
	var out result.OrganizationDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		value, err := tx.Organizations.Lock(ctx, id)
		if err != nil {
			return missing(err, "Organização não encontrada.")
		}
		value.Name = strings.TrimSpace(c.Name)
		if err := tx.Organizations.Update(ctx, id, &value); err != nil {
			return err
		}
		ids, err := tx.ParticipationIDsForOrganization(ctx, id)
		if err != nil {
			return err
		}
		if err := s.indexer.ReindexParticipations(ctx, tx, ids); err != nil {
			return err
		}
		out = toOrganizationDetails(value)
		return nil
	})
	return out, integrityError(err)
}
