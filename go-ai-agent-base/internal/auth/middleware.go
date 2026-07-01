package auth

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// Context keys.
const (
	CtxClaims  = "xarch.auth.claims"
	CtxSubject = "xarch.auth.subject"
)

// Middleware returns a Gin handler that verifies the Bearer JWT in the
// Authorization header and stores claims in the request context.
// Falls back to static API keys (config.AuthConfig.APIKeys) when JWT
// is not configured or when the API-Key header is supplied.
func Middleware(v *Verifier, staticKeys []APIKey) gin.HandlerFunc {
	keyMap := map[string]APIKey{}
	for _, k := range staticKeys {
		if k.Key != "" {
			keyMap[k.Key] = k
		}
	}
	return func(c *gin.Context) {
		// 1. Try static API key (X-API-Key header)
		if apiKey := c.GetHeader("X-API-Key"); apiKey != "" {
			if k, ok := keyMap[apiKey]; ok {
				c.Set(CtxClaims, &Claims{
					RegisteredClaims: struct{ Subject string `json:"sub,omitempty"` }{}, // placeholder
				})
				c.Set(CtxSubject, k.Name)
				c.Next()
				return
			}
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid API key"})
			return
		}

		// 2. JWT
		if v == nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "authentication not configured"})
			return
		}
		hdr := c.GetHeader("Authorization")
		token := ExtractBearerToken(hdr)
		if token == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "missing bearer token"})
			return
		}
		claims, err := v.Verify(token)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid token: " + err.Error()})
			return
		}
		c.Set(CtxClaims, claims)
		c.Set(CtxSubject, claims.Subject)
		c.Next()
	}
}

// APIKey describes a static API key for the middleware.
type APIKey struct {
	Key    string
	Name   string
	Scopes []string
}

// FromConfig is a helper that converts []string → APIKey when given
// "key:name:scope1,scope2" style entries. Not used in the scaffold yet
// but kept for future expansion.
func FromConfig(entries []string) []APIKey {
	out := make([]APIKey, 0, len(entries))
	for _, e := range entries {
		parts := strings.SplitN(e, ":", 3)
		ak := APIKey{Key: parts[0], Name: parts[0]}
		if len(parts) >= 2 {
			ak.Name = parts[1]
		}
		if len(parts) == 3 {
			ak.Scopes = strings.Split(parts[2], ",")
		}
		out = append(out, ak)
	}
	return out
}