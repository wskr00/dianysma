package domain

import "github.com/wskr00/dianysma/src/core/audit"

type Organization struct {
	audit.Auditable
	Name string `gorm:"size:120;not null"`
}
