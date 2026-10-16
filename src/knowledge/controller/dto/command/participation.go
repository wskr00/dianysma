package command

type GetParticipation struct {
	ParticipationID string `json:"participationId" jsonschema:"UUID da participação profissional." validate:"required,uuid"`
}

type ListParticipations struct {
	Limit *int `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateParticipation struct {
	ExperienceID *string `json:"experienceId,omitempty" jsonschema:"UUID de uma experiência existente para associar, se aplicável. Informe este campo, 'projectId' ou ambos." validate:"required_without=ProjectID,omitempty,uuid"`
	ProjectID    *string `json:"projectId,omitempty" jsonschema:"UUID de um projeto existente para associar, se aplicável. Informe este campo, 'experienceId' ou ambos." validate:"omitempty,uuid"`
	Description  *string `json:"description,omitempty" jsonschema:"Descrição opcional da participação, até 4.000 caracteres." validate:"omitempty,max=4000"`
}

type UpdateParticipation struct {
	ParticipationID string `json:"participationId" jsonschema:"UUID da participação que será atualizada." validate:"required,uuid"`
	CreateParticipation
}

type DeleteParticipation struct {
	ParticipationID string `json:"participationId" jsonschema:"UUID da participação que será excluída." validate:"required,uuid"`
}
