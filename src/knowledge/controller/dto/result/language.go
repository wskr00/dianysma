package result

import (
	"time"

	"github.com/wskr00/dianysma/src/knowledge/domain"
)

type LanguageDetails struct {
	ID          string                     `json:"id" jsonschema:"UUID do idioma no perfil pessoal." format:"uuid"`
	Name        string                     `json:"name" jsonschema:"Nome do idioma."`
	Proficiency domain.LanguageProficiency `json:"proficiency" jsonschema:"Proficiência: BASIC (básica), INTERMEDIATE (intermediária), ADVANCED (avançada), FLUENT (fluente) ou NATIVE (nativa)."`
	CreatedAt   time.Time                  `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt   time.Time                  `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type LanguageList struct {
	Languages []LanguageDetails `json:"languages" jsonschema:"Idiomas do perfil, em ordem alfabética. A lista pode estar vazia."`
}
