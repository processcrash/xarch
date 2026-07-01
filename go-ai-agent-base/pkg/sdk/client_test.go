package sdk

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestClient_Health(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		assert.Equal(t, "/api/v1/health", r.URL.Path)
		_, _ = w.Write([]byte(`{"status":"UP","agents":["a","b"]}`))
	}))
	defer srv.Close()

	c := New(srv.URL)
	res, err := c.Health(context.Background())
	require.NoError(t, err)
	assert.Equal(t, "UP", res["status"])
}

func TestClient_Send(t *testing.T) {
	var seenAuth string
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		seenAuth = r.Header.Get("Authorization")
		var body map[string]string
		_ = json.NewDecoder(r.Body).Decode(&body)
		assert.Equal(t, "hello", body["content"])
		_, _ = w.Write([]byte(`{
			"message": {"role":"assistant","content":"hi back"},
			"usage": {"prompt_tokens":5,"completion_tokens":3,"total_tokens":8},
			"stop_reason":"stop",
			"duration":"120ms"
		}`))
	}))
	defer srv.Close()

	c := New(srv.URL, WithToken("xyz"))
	resp, err := c.Send(context.Background(), "assistant", "sid-1", "hello")
	require.NoError(t, err)
	assert.Equal(t, "hi back", resp.Message.Content)
	assert.Equal(t, "xyz", seenAuth)
	assert.Equal(t, 8, resp.Usage.TotalTokens)
}

func TestClient_Timeout(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		time.Sleep(200 * time.Millisecond)
		_, _ = w.Write([]byte(`{}`))
	}))
	defer srv.Close()

	c := New(srv.URL, WithTimeout(50*time.Millisecond))
	_, err := c.Send(context.Background(), "a", "b", "c")
	require.Error(t, err)
}