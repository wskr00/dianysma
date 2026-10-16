package service

import (
	"strings"
	"testing"
	"unicode/utf8"

	"github.com/stretchr/testify/require"
)

func TestExcerptPreservesMultilingualContentAndCharacterLimit(t *testing.T) {
	short := "Implementei autenticação segura em Go e PostgreSQL — entrega validada ✅"
	require.Equal(t, short, excerpt(short))
	text := strings.Repeat("Entrega comprovada 🔒. ", 40)
	got := excerpt(text)
	require.True(t, utf8.ValidString(got))
	require.LessOrEqual(t, utf8.RuneCountInString(got), 500)
	require.True(t, strings.HasSuffix(got, "…"))
	require.True(t, strings.HasPrefix(text, strings.TrimSuffix(got, "…")))
}
