package exception

import "errors"

// Error represents an expected application error that can be shown to the MCP client.
type Error struct{ Message string }

func (e *Error) Error() string      { return e.Message }
func Invalid(message string) error  { return &Error{Message: message} }
func NotFound(message string) error { return &Error{Message: message} }

func PublicMessage(err error) (string, bool) {
	if expected, ok := errors.AsType[*Error](err); ok {
		return expected.Message, true
	}
	return "Não foi possível concluir a operação devido a uma falha interna. Tente novamente mais tarde.", false
}
