package config

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestLoadEnvironmentOverridesSecretAndLocalFile(t *testing.T) {
	isolateConfigEnvironment(t)
	t.Chdir(t.TempDir())
	require.NoError(t, os.WriteFile(".env", []byte("DATABASE_NAME=file_db\nDATABASE_SSL_MODE=disable\n"), 0600))
	endpoint := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		var input map[string]string
		if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
			t.Error(err)
			http.Error(w, "invalid request", 400)
			return
		}
		if input["SecretId"] != "local-test/runtime" {
			t.Errorf("unexpected secret ID: %s", input["SecretId"])
			http.Error(w, "invalid secret ID", 400)
			return
		}
		w.Header().Set("Content-Type", "application/x-amz-json-1.1")
		//nolint:gosec // These clearly synthetic fixture values are used to verify secret loading and precedence.
		if err := json.NewEncoder(w).Encode(map[string]string{"SecretString": `{"DATABASE_HOST":"secret-db.local","DATABASE_USER":"secret_user","DATABASE_PASSWORD":"secret_password","DATABASE_NAME":"secret_db","GEMINI_API_KEY":"secret-gemini"}`}); err != nil {
			t.Errorf("encode secret response: %v", err)
		}
	}))
	defer endpoint.Close()
	for k, v := range map[string]string{"AWS_SECRET_ID": "local-test/runtime", "AWS_REGION": "us-east-1", "AWS_ACCESS_KEY_ID": "dummy", "AWS_SECRET_ACCESS_KEY": "dummy", "AWS_EC2_METADATA_DISABLED": "true", "AWS_ENDPOINT_URL_SECRETS_MANAGER": endpoint.URL, "DATABASE_USER": "env_user"} {
		t.Setenv(k, v)
	}
	c, err := Load(context.Background())
	require.NoError(t, err)
	require.Equal(t, "env_user", c.DatabaseUser)
	require.Equal(t, "secret-db.local", c.DatabaseHost)
	require.Equal(t, "secret_password", c.DatabasePassword)
	require.Equal(t, "file_db", c.DatabaseName)
	require.Equal(t, "secret-gemini", c.GeminiAPIKey)
	require.Equal(t, "disable", c.DatabaseSSLMode)
}

func TestConfigurationErrorsDoNotExposeValues(t *testing.T) {
	isolateConfigEnvironment(t)
	t.Chdir(t.TempDir())
	t.Setenv("AWS_SECRET_ID", "")
	t.Setenv("DATABASE_PORT", "sensitive-invalid-value")
	_, err := Load(t.Context())
	require.Error(t, err)
	require.NotContains(t, err.Error(), "sensitive-invalid-value")
}

func TestDSNPreservesSpecialCredentialsAndUsesVerifiedTLS(t *testing.T) {
	isolateConfigEnvironment(t)
	t.Chdir(t.TempDir())
	t.Setenv("AWS_SECRET_ID", "")
	c, err := Load(t.Context())
	require.NoError(t, err)
	c.DatabaseUser, c.DatabasePassword = "user+name@example", `p@ss:#?/=&\`
	c.DatabaseMigrationHost = "direct-db.example"
	u, err := url.Parse(c.DSN(false))
	require.NoError(t, err)
	password, set := u.User.Password()
	require.True(t, set)
	require.Equal(t, c.DatabasePassword, password)
	require.Equal(t, c.DatabaseUser, u.User.Username())
	require.Equal(t, "verify-full", u.Query().Get("sslmode"))
	require.Contains(t, c.DSN(true), "direct-db.example")
	for _, endpoint := range []string{"/", "mcp", "/mcp?x=1", "/mcp/{id}"} {
		c.Endpoint = endpoint
		require.Error(t, c.Validate(), endpoint)
	}
	_, err = os.Stat(filepath.Join(".", ".env"))
	require.True(t, os.IsNotExist(err))
	require.False(t, strings.Contains(c.DSN(false), "sslmode=disable"))
}

func isolateConfigEnvironment(t *testing.T) {
	t.Helper()
	for _, entry := range os.Environ() {
		key, value, _ := strings.Cut(entry, "=")
		if !strings.HasPrefix(key, "AWS_") && !strings.HasPrefix(key, "DATABASE_") &&
			!strings.HasPrefix(key, "GEMINI_") && !strings.HasPrefix(key, "MCP_") &&
			!strings.HasPrefix(key, "SERVER_") && !strings.HasPrefix(key, "KNOWLEDGE_") {
			continue
		}
		require.NoError(t, os.Unsetenv(key))
		t.Cleanup(func() { _ = os.Setenv(key, value) })
	}
}
