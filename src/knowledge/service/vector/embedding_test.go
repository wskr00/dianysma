package vector

import (
	"context"
	"io"
	"log"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/stretchr/testify/require"
	"google.golang.org/genai"
)

func TestGoogleSDKRejectsUntrustedTLS(t *testing.T) {
	server := httptest.NewUnstartedServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if _, err := io.WriteString(w, `{"embeddings":[{"values":[1,0]}]}`); err != nil {
			t.Errorf("write embedding fixture response: %v", err)
		}
	}))
	server.Config.ErrorLog = log.New(io.Discard, "", 0)
	server.StartTLS()
	defer server.Close()
	client, err := genai.NewClient(t.Context(), &genai.ClientConfig{Backend: genai.BackendGeminiAPI, APIKey: "local", HTTPClient: &http.Client{Timeout: time.Second}, HTTPOptions: genai.HTTPOptions{BaseURL: server.URL}})
	require.NoError(t, err)
	_, err = NewGoogleEmbedderWithClient(client, "gemini-embedding-2", 2).Embed(t.Context(), "texto")
	require.Error(t, err)
	require.Contains(t, err.Error(), "certificate")
}

func TestGoogleSDKRequestHonorsContextDeadline(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		select {
		case <-r.Context().Done():
		case <-time.After(time.Second):
		}
	}))
	defer server.Close()
	client, err := genai.NewClient(t.Context(), &genai.ClientConfig{Backend: genai.BackendGeminiAPI, APIKey: "local", HTTPClient: server.Client(), HTTPOptions: genai.HTTPOptions{BaseURL: server.URL}})
	require.NoError(t, err)
	ctx, cancel := context.WithTimeout(t.Context(), 30*time.Millisecond)
	defer cancel()
	_, err = NewGoogleEmbedderWithClient(client, "gemini-embedding-2", 2).Embed(ctx, "texto")
	require.ErrorIs(t, err, context.DeadlineExceeded)
}
