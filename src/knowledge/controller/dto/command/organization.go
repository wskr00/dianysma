package command

type GetOrganization struct {
	OrganizationID string `json:"organizationId" jsonschema:"UUID da organização." validate:"required,uuid"`
}

type SearchOrganizations struct {
	Query string `json:"query" jsonschema:"Nome completo ou trecho do nome da organização. Obrigatório e limitado a 120 caracteres." validate:"notblank,max=120"`
	Limit *int   `json:"limit,omitempty" jsonschema:"Máximo de resultados. Padrão: 10; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateOrganization struct {
	Name string `json:"name" jsonschema:"Nome obrigatório da organização, até 120 caracteres." validate:"notblank,max=120"`
}

type UpdateOrganization struct {
	OrganizationID string `json:"organizationId" jsonschema:"UUID da organização que será atualizada." validate:"required,uuid"`
	CreateOrganization
}

type DeleteOrganization struct {
	OrganizationID string `json:"organizationId" jsonschema:"UUID da organização que será excluída." validate:"required,uuid"`
}
