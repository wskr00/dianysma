package command

type SearchKnowledge struct {
	Query string `json:"query" jsonschema:"Descreva em linguagem natural o conhecimento que procura, por exemplo uma entrega, responsabilidade, participação ou formação. A busca é semântica, não uma correspondência literal; obrigatório, até 2.000 caracteres." validate:"notblank,max=2000"`
	Limit *int   `json:"limit,omitempty" jsonschema:"Quantidade máxima de resultados. Padrão: 5; deve estar entre 1 e 10." validate:"omitempty,min=1,max=10"`
}
