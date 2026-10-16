package command

type GetExperience struct {
	ExperienceID string `json:"experienceId" jsonschema:"UUID da experiência profissional." validate:"required,uuid"`
}

type ListExperiences struct {
	Limit *int `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateExperience struct {
	EmployerID     string  `json:"employerId" jsonschema:"UUID da organização empregadora. Use 'search_organizations' para localizá-la; se não existir, crie-a com 'create_organization' primeiro." validate:"required,uuid"`
	ClientID       *string `json:"clientId,omitempty" jsonschema:"UUID de uma organização cliente, quando aplicável. Use uma organização existente; pode ser igual à empregadora." validate:"omitempty,uuid"`
	Role           string  `json:"role" jsonschema:"Cargo ou função, obrigatório e limitado a 200 caracteres." validate:"notblank,max=200"`
	EmploymentType string  `json:"employmentType" jsonschema:"Tipo de vínculo em texto livre, obrigatório e limitado a 40 caracteres; por exemplo, CLT, PJ, estágio ou freelancer." validate:"notblank,max=40"`
	Description    *string `json:"description,omitempty" jsonschema:"Descrição opcional da experiência."`
	StartedAt      *string `json:"startedAt,omitempty" jsonschema:"Data inicial opcional no formato ISO 8601 (AAAA-MM-DD)." validate:"omitempty,datetime=2006-01-02"`
	EndedAt        *string `json:"endedAt,omitempty" jsonschema:"Data final opcional no formato ISO 8601 (AAAA-MM-DD); não pode ser anterior a startedAt." validate:"omitempty,datetime=2006-01-02"`
}

type UpdateExperience struct {
	ExperienceID string `json:"experienceId" jsonschema:"UUID da experiência que será atualizada." validate:"required,uuid"`
	CreateExperience
}

type DeleteExperience struct {
	ExperienceID string `json:"experienceId" jsonschema:"UUID da experiência que será excluída." validate:"required,uuid"`
}
