package repository

import (
	"context"
	"errors"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// Store binds repositories to one database handle or transaction.
type Store struct {
	db             *gorm.DB
	Profiles       Entities[domain.Profile]
	Organizations  Entities[domain.Organization]
	Experiences    Entities[domain.Experience]
	Projects       Entities[domain.Project]
	Participations Entities[domain.Participation]
	Claims         Entities[domain.Claim]
	Skills         Entities[domain.Skill]
	Educations     Entities[domain.Education]
	Languages      Entities[domain.Language]
}

func New(db *gorm.DB) *Store {
	return &Store{db: db,
		Profiles: Entities[domain.Profile]{db}, Organizations: Entities[domain.Organization]{db},
		Experiences: Entities[domain.Experience]{db}, Projects: Entities[domain.Project]{db},
		Participations: Entities[domain.Participation]{db}, Claims: Entities[domain.Claim]{db},
		Skills: Entities[domain.Skill]{db}, Educations: Entities[domain.Education]{db}, Languages: Entities[domain.Language]{db},
	}
}

func (s *Store) Transaction(ctx context.Context, fn func(*Store) error) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error { return fn(New(tx)) })
}

type Entities[T any] struct{ db *gorm.DB }

func (r Entities[T]) Get(ctx context.Context, id uuid.UUID) (T, error) {
	var v T
	err := r.db.WithContext(ctx).Where("id = ?", id).First(&v).Error
	return v, err
}

func (r Entities[T]) Lock(ctx context.Context, id uuid.UUID) (T, error) {
	var v T
	err := r.db.WithContext(ctx).Clauses(clause.Locking{Strength: "UPDATE"}).Where("id = ?", id).First(&v).Error
	return v, err
}

func (r Entities[T]) List(ctx context.Context, limit int) ([]T, error) {
	items := make([]T, 0)
	err := r.db.WithContext(ctx).Order("updated_at DESC").Order("id DESC").Limit(limit).Find(&items).Error
	return items, err
}

func (r Entities[T]) Create(ctx context.Context, value *T) error {
	return r.db.WithContext(ctx).Omit(clause.Associations).Create(value).Error
}

func (r Entities[T]) Update(ctx context.Context, id uuid.UUID, value *T) error {
	// Select all scalar fields so nil, false and zero values are persisted as replacements.
	// unqueryvet flags this GORM update syntax although it intentionally updates every scalar field.
	//nolint:unqueryvet
	return r.db.WithContext(ctx).Model(value).Where("id = ?", id).Select("*").
		Omit("id", "created_at", clause.Associations).Updates(value).Error
}

func (r Entities[T]) Delete(ctx context.Context, id uuid.UUID) (bool, error) {
	result := r.db.WithContext(ctx).Where("id = ?", id).Delete(new(T))
	return result.RowsAffected > 0, result.Error
}

func (r Entities[T]) Has(ctx context.Context, column string, value uuid.UUID) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(new(T)).Where(clause.Eq{Column: clause.Column{Name: column}, Value: value}).Count(&n).Error
	return n > 0, err
}

func IsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }

func IsConstraint(err error, name string) bool {
	var pg *pgconn.PgError
	return errors.As(err, &pg) && pg.ConstraintName == name
}

func IsForeignKeyViolation(err error) bool {
	var pg *pgconn.PgError
	return errors.As(err, &pg) && pg.Code == "23503"
}
