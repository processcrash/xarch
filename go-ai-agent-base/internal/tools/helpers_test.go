package tools

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
)

// mustJSON returns marshalled bytes for an arbitrary value or panics.
func mustJSON(v any) []byte {
	b, err := json.Marshal(v)
	if err != nil {
		panic(err)
	}
	return b
}

// Helper used by tests in this package. Silences the import of
// httptest in calculator_test.go (which only needs the helpers).
var _ = httptest.NewServer
var _ = http.StatusOK