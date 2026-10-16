package service

import (
	"context"
	"strings"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
)

type LanguageService struct {
	inputValidation
	repo *repository.Store
}

func (s *LanguageService) Get(ctx context.Context, c command.GetLanguage) (result.LanguageDetails, error) {
	if err := s.validate(c); err != nil {
		return result.LanguageDetails{}, err
	}
	id, err := parseID(c.LanguageID)
	if err != nil {
		return result.LanguageDetails{}, err
	}
	value, err := s.repo.Languages.Get(ctx, id)
	if err != nil {
		return result.LanguageDetails{}, missing(err, "Idioma não encontrado.")
	}
	return toLanguageDetails(value), nil
}

func (s *LanguageService) List(ctx context.Context, c command.ListLanguages) (result.LanguageList, error) {
	if err := s.validate(c); err != nil {
		return result.LanguageList{}, err
	}
	values, err := s.repo.ListLanguages(ctx, limit(c.Limit, 20))
	if err != nil {
		return result.LanguageList{}, err
	}
	return result.LanguageList{Languages: mapSlice(values, toLanguageDetails)}, nil
}

func (s *LanguageService) Delete(ctx context.Context, c command.DeleteLanguage) (result.DeleteResult, error) {
	if err := s.validate(c); err != nil {
		return result.DeleteResult{}, err
	}
	id, err := parseID(c.LanguageID)
	if err != nil {
		return result.DeleteResult{}, err
	}
	out := result.DeleteResult{ID: id.String()}
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		if _, err := tx.Languages.Lock(ctx, id); err != nil {
			if repository.IsNotFound(err) {
				return nil
			}
			return err
		}
		deleted, err := tx.Languages.Delete(ctx, id)
		out.Deleted = deleted
		return err
	})
	return out, integrityError(err)
}

func (s *LanguageService) Create(ctx context.Context, c command.CreateLanguage) (result.LanguageDetails, error) {
	if err := s.validate(c); err != nil {
		return result.LanguageDetails{}, err
	}
	var out result.LanguageDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		profile, err := tx.FirstProfile(ctx)
		if err != nil {
			return missing(err, "Crie o perfil antes de registrar idiomas.")
		}
		value := domain.Language{ProfileID: profile.ID, Name: strings.TrimSpace(c.Name), Proficiency: c.Proficiency}
		if err := tx.Languages.Create(ctx, &value); err != nil {
			return err
		}
		out = toLanguageDetails(value)
		return nil
	})
	return out, integrityError(err)
}

func (s *LanguageService) Update(ctx context.Context, c command.UpdateLanguage) (result.LanguageDetails, error) {
	if err := s.validate(c); err != nil {
		return result.LanguageDetails{}, err
	}
	id, err := parseID(c.LanguageID)
	if err != nil {
		return result.LanguageDetails{}, err
	}
	var out result.LanguageDetails
	err = s.repo.Transaction(ctx, func(tx *repository.Store) error {
		value, err := tx.Languages.Lock(ctx, id)
		if err != nil {
			return missing(err, "Idioma não encontrado.")
		}
		value.Name, value.Proficiency = strings.TrimSpace(c.Name), c.Proficiency
		if err := tx.Languages.Update(ctx, id, &value); err != nil {
			return err
		}
		out = toLanguageDetails(value)
		return nil
	})
	return out, integrityError(err)
}
