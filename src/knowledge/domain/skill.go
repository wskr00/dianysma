package domain

import (
	"github.com/lib/pq"
	"github.com/wskr00/dianysma/src/core/audit"
)

type Skill struct {
	audit.Auditable
	Name    string         `gorm:"size:80;not null"`
	Aliases pq.StringArray `gorm:"type:text[]"`
}
