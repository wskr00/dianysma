package command

import "github.com/wskr00/dianysma/src/knowledge/domain"

type GetEducation struct {
	EducationID string `json:"educationId" jsonschema:"UUID da formação." validate:"required,uuid"`
}

type ListEducation struct {
	Limit *int `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateEducation struct {
	Institution   string               `json:"institution" jsonschema:"Instituição de ensino, obrigatória e limitada a 100 caracteres." validate:"notblank,max=100"`
	Type          domain.EducationType `json:"type" jsonschema:"Categoria da formação: CERTIFICATION (certificação), COURSE (curso) ou DEGREE (graduação ou pós-graduação)." validate:"oneof=DEGREE COURSE CERTIFICATION"`
	Title         string               `json:"title" jsonschema:"Título do curso ou formação, obrigatório e limitado a 120 caracteres." validate:"notblank,max=120"`
	Degree        *string              `json:"degree,omitempty" jsonschema:"Grau ou título acadêmico opcional, até 100 caracteres." validate:"omitempty,max=100"`
	Description   *string              `json:"description,omitempty" jsonschema:"Descrição opcional, até 4.000 caracteres." validate:"omitempty,max=4000"`
	WorkloadHours *int32               `json:"workloadHours,omitempty" jsonschema:"Carga horária opcional, em horas; use zero ou um valor positivo." validate:"omitempty,gte=0"`
	StartedAt     *string              `json:"startedAt,omitempty" jsonschema:"Data inicial opcional no formato AAAA-MM-DD." validate:"omitempty,datetime=2006-01-02"`
	CompletedAt   *string              `json:"completedAt,omitempty" jsonschema:"Data de conclusão opcional ou prevista, no formato AAAA-MM-DD; não pode ser anterior a startedAt." validate:"omitempty,datetime=2006-01-02"`
	Expected      *bool                `json:"expected,omitempty" jsonschema:"Informe true quando completedAt for uma previsão, false quando for a data efetiva; omita se não houver data de conclusão."`
}

type UpdateEducation struct {
	EducationID string `json:"educationId" jsonschema:"UUID da formação que será atualizada." validate:"required,uuid"`
	CreateEducation
}

type DeleteEducation struct {
	EducationID string `json:"educationId" jsonschema:"UUID da formação que será excluída." validate:"required,uuid"`
}
