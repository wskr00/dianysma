package repository

import (
	"context"
	"strings"

	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

func (s *Store) FirstProfile(ctx context.Context) (domain.Profile, error) {
	var profile domain.Profile
	err := s.db.WithContext(ctx).Order("created_at").First(&profile).Error
	return profile, err
}

func (s *Store) SearchOrganizations(ctx context.Context, query string, limit int) ([]domain.Organization, error) {
	items := make([]domain.Organization, 0)
	err := s.db.WithContext(ctx).Where("name ILIKE ?", literalContains(query)).Order("name").Limit(limit).Find(&items).Error
	return items, err
}

func (s *Store) SearchSkills(ctx context.Context, query string, limit int) ([]domain.Skill, error) {
	items := make([]domain.Skill, 0)
	pattern := literalContains(query)
	err := s.db.WithContext(ctx).Where(`name ILIKE ? OR EXISTS (
		SELECT 1 FROM unnest(COALESCE(aliases, ARRAY[]::text[])) AS a(alias) WHERE a.alias ILIKE ?
	)`, pattern, pattern).Order("name").Limit(limit).Find(&items).Error
	return items, err
}

func (s *Store) CreateCanonicalSkill(ctx context.Context, skill *domain.Skill) error {
	insert := s.db.WithContext(ctx).Clauses(clause.OnConflict{DoNothing: true}).Create(skill)
	if insert.Error != nil {
		return insert.Error
	}
	if insert.RowsAffected > 0 {
		return nil
	}
	var existing domain.Skill
	if err := s.db.WithContext(ctx).Where("upper(name) = upper(?)", skill.Name).First(&existing).Error; err != nil {
		return err
	}
	*skill = existing
	return nil
}

func (s *Store) SkillsByID(ctx context.Context, ids []uuid.UUID) ([]domain.Skill, error) {
	items := make([]domain.Skill, 0, len(ids))
	if len(ids) == 0 {
		return items, nil
	}
	err := s.db.WithContext(ctx).Where("id IN ?", ids).Order("lower(name)").Order("id").Find(&items).Error
	return items, err
}

func (s *Store) Claim(ctx context.Context, id uuid.UUID) (domain.Claim, error) {
	var claim domain.Claim
	err := s.db.WithContext(ctx).Preload("Skills", func(db *gorm.DB) *gorm.DB { return db.Order("lower(name)").Order("id") }).Where("id = ?", id).First(&claim).Error
	return claim, err
}

func (s *Store) ListClaims(ctx context.Context, participationID *uuid.UUID, limit int) ([]domain.Claim, error) {
	items := make([]domain.Claim, 0)
	query := s.db.WithContext(ctx).Preload("Skills", func(db *gorm.DB) *gorm.DB { return db.Order("lower(name)").Order("id") })
	if participationID != nil {
		query = query.Where("participation_id = ?", participationID)
	}
	err := query.Order("updated_at DESC").Order("id DESC").Limit(limit).Find(&items).Error
	return items, err
}

func (s *Store) ReplaceClaimSkills(ctx context.Context, claim *domain.Claim, skills []domain.Skill) error {
	return s.db.WithContext(ctx).Model(claim).Omit("Skills.*").Association("Skills").Replace(skills)
}

func (s *Store) DetailedParticipation(ctx context.Context, id uuid.UUID) (domain.Participation, error) {
	var value domain.Participation
	err := s.db.WithContext(ctx).Preload("Experience.Employer").Preload("Project.Organization").Where("id = ?", id).First(&value).Error
	return value, err
}

func (s *Store) ParticipationIDsForExperience(ctx context.Context, id uuid.UUID) ([]uuid.UUID, error) {
	return s.participationIDs(ctx, s.db.Model(&domain.Participation{}).Where("experience_id = ?", id))
}

func (s *Store) ParticipationIDsForProject(ctx context.Context, id uuid.UUID) ([]uuid.UUID, error) {
	return s.participationIDs(ctx, s.db.Model(&domain.Participation{}).Where("project_id = ?", id))
}

func (s *Store) ParticipationIDsForOrganization(ctx context.Context, id uuid.UUID) ([]uuid.UUID, error) {
	experiences := s.db.Model(&domain.Experience{}).Select("id").Where("employer_id = ?", id)
	projects := s.db.Model(&domain.Project{}).Select("id").Where("organization_id = ?", id)
	return s.participationIDs(ctx, s.db.Model(&domain.Participation{}).Where("experience_id IN (?) OR project_id IN (?)", experiences, projects))
}

func (s *Store) participationIDs(ctx context.Context, query *gorm.DB) ([]uuid.UUID, error) {
	ids := make([]uuid.UUID, 0)
	err := query.WithContext(ctx).Order("id").Pluck("id", &ids).Error
	return ids, err
}

func (s *Store) ListLanguages(ctx context.Context, limit int) ([]domain.Language, error) {
	items := make([]domain.Language, 0)
	err := s.db.WithContext(ctx).Order("name").Order("id").Limit(limit).Find(&items).Error
	return items, err
}

func literalContains(query string) string {
	return "%" + strings.NewReplacer(`\`, `\\`, "%", `\%`, "_", `\_`).Replace(query) + "%"
}
