package mcpcontroller

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"reflect"
	"strings"
	"time"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"
	mcpaudit "github.com/wskr00/dianysma/src/core/audit/mcp"
	"github.com/wskr00/dianysma/src/knowledge/domain"
	"github.com/wskr00/dianysma/src/knowledge/service"
	"github.com/wskr00/dianysma/src/knowledge/service/exception"
)

func NewServer(services *service.Services, logger *slog.Logger, version string, timeout time.Duration) *mcp.Server {
	server := mcp.NewServer(&mcp.Implementation{Name: "dianysma", Version: version}, &mcp.ServerOptions{Logger: logger})
	server.AddReceivingMiddleware(mcpaudit.Middleware(logger), requestTimeout(timeout), localizeSDKToolErrors())
	registerProfile(server, services)
	registerOrganization(server, services)
	registerExperience(server, services)
	registerProject(server, services)
	registerParticipation(server, services)
	registerClaim(server, services)
	registerSkill(server, services)
	registerEducation(server, services)
	registerLanguage(server, services)
	registerKnowledge(server, services)
	return server
}

func NewHandler(server *mcp.Server, logger *slog.Logger) http.Handler {
	handler := mcp.NewStreamableHTTPHandler(func(*http.Request) *mcp.Server { return server }, &mcp.StreamableHTTPOptions{
		Stateless: true, JSONResponse: true, Logger: logger, MaxRequestBodyBytes: 1 << 20, PropagateRequestCancellation: true,
	})
	return mcpaudit.HTTP(logger, handler)
}

func addTool[In, Out any](server *mcp.Server, tool *mcp.Tool, action func(context.Context, In) (Out, error)) {
	tool.InputSchema, tool.OutputSchema = schemaFor[In](), schemaFor[Out]()
	mcp.AddTool(server, tool, func(ctx context.Context, _ *mcp.CallToolRequest, input In) (*mcp.CallToolResult, Out, error) {
		output, err := action(ctx, input)
		if err == nil {
			return nil, output, nil
		}
		message, expected := exception.PublicMessage(err)
		if errors.Is(err, context.Canceled) {
			message, expected = "A operação foi cancelada.", true
		}
		if errors.Is(err, context.DeadlineExceeded) {
			message, expected = "A operação excedeu o tempo limite. Tente novamente.", true
		}
		if expected {
			mcpaudit.RecordFailureType(ctx, err)
		} else {
			mcpaudit.RecordFailure(ctx, err)
		}
		var zero Out
		return nil, zero, &localizedToolError{message: message}
	})
}

// localizedToolError marks errors already translated for the MCP client.
type localizedToolError struct {
	message string
}

func (e *localizedToolError) Error() string { return e.message }

func localizeSDKToolErrors() mcp.Middleware {
	return func(next mcp.MethodHandler) mcp.MethodHandler {
		return func(ctx context.Context, method string, req mcp.Request) (mcp.Result, error) {
			result, err := next(ctx, method, req)
			if err != nil || method != "tools/call" {
				return result, err
			}

			toolResult, ok := result.(*mcp.CallToolResult)
			if !ok || !toolResult.IsError {
				return result, nil
			}

			sdkError := toolResult.GetError()
			if sdkError == nil {
				return result, nil
			}
			if _, ok := errors.AsType[*localizedToolError](sdkError); ok {
				return result, nil
			}

			mcpaudit.RecordFailureType(ctx, sdkError)
			toolResult.Content = []mcp.Content{&mcp.TextContent{
				Text: "Os argumentos da ferramenta são inválidos. Confira os campos obrigatórios, os tipos e os valores aceitos na descrição e tente novamente.",
			}}
			return result, nil
		}
	}
}

func schemaFor[T any]() *jsonschema.Schema {
	schema, err := jsonschema.For[T](&jsonschema.ForOptions{TypeSchemas: map[reflect.Type]*jsonschema.Schema{
		reflect.TypeFor[domain.EducationType]():       {Type: "string", Enum: []any{domain.Degree, domain.Course, domain.Certification}},
		reflect.TypeFor[domain.LanguageProficiency](): {Type: "string", Enum: []any{domain.Basic, domain.Intermediate, domain.Advanced, domain.Fluent, domain.Native}},
		reflect.TypeFor[time.Time]():                  {Type: "string", Format: "date-time"},
	}})
	if err != nil {
		panic(err)
	} // A schema/type mismatch is a programming error, caught during startup and tests.
	applySchemaFormats(schema, reflect.TypeFor[T]())
	return schema
}

// applySchemaFormats restores JSON Schema formats from Go types and field
// metadata that jsonschema-go does not infer by itself. Command DTOs reuse
// their validation tags; result DTOs declare formats with `format` tags.
func applySchemaFormats(schema *jsonschema.Schema, typ reflect.Type) {
	for typ.Kind() == reflect.Pointer {
		typ = typ.Elem()
	}

	switch typ.Kind() {
	case reflect.Struct:
		for field := range typ.Fields() {
			if !field.IsExported() {
				continue
			}

			name, _, _ := strings.Cut(field.Tag.Get("json"), ",")
			if name == "-" {
				continue
			}
			if name == "" {
				name = field.Name
			}

			property := schema.Properties[name]
			if property == nil && field.Anonymous {
				// Anonymous fields may be flattened by the JSON encoder/schema.
				applySchemaFormats(schema, field.Type)
				continue
			}
			if property == nil {
				continue
			}

			if format, each := fieldFormat(field); format != "" {
				if each && property.Items != nil {
					property = property.Items
				}
				property.Format = format
				continue
			}
			applySchemaFormats(property, field.Type)
		}
	case reflect.Slice, reflect.Array:
		if schema.Items != nil {
			applySchemaFormats(schema.Items, typ.Elem())
		}
	}
}

func fieldFormat(field reflect.StructField) (format string, each bool) {
	if format := field.Tag.Get("format"); format != "" {
		return format, false
	}

	for rule := range strings.SplitSeq(field.Tag.Get("validate"), ",") {
		switch rule {
		case "dive":
			each = true
		case "uuid":
			return "uuid", each
		case "datetime=2006-01-02":
			return "date", each
		}
	}
	return "", false
}

func requestTimeout(timeout time.Duration) mcp.Middleware {
	return func(next mcp.MethodHandler) mcp.MethodHandler {
		return func(ctx context.Context, method string, req mcp.Request) (mcp.Result, error) {
			ctx, cancel := context.WithTimeout(ctx, timeout)
			defer cancel()
			return next(ctx, method, req)
		}
	}
}
