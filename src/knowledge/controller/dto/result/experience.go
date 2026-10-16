package result

import (
	"time"
)

type ExperienceDetails struct {
	ID             string    `json:"id" jsonschema:"UUID da experiência profissional." format:"uuid"`
	EmployerID     string    `json:"employerId" jsonschema:"UUID da organização empregadora." format:"uuid"`
	ClientID       *string   `json:"clientId" jsonschema:"UUID da organização cliente; null quando não há cliente associado." format:"uuid"`
	Role           string    `json:"role" jsonschema:"Cargo ou função exercida."`
	EmploymentType string    `json:"employmentType" jsonschema:"Tipo de vínculo informado como texto livre, por exemplo CLT, PJ, estágio ou freelancer."`
	Description    *string   `json:"description" jsonschema:"Descrição da experiência, quando informada."`
	StartedAt      *string   `json:"startedAt" jsonschema:"Data de início no formato AAAA-MM-DD, quando informada." format:"date"`
	EndedAt        *string   `json:"endedAt" jsonschema:"Data de término no formato AAAA-MM-DD, quando informada." format:"date"`
	CreatedAt      time.Time `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt      time.Time `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type ExperienceList struct {
	Experiences []ExperienceDetails `json:"experiences" jsonschema:"Experiências profissionais encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia."`
}
