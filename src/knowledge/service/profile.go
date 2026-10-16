package service

import (
	"context"

	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/repository"
)

type ProfileService struct {
	inputValidation
	repo *repository.Store
}

func (s *ProfileService) Get(ctx context.Context, c command.GetProfile) (result.ProfileDetails, error) {
	value, err := s.repo.FirstProfile(ctx)
	if err != nil {
		return result.ProfileDetails{}, missing(err, "O perfil ainda não foi criado.")
	}
	return toProfileDetails(value), nil
}

func (s *ProfileService) Create(ctx context.Context, c command.CreateProfile) (result.ProfileDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ProfileDetails{}, err
	}
	v := domain.Profile{Name: c.Name, Email: c.Email, Phone: c.Phone, City: c.City, State: c.State,
		Country: c.Country, LinkedInURL: c.LinkedInURL, GitHubURL: c.GitHubURL, PortfolioURL: c.PortfolioURL}
	if err := s.repo.Profiles.Create(ctx, &v); err != nil {
		return result.ProfileDetails{}, integrityError(err)
	}
	return toProfileDetails(v), nil
}

func (s *ProfileService) Update(ctx context.Context, c command.UpdateProfile) (result.ProfileDetails, error) {
	if err := s.validate(c); err != nil {
		return result.ProfileDetails{}, err
	}
	var out result.ProfileDetails
	err := s.repo.Transaction(ctx, func(tx *repository.Store) error {
		p, err := tx.FirstProfile(ctx)
		if err != nil {
			return missing(err, "O perfil ainda não foi criado; use create_profile primeiro.")
		}
		p, err = tx.Profiles.Lock(ctx, p.ID)
		if err != nil {
			return err
		}
		p.Name, p.Email, p.Phone, p.City, p.State = c.Name, c.Email, c.Phone, c.City, c.State
		p.Country, p.LinkedInURL, p.GitHubURL, p.PortfolioURL = c.Country, c.LinkedInURL, c.GitHubURL, c.PortfolioURL
		if err := tx.Profiles.Update(ctx, p.ID, &p); err != nil {
			return err
		}
		out = toProfileDetails(p)
		return nil
	})
	return out, integrityError(err)
}
