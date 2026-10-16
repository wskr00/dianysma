package result

import (
	"time"
)

type ParticipationDetails struct {
	ID           string    `json:"id" jsonschema:"UUID da participação; use-o para associar fatos profissionais." format:"uuid"`
	ExperienceID *string   `json:"experienceId" jsonschema:"UUID da experiência associada; null quando não há experiência." format:"uuid"`
	ProjectID    *string   `json:"projectId" jsonschema:"UUID do projeto associado; null quando não há projeto." format:"uuid"`
	Description  *string   `json:"description" jsonschema:"Descrição da participação, quando informada."`
	CreatedAt    time.Time `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt    time.Time `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type ParticipationList struct {
	Participations []ParticipationDetails `json:"participations" jsonschema:"Participações profissionais encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia."`
}
