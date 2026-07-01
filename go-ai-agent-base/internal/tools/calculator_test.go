package tools

import (
	"context"
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestCalculator(t *testing.T) {
	c := &Calculator{}
	cases := []struct {
		expr string
		want string
	}{
		{"1+2", "3"},
		{"10-4", "6"},
		{"2*3", "6"},
		{"8/4", "2"},
		{"2+3*4", "14"},
		{"(2+3)*4", "20"},
		{"-5+10", "5"},
		{"10/(2+3)", "2"},
	}
	for _, tc := range cases {
		args, _ := json.Marshal(map[string]string{"expression": tc.expr})
		out, err := c.Execute(context.Background(), args)
		require.NoError(t, err, tc.expr)
		assert.Equal(t, tc.want, out, tc.expr)
	}
}

func TestCurrentTime(t *testing.T) {
	c := &CurrentTime{}
	out, err := c.Execute(context.Background(), json.RawMessage(`{"location":"UTC"}`))
	require.NoError(t, err)
	assert.NotEmpty(t, out)
	assert.Contains(t, out, "T") // RFC3339
}

func TestHTTPFetch(t *testing.T) {
	// Stub a small in-memory HTTP server so we don't depend on the network.
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, _ = w.Write([]byte("hello from upstream"))
	}))
	defer srv.Close()

	h := &HTTPFetch{MaxBytes: 1024, Timeout: 5 * 1e9}
	out, err := h.Execute(context.Background(), mustJSON(map[string]string{"url": srv.URL}))
	require.NoError(t, err)
	assert.Equal(t, "hello from upstream", out)
}