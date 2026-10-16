package domain

import (
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Experience struct {
	audit.Auditable
	EmployerID     uuid.UUID     `gorm:"type:uuid;not null"`
	ClientID       *uuid.UUID    `gorm:"type:uuid"`
	Role           string        `gorm:"size:200;not null"`
	EmploymentType string        `gorm:"size:40;not null"`
	Description    *string       `gorm:"type:text"`
	StartedAt      *pgtype.Date  `gorm:"type:date"`
	EndedAt        *pgtype.Date  `gorm:"type:date"`
	Employer       Organization  `gorm:"foreignKey:EmployerID"`
	Client         *Organization `gorm:"foreignKey:ClientID"`
}
