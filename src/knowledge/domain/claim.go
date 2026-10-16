package domain

import (
	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Claim struct {
	audit.Auditable
	ParticipationID *uuid.UUID `gorm:"type:uuid"`
	Content         string     `gorm:"type:text;not null"`
	Skills          []Skill    `gorm:"many2many:claim_skill"`
}
