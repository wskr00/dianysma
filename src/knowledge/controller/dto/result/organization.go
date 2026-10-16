package result

import (
	"time"
)

type OrganizationDetails struct {
	ID        string    `json:"id" jsonschema:"UUID da organização; use-o nos campos employerId, clientId ou organizationId." format:"uuid"`
	Name      string    `json:"name" jsonschema:"Nome da organização."`
	CreatedAt time.Time `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt time.Time `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type OrganizationSearchResult struct {
	Organizations []OrganizationDetails `json:"organizations" jsonschema:"Organizações que correspondem à busca. A lista pode estar vazia."`
}
