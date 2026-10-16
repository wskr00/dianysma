package domain

import (
	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Participation struct {
	audit.Auditable
	ExperienceID *uuid.UUID  `gorm:"type:uuid"`
	ProjectID    *uuid.UUID  `gorm:"type:uuid"`
	Description  *string     `gorm:"type:text"`
	Experience   *Experience `gorm:"foreignKey:ExperienceID"`
	Project      *Project    `gorm:"foreignKey:ProjectID"`
}
