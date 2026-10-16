package validation

import (
	"errors"
	"reflect"
	"sort"
	"strings"

	"github.com/go-playground/locales/pt_BR"
	ut "github.com/go-playground/universal-translator"
	"github.com/go-playground/validator/v10"
	"github.com/go-playground/validator/v10/non-standard/validators"
	pt "github.com/go-playground/validator/v10/translations/pt_BR"
)

type Validator struct {
	validate   *validator.Validate
	translator ut.Translator
}

func New() (*Validator, error) {
	v := validator.New(validator.WithRequiredStructEnabled())
	v.RegisterTagNameFunc(func(f reflect.StructField) string {
		name, _, _ := strings.Cut(f.Tag.Get("json"), ",")
		if name == "" || name == "-" {
			return f.Name
		}
		return name
	})
	locale := pt_BR.New()
	translator, ok := ut.New(locale, locale).GetTranslator("pt_BR")
	if !ok {
		return nil, errors.New("tradutor pt_BR não encontrado")
	}
	if err := pt.RegisterDefaultTranslations(v, translator); err != nil {
		return nil, err
	}
	if err := v.RegisterValidation("notblank", validators.NotBlank); err != nil {
		return nil, err
	}
	v.RegisterAlias("optionalemail", "email|len=0")
	if err := v.RegisterTranslation("optionalemail", translator,
		func(t ut.Translator) error {
			return t.Add("optionalemail", "Informe um endereço de e-mail válido.", true)
		},
		func(t ut.Translator, _ validator.FieldError) string { s, _ := t.T("optionalemail"); return s },
	); err != nil {
		return nil, err
	}
	if err := v.RegisterTranslation("notblank", translator,
		func(t ut.Translator) error { return t.Add("notblank", "O campo {0} não pode ficar em branco.", true) },
		func(t ut.Translator, f validator.FieldError) string { s, _ := t.T("notblank", f.Field()); return s },
	); err != nil {
		return nil, err
	}
	if err := v.RegisterTranslation("required_without", translator,
		func(t ut.Translator) error {
			return t.Add("required_without", "Informe uma experiência ou um projeto para contextualizar a participação.", true)
		},
		func(t ut.Translator, _ validator.FieldError) string {
			message, _ := t.T("required_without")
			return message
		},
	); err != nil {
		return nil, err
	}
	return &Validator{validate: v, translator: translator}, nil
}

func (v *Validator) Struct(input any) error {
	err := v.validate.Struct(input)
	if err == nil {
		return nil
	}
	var fields validator.ValidationErrors
	if !errors.As(err, &fields) {
		return err
	}
	messages := make([]string, 0, len(fields))
	for _, f := range fields {
		messages = append(messages, f.Translate(v.translator))
	}
	sort.Strings(messages)
	return errors.New(strings.Join(messages, " "))
}
