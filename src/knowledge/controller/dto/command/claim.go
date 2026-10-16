package command

type GetClaim struct {
	ClaimID string `json:"claimId" jsonschema:"UUID do fato profissional que será consultado." validate:"required,uuid"`
}

type ListClaims struct {
	ParticipationID *string `json:"participationId,omitempty" jsonschema:"UUID opcional da participação; omita para incluir todos os fatos." validate:"omitempty,uuid"`
	Limit           *int    `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateClaim struct {
	Content         string   `json:"content" jsonschema:"Declaração factual que será registrada. Obrigatória, não pode ficar em branco e aceita até 4.000 caracteres." validate:"notblank,max=4000"`
	ParticipationID *string  `json:"participationId,omitempty" jsonschema:"UUID de uma participação existente para contextualizar o fato. Omita se o fato for autônomo." validate:"omitempty,uuid"`
	SkillIDs        []string `json:"skillIds,omitempty" jsonschema:"UUIDs de competências existentes para associar, até 50. Use 'search_skills' para obter os IDs. Omita ou envie [] se não houver competências." validate:"omitempty,max=50,dive,required,uuid"`
}

type UpdateClaim struct {
	ClaimID            string   `json:"claimId" jsonschema:"UUID do fato profissional retornado por 'get_claim', 'list_claims' ou 'search_knowledge'." validate:"required,uuid"`
	Content            string   `json:"content" jsonschema:"Conteúdo completo que substituirá o conteúdo atual. Obrigatório, não pode ficar em branco e deve ter até 4.000 caracteres." validate:"notblank,max=4000"`
	ParticipationID    *string  `json:"participationId,omitempty" jsonschema:"UUID de uma participação existente para associar. Omita ou envie null para manter o vínculo atual." validate:"omitempty,uuid"`
	ClearParticipation *bool    `json:"clearParticipation,omitempty" jsonschema:"Informe true para remover o vínculo atual com a participação; omita ou informe false/null para mantê-lo."`
	SkillIDs           []string `json:"skillIds,omitempty" jsonschema:"UUIDs das competências que substituirão os vínculos atuais, até 50. Omita ou envie null para mantê-los; envie [] para remover todos." validate:"omitempty,max=50,dive,required,uuid"`
}

type DeleteClaim struct {
	ClaimID string `json:"claimId" jsonschema:"UUID do fato profissional que será excluído." validate:"required,uuid"`
}
