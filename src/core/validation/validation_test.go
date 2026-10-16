package validation_test

import (
	"strings"
	"testing"

	"github.com/wskr00/dianysma/src/core/validation"
	"github.com/wskr00/dianysma/src/knowledge/controller/dto/command"
)

func TestStructTranslatesRequiredWithoutToPortuguese(t *testing.T) {
	validator, err := validation.New()
	if err != nil {
		t.Fatal(err)
	}

	err = validator.Struct(command.CreateParticipation{})
	if err == nil {
		t.Fatal("expected validation error when participation has no context")
	}
	if got := err.Error(); got != "Informe uma experiência ou um projeto para contextualizar a participação." {
		t.Fatalf("unexpected public validation message: %q", got)
	}
	if strings.Contains(err.Error(), "Key:") || strings.Contains(err.Error(), "required_without") {
		t.Fatalf("internal validator details leaked: %q", err)
	}
}
