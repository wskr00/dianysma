package mcpaudit

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"runtime/debug"
	"sync"

	"github.com/felixge/httpsnoop"
	"github.com/google/uuid"
	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type stateKey struct{}

type requestState struct {
	mu        sync.Mutex
	id        string
	tool      string
	failed    bool
	errorType string
	cause     error
}

// RecordFailure attaches an unexpected cause to the single completion log.
func RecordFailure(ctx context.Context, cause error) {
	recordFailure(ctx, cause, cause)
}

// RecordFailureType records a failure's type without retaining its message or cause.
func RecordFailureType(ctx context.Context, failure error) {
	recordFailure(ctx, failure, nil)
}

func recordFailure(ctx context.Context, failure, cause error) {
	if state, ok := ctx.Value(stateKey{}).(*requestState); ok {
		state.mu.Lock()
		defer state.mu.Unlock()
		state.failed = true
		if state.errorType == "" && failure != nil {
			state.errorType = fmt.Sprintf("%T", failure)
		}
		if cause != nil {
			state.cause = cause
		}
	}
}

func Middleware(logger *slog.Logger) mcp.Middleware {
	return func(next mcp.MethodHandler) mcp.MethodHandler {
		return func(ctx context.Context, method string, req mcp.Request) (result mcp.Result, err error) {
			if method != "tools/call" {
				return next(ctx, method, req)
			}
			if params, ok := req.GetParams().(*mcp.CallToolParamsRaw); ok {
				requestID := ""
				if state, ok := ctx.Value(stateKey{}).(*requestState); ok {
					state.mu.Lock()
					state.tool = params.Name
					requestID = state.id
					state.mu.Unlock()
				}
				logger.InfoContext(ctx, "tool: "+params.Name, "http.request.id", requestID)
			}
			defer func() {
				if value := recover(); value != nil {
					RecordFailure(ctx, fmt.Errorf("panic: %v\n%s", value, debug.Stack()))
					result = &mcp.CallToolResult{IsError: true, Content: []mcp.Content{&mcp.TextContent{Text: "Não foi possível concluir a operação devido a uma falha interna."}}}
					err = nil
				}
			}()
			result, err = next(ctx, method, req)
			if err != nil {
				RecordFailureType(ctx, err)
			} else if tool, ok := result.(*mcp.CallToolResult); ok && tool.IsError {
				RecordFailure(ctx, nil)
			}
			return result, err
		}
	}
}

func HTTP(logger *slog.Logger, next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		id := uuid.NewString()
		state := &requestState{id: id}
		r = r.WithContext(context.WithValue(r.Context(), stateKey{}, state))
		w.Header().Set("X-Request-Id", id)
		metrics := httpsnoop.CaptureMetrics(next, w, r)
		state.mu.Lock()
		tool, failed, errorType, cause := state.tool, state.failed, state.errorType, state.cause
		state.mu.Unlock()
		if tool == "" {
			if metrics.Code >= 400 {
				logger.WarnContext(r.Context(), "Falha na requisição MCP HTTP", "status", metrics.Code, "duration_ms", metrics.Duration.Milliseconds(), "http.request.id", id)
			}
			return
		}
		outcome := "success"
		if failed || metrics.Code >= 400 {
			outcome = "failure"
		}
		level := slog.LevelInfo
		if cause != nil || metrics.Code >= 500 {
			level = slog.LevelError
		}
		attrs := []slog.Attr{
			slog.Int("status", metrics.Code),
			slog.Int64("duration_ms", metrics.Duration.Milliseconds()),
			slog.String("event.outcome", outcome),
			slog.String("http.request.id", id),
		}
		if errorType != "" {
			attrs = append(attrs, slog.String("error.type", errorType))
		}
		if cause != nil {
			attrs = append(attrs, slog.String("error", cause.Error()))
		}
		logger.LogAttrs(r.Context(), level, "tool: "+tool, attrs...)
	})
}
