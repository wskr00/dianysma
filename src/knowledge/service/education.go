package service

import (
	"context"
	"strings"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type EducationService struct {
	inputValidation
	repo    *repository.Store
	indexer *vector.Indexer
}

func (s *EducationService) Get(ctx context.Context, c command.GetEducation) (result.EducationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.EducationDetails{}, err
	}
	id, err := parseID(c.EducationID)
	if err != nil {
		return result.EducationDetails{}, err
	}
	value, err := s.repo.Educations.Get(ctx, id)
	if err != nil {
		return result.EducationDetails{}, missing(err, "Formação não encontrada.")
	}
	return toEducationDetails(value), nil
}

func (s *EducationService) List(ctx context.Context, c command.ListEducation) (result.EducationList, error) {
	if err := s.validate(c); err != nil {
		return result.EducationList{}, err
	}
	values, err := s.repo.Educations.List(ctx, limit(c.Limit, 20))
	if err != nil {
		return result.EducationList{}, err
	}
	return result.EducationList{Educations: mapSlice(values, toEducationDetails)}, nil
}

func (s *EducationService) Delete(ctx context.Context, c command.DeleteEducation) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.EducationID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Educations.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		if err := tx.DeleteVector(ctx, id); err != nil {
			return err
		}
		deleted, err := tx.Educations.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func educationFrom(c command.CreateEducation) (domain.Education, error) {
	var value domain.Education
	if err := dateRange(c.StartedAt, c.CompletedAt); err != nil {
		return value, err
	}
	start, err := date(c.StartedAt)
	if err != nil {
		return value, err
	}
	end, err := date(c.CompletedAt)
	if err != nil {
		return value, err
	}
	return domain.Education{Institution: strings.TrimSpace(c.Institution), Type: c.Type, Title: strings.TrimSpace(c.Title),
		Degree: stripToNull(c.Degree), Description: c.Description, WorkloadHours: c.WorkloadHours,
		StartedAt: start, CompletedAt: end, Expected: c.Expected}, nil
}

func (s *EducationService) Create(ctx context.Context, c command.CreateEducation) (result.EducationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.EducationDetails{}, err
	}
	value, err := educationFrom(c)
	if err != nil {
		return result.EducationDetails{}, err
	}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if err := tx.Educations.Create(ctx, &value); err != nil {
			return err
		}
		return s.indexer.IndexEducation(ctx, tx, value)
	})
	if err != nil {
		return result.EducationDetails{}, integrityError(err)
	}
	return toEducationDetails(value), nil
}

func (s *EducationService) Update(ctx context.Context, c command.UpdateEducation) (result.EducationDetails, error) {
	if err := s.validate(c); err != nil {
		return result.EducationDetails{}, err
	}
	id, err := parseID(c.EducationID)
	if err != nil {
		return result.EducationDetails{}, err
	}
	value, err := educationFrom(c.CreateEducation)
	if err != nil {
		return result.EducationDetails{}, err
	}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		previous, err := tx.Educations.Lock(ctx, id)
		if err != nil {
			return missing(err, "Formação não encontrada.")
		}
		value.Auditable = previous.Auditable
		if err := tx.Educations.Update(ctx, id, &value); err != nil {
			return err
		}
		return s.indexer.IndexEducation(ctx, tx, value)
	})
	if err != nil {
		return result.EducationDetails{}, integrityError(err)
	}
	return toEducationDetails(value), nil
}
