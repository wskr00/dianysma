package service

import (
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/result"
	"github.com/wskr00/dianysma/src/knowledge/domain"
)

func optionalID(v *uuid.UUID) *string {
	if v == nil {
		return nil
	}
	id := v.String()
	return &id
}
func optionalDate(v *pgtype.Date) *string {
	if v == nil || !v.Valid {
		return nil
	}
	date := v.Time.Format(time.DateOnly)
	return &date
}

func optionalWorkloadHours(v *int32) *int {
	if v == nil {
		return nil
	}
	hours := int(*v)
	return &hours
}

func toClaimDetails(v domain.Claim) result.ClaimDetails {
	return result.ClaimDetails{
		ID:              v.ID.String(),
		Content:         v.Content,
		ParticipationID: optionalID(v.ParticipationID),
		Skills:          mapSkills(v.Skills),
		CreatedAt:       v.CreatedAt,
		UpdatedAt:       v.UpdatedAt,
	}
}
func toEducationDetails(v domain.Education) result.EducationDetails {
	return result.EducationDetails{
		ID:            v.ID.String(),
		Institution:   v.Institution,
		Type:          v.Type,
		Title:         v.Title,
		Degree:        v.Degree,
		Description:   v.Description,
		WorkloadHours: optionalWorkloadHours(v.WorkloadHours),
		StartedAt:     optionalDate(v.StartedAt),
		CompletedAt:   optionalDate(v.CompletedAt),
		Expected:      v.Expected,
		CreatedAt:     v.CreatedAt,
		UpdatedAt:     v.UpdatedAt,
	}
}
func toExperienceDetails(v domain.Experience) result.ExperienceDetails {
	return result.ExperienceDetails{
		ID:             v.ID.String(),
		EmployerID:     v.EmployerID.String(),
		ClientID:       optionalID(v.ClientID),
		Role:           v.Role,
		EmploymentType: v.EmploymentType,
		Description:    v.Description,
		StartedAt:      optionalDate(v.StartedAt),
		EndedAt:        optionalDate(v.EndedAt),
		CreatedAt:      v.CreatedAt,
		UpdatedAt:      v.UpdatedAt,
	}
}
func toLanguageDetails(v domain.Language) result.LanguageDetails {
	return result.LanguageDetails{
		ID:          v.ID.String(),
		Name:        v.Name,
		Proficiency: v.Proficiency,
		CreatedAt:   v.CreatedAt,
		UpdatedAt:   v.UpdatedAt,
	}
}
func toOrganizationDetails(v domain.Organization) result.OrganizationDetails {
	return result.OrganizationDetails{
		ID:        v.ID.String(),
		Name:      v.Name,
		CreatedAt: v.CreatedAt,
		UpdatedAt: v.UpdatedAt,
	}
}
func toParticipationDetails(v domain.Participation) result.ParticipationDetails {
	return result.ParticipationDetails{
		ID:           v.ID.String(),
		ExperienceID: optionalID(v.ExperienceID),
		ProjectID:    optionalID(v.ProjectID),
		Description:  v.Description,
		CreatedAt:    v.CreatedAt,
		UpdatedAt:    v.UpdatedAt,
	}
}
func toProfileDetails(v domain.Profile) result.ProfileDetails {
	return result.ProfileDetails{
		ID:           v.ID.String(),
		Name:         v.Name,
		Email:        v.Email,
		Phone:        v.Phone,
		City:         v.City,
		State:        v.State,
		Country:      v.Country,
		LinkedInURL:  v.LinkedInURL,
		GitHubURL:    v.GitHubURL,
		PortfolioURL: v.PortfolioURL,
		CreatedAt:    v.CreatedAt,
		UpdatedAt:    v.UpdatedAt,
	}
}
func toProjectDetails(v domain.Project) result.ProjectDetails {
	return result.ProjectDetails{
		ID:             v.ID.String(),
		OrganizationID: optionalID(v.OrganizationID),
		Name:           v.Name,
		Description:    v.Description,
		CreatedAt:      v.CreatedAt,
		UpdatedAt:      v.UpdatedAt,
	}
}
func toSkillDetails(v domain.Skill) result.SkillDetails {
	return result.SkillDetails{
		ID:      v.ID.String(),
		Name:    v.Name,
		Aliases: append([]string{}, v.Aliases...),
	}
}
func mapSkills(skills []domain.Skill) []result.SkillDetails {
	out := make([]result.SkillDetails, 0, len(skills))
	for _, v := range skills {
		out = append(out, toSkillDetails(v))
	}
	return out
}
