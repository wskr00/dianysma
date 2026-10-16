package result

import (
	"time"
)

type ProfileDetails struct {
	ID           string    `json:"id" jsonschema:"UUID do único perfil pessoal desta instalação." format:"uuid"`
	Name         *string   `json:"name" jsonschema:"Nome pessoal, quando informado."`
	Email        *string   `json:"email" jsonschema:"E-mail pessoal, quando informado."`
	Phone        *string   `json:"phone" jsonschema:"Telefone como texto, quando informado."`
	City         *string   `json:"city" jsonschema:"Cidade, quando informada."`
	State        *string   `json:"state" jsonschema:"Estado ou região, quando informado."`
	Country      *string   `json:"country" jsonschema:"País, quando informado."`
	LinkedInURL  *string   `json:"linkedinUrl" jsonschema:"URL do perfil do LinkedIn, quando informada."`
	GitHubURL    *string   `json:"githubUrl" jsonschema:"URL do perfil do GitHub, quando informada."`
	PortfolioURL *string   `json:"portfolioUrl" jsonschema:"URL do portfólio, quando informada."`
	CreatedAt    time.Time `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt    time.Time `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}
