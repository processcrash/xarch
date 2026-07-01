// Package auth provides JWT verification and a Gin middleware.
package auth

import (
	"crypto/rsa"
	"errors"
	"fmt"
	"os"
	"strings"

	"github.com/golang-jwt/jwt/v5"
)

// Claims is the standard JWT claims plus a few xarch-specific fields.
type Claims struct {
	jwt.RegisteredClaims
	Scopes []string `json:"scopes,omitempty"`
	Name   string   `json:"name,omitempty"`
}

// Verifier validates incoming JWTs.
type Verifier struct {
	algo      string
	hmacKey   []byte
	publicKey *rsa.PublicKey
	issuer    string
	audience  string
}

// NewVerifier builds a Verifier from config.
func NewVerifier(algo, secret, pubKeyPath, issuer, audience string) (*Verifier, error) {
	v := &Verifier{algo: algo, issuer: issuer, audience: audience}
	switch strings.ToUpper(algo) {
	case "HS256":
		if secret == "" {
			return nil, errors.New("HS256 requires secret")
		}
		v.hmacKey = []byte(secret)
	case "RS256":
		if pubKeyPath == "" {
			return nil, errors.New("RS256 requires public_key_path")
		}
		pem, err := os.ReadFile(pubKeyPath)
		if err != nil {
			return nil, fmt.Errorf("read public key: %w", err)
		}
		pub, err := jwt.ParseRSAPublicKeyFromPEM(pem)
		if err != nil {
			return nil, fmt.Errorf("parse rsa public key: %w", err)
		}
		v.publicKey = pub
	default:
		return nil, fmt.Errorf("unsupported algorithm: %s", algo)
	}
	return v, nil
}

// Verify parses and validates a token string.
func (v *Verifier) Verify(tokenString string) (*Claims, error) {
	c := &Claims{}
	parser := jwt.NewParser(
		jwt.WithIssuer(v.issuer),
		jwt.WithAudience(v.audience),
		jwt.WithExpirationRequired(),
	)
	_, err := parser.ParseWithClaims(tokenString, c, func(t *jwt.Token) (any, error) {
		switch t.Method.Alg() {
		case "HS256":
			return v.hmacKey, nil
		case "RS256":
			return v.publicKey, nil
		default:
			return nil, fmt.Errorf("unexpected signing method: %v", t.Header["alg"])
		}
	})
	if err != nil {
		return nil, err
	}
	return c, nil
}

// Sign is a small helper for tests/dev to mint tokens against the
// configured verifier's key.
func (v *Verifier) Sign(claims Claims) (string, error) {
	method := jwt.GetSigningMethod(strings.ToUpper(v.algo))
	token := jwt.NewWithClaims(method, &claims)
	switch strings.ToUpper(v.algo) {
	case "HS256":
		return token.SignedString(v.hmacKey)
	case "RS256":
		return "", errors.New("Sign() not supported for RS256 in this scaffold (use a real keypair)")
	}
	_ = method
	return "", nil
}

// HasScope reports whether the claims include the given scope.
func (c *Claims) HasScope(s string) bool {
	for _, x := range c.Scopes {
		if x == s || x == "*" {
			return true
		}
	}
	return false
}

// ExtractBearerToken returns the token portion from "Authorization: Bearer ..."
// or "" if the header is missing/malformed.
func ExtractBearerToken(authHeader string) string {
	const prefix = "Bearer "
	if len(authHeader) <= len(prefix) || !strings.EqualFold(authHeader[:len(prefix)], prefix) {
		return ""
	}
	return strings.TrimSpace(authHeader[len(prefix):])
}