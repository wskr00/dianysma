package domain

import (
	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Language struct {
	audit.Auditable
	ProfileID   uuid.UUID           `gorm:"type:uuid;not null"`
	Name        string              `gorm:"size:50;not null"`
	Proficiency LanguageProficiency `gorm:"type:languageproficiency;not null"`
}
