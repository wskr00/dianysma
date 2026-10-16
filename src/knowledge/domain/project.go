package domain

import (
	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Project struct {
	audit.Auditable
	OrganizationID *uuid.UUID    `gorm:"type:uuid"`
	Name           string        `gorm:"size:200;not null"`
	Description    *string       `gorm:"type:text"`
	Organization   *Organization `gorm:"foreignKey:OrganizationID"`
}
