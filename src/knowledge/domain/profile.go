package domain

import "github.com/wskr00/dianysma/src/core/audit"

type Profile struct {
	audit.Auditable
	Name         *string `gorm:"size:120"`
	Email        *string `gorm:"size:50"`
	Phone        *string `gorm:"size:20"`
	City         *string `gorm:"size:40"`
	State        *string `gorm:"size:40"`
	Country      *string `gorm:"size:40"`
	LinkedInURL  *string `gorm:"column:linkedin_url;size:150"`
	GitHubURL    *string `gorm:"column:github_url;size:150"`
	PortfolioURL *string `gorm:"column:portfolio_url;size:150"`
}
