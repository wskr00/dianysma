package command

type GetProfile struct {
}

type CreateProfile struct {
	Name         *string `json:"name,omitempty" jsonschema:"Nome pessoal, até 120 caracteres." validate:"omitempty,max=120"`
	Email        *string `json:"email,omitempty" jsonschema:"E-mail pessoal, até 50 caracteres." validate:"omitempty,max=50,optionalemail"`
	Phone        *string `json:"phone,omitempty" jsonschema:"Telefone como texto, até 20 caracteres." validate:"omitempty,max=20"`
	City         *string `json:"city,omitempty" jsonschema:"Cidade, até 40 caracteres." validate:"omitempty,max=40"`
	State        *string `json:"state,omitempty" jsonschema:"Estado, até 40 caracteres." validate:"omitempty,max=40"`
	Country      *string `json:"country,omitempty" jsonschema:"País, até 40 caracteres." validate:"omitempty,max=40"`
	LinkedInURL  *string `json:"linkedinUrl,omitempty" jsonschema:"URL do LinkedIn, até 150 caracteres." validate:"omitempty,max=150"`
	GitHubURL    *string `json:"githubUrl,omitempty" jsonschema:"URL do GitHub, até 150 caracteres." validate:"omitempty,max=150"`
	PortfolioURL *string `json:"portfolioUrl,omitempty" jsonschema:"URL do portfólio, até 150 caracteres." validate:"omitempty,max=150"`
}

type UpdateProfile = CreateProfile
