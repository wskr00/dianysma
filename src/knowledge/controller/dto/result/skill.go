package result

type SkillDetails struct {
	ID      string   `json:"id" jsonschema:"UUID da competência canônica; use-o ao associar a competência a um fato profissional." format:"uuid"`
	Name    string   `json:"name" jsonschema:"Nome canônico da competência."`
	Aliases []string `json:"aliases" jsonschema:"Nomes alternativos usados para localizar esta competência; não são competências distintas."`
}

type SkillSearchResult struct {
	Skills []SkillDetails `json:"skills" jsonschema:"Competências que correspondem à busca, ordenadas por nome. A lista pode estar vazia."`
}
