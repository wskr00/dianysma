package result

import (
	"time"
)

type ClaimDetails struct {
	ID              string         `json:"id" jsonschema:"UUID do fato profissional." format:"uuid"`
	Content         string         `json:"content" jsonschema:"Conteúdo factual do registro profissional."`
	ParticipationID *string        `json:"participationId" jsonschema:"UUID da participação associada; null quando o fato é autônomo." format:"uuid"`
	Skills          []SkillDetails `json:"skills" jsonschema:"Competências canônicas associadas ao fato."`
	CreatedAt       time.Time      `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt       time.Time      `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type ClaimList struct {
	Claims []ClaimDetails `json:"claims" jsonschema:"Fatos profissionais encontrados, ordenados pela atualização mais recente. A lista pode estar vazia."`
}
