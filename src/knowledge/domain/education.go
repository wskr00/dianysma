package domain

import (
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Education struct {
	audit.Auditable
	Institution   string        `gorm:"size:100;not null"`
	Type          EducationType `gorm:"type:educationtype;not null"`
	Title         string        `gorm:"size:120;not null"`
	Degree        *string       `gorm:"size:100"`
	Description   *string       `gorm:"type:text"`
	WorkloadHours *int32
	StartedAt     *pgtype.Date `gorm:"type:date"`
	CompletedAt   *pgtype.Date `gorm:"type:date"`
	Expected      *bool
}
