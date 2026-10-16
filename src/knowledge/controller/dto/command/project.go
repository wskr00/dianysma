package command

type GetProject struct {
	ProjectID string `json:"projectId" jsonschema:"UUID do projeto." validate:"required,uuid"`
}

type ListProjects struct {
	Limit *int `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateProject struct {
	OrganizationID *string `json:"organizationId,omitempty" jsonschema:"UUID de uma organização existente que contextualiza o projeto. Pesquise com 'search_organizations' e, se necessário, crie-a com 'create_organization'; omita se não houver organização associada." validate:"omitempty,uuid"`
	Name           string  `json:"name" jsonschema:"Nome obrigatório do projeto, até 200 caracteres." validate:"notblank,max=200"`
	Description    *string `json:"description,omitempty" jsonschema:"Descrição opcional do projeto."`
}

type UpdateProject struct {
	ProjectID string `json:"projectId" jsonschema:"UUID do projeto que será atualizado." validate:"required,uuid"`
	CreateProject
}

type DeleteProject struct {
	ProjectID string `json:"projectId" jsonschema:"UUID do projeto que será excluído." validate:"required,uuid"`
}
