package command

type SearchSkills struct {
	Query string `json:"query" jsonschema:"Nome completo ou trecho do nome ou de um nome alternativo. Obrigatório e limitado a 200 caracteres." validate:"notblank,max=200"`
	Limit *int   `json:"limit,omitempty" jsonschema:"Quantidade máxima de competências retornadas. Padrão: 10; deve estar entre 1 e 25." validate:"omitempty,min=1,max=25"`
}

type CreateSkill struct {
	Name    string   `json:"name" jsonschema:"Nome canônico da competência. Obrigatório, não pode ficar em branco e aceita até 80 caracteres." validate:"notblank,max=80"`
	Aliases []string `json:"aliases,omitempty" jsonschema:"Até 50 nomes alternativos para facilitar buscas. Cada um deve ser preenchido e ter até 80 caracteres. Só são gravados quando uma nova competência é criada." validate:"omitempty,max=50,dive,notblank,max=80"`
}
