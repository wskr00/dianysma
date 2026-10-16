package audit

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Auditable struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey"`
	CreatedAt time.Time `gorm:"not null;autoCreateTime"`
	UpdatedAt time.Time `gorm:"not null;autoUpdateTime"`
}

func (a *Auditable) BeforeCreate(_ *gorm.DB) error {
	if a.ID != uuid.Nil {
		return nil
	}
	id, err := uuid.NewV7()
	if err != nil {
		return err
	}
	a.ID = id
	return nil
}
