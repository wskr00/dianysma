package result

type KnowledgeSearchHit struct {
	SourceID   string  `json:"sourceId" jsonschema:"UUID do registro canônico que originou este resultado." format:"uuid"`
	SourceType string  `json:"sourceType" jsonschema:"Tipo do registro: claim, participation ou education. Use esse tipo para escolher a ferramenta get correspondente."`
	Excerpt    string  `json:"excerpt" jsonschema:"Trecho do conteúdo indexado, limitado a 500 caracteres. Consulte o registro canônico para obter os dados completos."`
	Similarity float64 `json:"similarity" jsonschema:"Pontuação de similaridade semântica usada para ordenar os resultados; não representa probabilidade nem confirmação factual."`
}

type KnowledgeSearchResult struct {
	Results []KnowledgeSearchHit `json:"results" jsonschema:"Resultados mais relevantes da busca semântica. A lista pode estar vazia."`
}
