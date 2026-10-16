package service

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/wskr00/dianysma/src/core/validation"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

type inputValidation struct {
	validator *validation.Validator
}

type Services struct {
	Profile       *ProfileService
	Organization  *OrganizationService
	Experience    *ExperienceService
	Project       *ProjectService
	Participation *ParticipationService
	Claim         *ClaimService
	Skill         *SkillService
	Education     *EducationService
	Language      *LanguageService
	Knowledge     *KnowledgeSearchService
}

func New(repo *repository.Store, indexer *vector.Indexer, vectors *vector.Store) (*Services, error) {
	v, err := validation.New()
	if err != nil {
		return nil, err
	}
	input := inputValidation{validator: v}
	return &Services{
		Profile:       &ProfileService{inputValidation: input, repo: repo},
		Organization:  &OrganizationService{inputValidation: input, repo: repo, indexer: indexer},
		Experience:    &ExperienceService{inputValidation: input, repo: repo, indexer: indexer},
		Project:       &ProjectService{inputValidation: input, repo: repo, indexer: indexer},
		Participation: &ParticipationService{inputValidation: input, repo: repo, indexer: indexer},
		Claim:         &ClaimService{inputValidation: input, repo: repo, indexer: indexer},
		Skill:         &SkillService{inputValidation: input, repo: repo},
		Education:     &EducationService{inputValidation: input, repo: repo, indexer: indexer},
		Language:      &LanguageService{inputValidation: input, repo: repo},
		Knowledge:     &KnowledgeSearchService{inputValidation: input, repo: repo, vectors: vectors},
	}, nil
}

func (d inputValidation) validate(input any) error {
	if err := d.validator.Struct(input); err != nil {
		return exception.Invalid(err.Error())
	}
	return nil
}

func missing(err error, message string) error {
	if repository.IsNotFound(err) {
		return exception.NotFound(message)
	}
	return err
}

func parseID(value string) (uuid.UUID, error) {
	id, err := uuid.Parse(value)
	if err != nil || id == uuid.Nil {
		return uuid.Nil, exception.Invalid("Informe um UUID válido e diferente de zero.")
	}
	return id, nil
}

func optionalUUID(value *string) (*uuid.UUID, error) {
	if value == nil {
		return nil, nil
	}
	id, err := parseID(*value)
	if err != nil {
		return nil, err
	}
	return &id, nil
}

func reference[T any](ctx context.Context, repo repository.Entities[T], value string, message string) (uuid.UUID, error) {
	id, err := parseID(value)
	if err != nil {
		return id, err
	}
	exists, err := repo.Has(ctx, "id", id)
	if err != nil {
		return id, err
	}
	if !exists {
		return id, exception.NotFound(message)
	}
	return id, nil
}

func optionalReference[T any](ctx context.Context, repo repository.Entities[T], value *string, message string) (*uuid.UUID, error) {
	if value == nil {
		return nil, nil
	}
	id, err := reference(ctx, repo, *value, message)
	if err != nil {
		return nil, err
	}
	return &id, nil
}

func date(value *string) (*pgtype.Date, error) {
	if value == nil {
		return nil, nil
	}
	parsed, err := time.Parse(time.DateOnly, *value)
	if err != nil {
		return nil, exception.Invalid("Informe datas no formato AAAA-MM-DD.")
	}
	return &pgtype.Date{Time: parsed, Valid: true}, nil
}

func dateRange(start, end *string) error {
	if start != nil && end != nil && *end < *start {
		return exception.Invalid("A data final não pode ser anterior à data inicial.")
	}
	return nil
}

func stripToNull(value *string) *string {
	if value == nil {
		return nil
	}
	s := strings.TrimSpace(*value)
	if s == "" {
		return nil
	}
	return &s
}

func limit(value *int, fallback int) int {
	if value == nil {
		return fallback
	}
	return *value
}

func mapSlice[T, R any](values []T, convert func(T) R) []R {
	result := make([]R, 0, len(values))
	for _, value := range values {
		result = append(result, convert(value))
	}
	return result
}

func integrityError(err error) error {
	if repository.IsConstraint(err, "uq_profile_singleton") {
		return exception.Invalid("Já existe um perfil nesta instalação. Use update_profile para alterá-lo.")
	}
	if repository.IsConstraint(err, "uk_participation_context") {
		return exception.Invalid("Já existe uma participação com esta combinação de experiência e projeto.")
	}
	if repository.IsForeignKeyViolation(err) {
		return exception.Invalid("A operação possui vínculos inválidos ou registros relacionados. Verifique os vínculos antes de tentar novamente.")
	}
	return err
}
