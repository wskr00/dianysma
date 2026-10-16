package command

import "github.com/wskr00/dianysma/src/knowledge/domain"

type GetLanguage struct {
	LanguageID string `json:"languageId" jsonschema:"UUID do idioma." validate:"required,uuid"`
}

type ListLanguages struct {
	Limit *int `json:"limit,omitempty" jsonschema:"Máximo de registros. Padrão: 20; intervalo: 1–50." validate:"omitempty,min=1,max=50"`
}

type CreateLanguage struct {
	Name        string                     `json:"name" jsonschema:"Nome do idioma, obrigatório e limitado a 50 caracteres." validate:"notblank,max=50"`
	Proficiency domain.LanguageProficiency `json:"proficiency" jsonschema:"Proficiência: BASIC (básica), INTERMEDIATE (intermediária), ADVANCED (avançada), FLUENT (fluente) ou NATIVE (nativa)." validate:"oneof=BASIC INTERMEDIATE ADVANCED FLUENT NATIVE"`
}

type UpdateLanguage struct {
	LanguageID string `json:"languageId" jsonschema:"UUID do idioma que será atualizado." validate:"required,uuid"`
	CreateLanguage
}

type DeleteLanguage struct {
	LanguageID string `json:"languageId" jsonschema:"UUID do idioma que será excluído." validate:"required,uuid"`
}
