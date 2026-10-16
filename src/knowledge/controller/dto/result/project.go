package result

import (
	"time"
)

type ProjectDetails struct {
	ID             string    `json:"id" jsonschema:"UUID do projeto; use-o para criar uma participação." format:"uuid"`
	OrganizationID *string   `json:"organizationId" jsonschema:"UUID da organização associada; null quando o projeto não pertence a uma organização." format:"uuid"`
	Name           string    `json:"name" jsonschema:"Nome do projeto."`
	Description    *string   `json:"description" jsonschema:"Descrição do projeto, quando informada."`
	CreatedAt      time.Time `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt      time.Time `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type ProjectList struct {
	Projects []ProjectDetails `json:"projects" jsonschema:"Projetos encontrados, ordenados pela atualização mais recente. A lista pode estar vazia."`
}
