// Package tools provides built-in tools (time, calculator, http fetch)
// plus a registry that loads MCP-bridged tools at startup.
package tools

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/xarch/go-ai-agent-base/internal/agent"
)

// CurrentTime returns the current time in RFC3339 format.
type CurrentTime struct{}

// Name implements agent.Tool.
func (c *CurrentTime) Name() string { return "current_time" }

// Description implements agent.Tool.
func (c *CurrentTime) Description() string {
	return "Returns the current server time in RFC3339 format with optional timezone offset."
}

// ParametersSchema implements agent.Tool.
func (c *CurrentTime) ParametersSchema() json.RawMessage {
	return json.RawMessage(`{
		"type": "object",
		"properties": {
			"location": {"type": "string", "description": "IANA timezone (e.g. 'America/New_York'); empty = UTC"}
		}
	}`)
}

// Execute implements agent.Tool.
func (c *CurrentTime) Execute(_ context.Context, args json.RawMessage) (string, error) {
	var p struct {
		Location string `json:"location"`
	}
	_ = json.Unmarshal(args, &p)
	loc := time.UTC
	if p.Location != "" {
		l, err := time.LoadLocation(p.Location)
		if err != nil {
			return "", fmt.Errorf("invalid timezone: %w", err)
		}
		loc = l
	}
	return time.Now().In(loc).Format(time.RFC3339), nil
}

// Calculator evaluates a simple arithmetic expression. Uses a tiny
// recursive-descent parser — supports + - * / and parentheses.
type Calculator struct{}

// Name implements agent.Tool.
func (c *Calculator) Name() string { return "calculator" }

// Description implements agent.Tool.
func (c *Calculator) Description() string {
	return "Evaluate a basic arithmetic expression (+, -, *, /, parentheses)."
}

// ParametersSchema implements agent.Tool.
func (c *Calculator) ParametersSchema() json.RawMessage {
	return json.RawMessage(`{
		"type": "object",
		"properties": {
			"expression": {"type": "string", "description": "Arithmetic expression to evaluate"}
		},
		"required": ["expression"]
	}`)
}

// Execute implements agent.Tool.
func (c *Calculator) Execute(_ context.Context, args json.RawMessage) (string, error) {
	var p struct {
		Expression string `json:"expression"`
	}
	if err := json.Unmarshal(args, &p); err != nil {
		return "", fmt.Errorf("parse args: %w", err)
	}
	v, err := evalExpr(p.Expression)
	if err != nil {
		return "", err
	}
	return strconv.FormatFloat(v, 'f', -1, 64), nil
}

// HTTPFetch fetches a URL and returns the body (truncated). Use for
// reading public docs / pages.
type HTTPFetch struct {
	MaxBytes int64
	Timeout  time.Duration
}

// Name implements agent.Tool.
func (h *HTTPFetch) Name() string { return "http_fetch" }

// Description implements agent.Tool.
func (h *HTTPFetch) Description() string {
	return "Fetch a public URL and return the response body (text or HTML)."
}

// ParametersSchema implements agent.Tool.
func (h *HTTPFetch) ParametersSchema() json.RawMessage {
	return json.RawMessage(`{
		"type": "object",
		"properties": {
			"url": {"type": "string", "description": "URL to fetch"},
			"maxBytes": {"type": "integer", "default": 100000, "description": "Max bytes to read"}
		},
		"required": ["url"]
	}`)
}

// Execute implements agent.Tool.
func (h *HTTPFetch) Execute(ctx context.Context, args json.RawMessage) (string, error) {
	var p struct {
		URL      string `json:"url"`
		MaxBytes int64  `json:"maxBytes"`
	}
	if err := json.Unmarshal(args, &p); err != nil {
		return "", fmt.Errorf("parse args: %w", err)
	}
	if p.URL == "" {
		return "", fmt.Errorf("url is required")
	}
	maxBytes := p.MaxBytes
	if maxBytes == 0 {
		maxBytes = h.MaxBytes
		if maxBytes == 0 {
			maxBytes = 100_000
		}
	}
	timeout := h.Timeout
	if timeout == 0 {
		timeout = 10 * time.Second
	}
	client := &http.Client{Timeout: timeout}
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, p.URL, nil)
	if err != nil {
		return "", fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("User-Agent", "go-ai-agent-base/0.1")
	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("fetch: %w", err)
	}
	defer resp.Body.Close()
	if resp.StatusCode >= 400 {
		return "", fmt.Errorf("http %d from %s", resp.StatusCode, p.URL)
	}
	limited := io.LimitReader(resp.Body, maxBytes)
	body, err := io.ReadAll(limited)
	if err != nil {
		return "", fmt.Errorf("read body: %w", err)
	}
	return string(body), nil
}

// Builtins returns the canonical built-in tool set.
func Builtins() []agent.Tool {
	return []agent.Tool{
		&CurrentTime{},
		&Calculator{},
		&HTTPFetch{},
	}
}

// ---- tiny expression evaluator ------------------------------------------

func evalExpr(src string) (float64, error) {
	p := &parser{src: strings.ReplaceAll(src, " ", "")}
	v, err := p.parseExpr()
	if err != nil {
		return 0, err
	}
	if p.pos != len(p.src) {
		return 0, fmt.Errorf("unexpected character at %d: %q", p.pos, string(p.src[p.pos]))
	}
	return v, nil
}

type parser struct {
	src string
	pos int
}

func (p *parser) peek() byte {
	if p.pos >= len(p.src) {
		return 0
	}
	return p.src[p.pos]
}

func (p *parser) consume() byte {
	c := p.peek()
	p.pos++
	return c
}

func (p *parser) parseExpr() (float64, error) {
	return p.parseAddSub()
}

func (p *parser) parseAddSub() (float64, error) {
	left, err := p.parseMulDiv()
	if err != nil {
		return 0, err
	}
	for {
		switch p.peek() {
		case '+':
			p.consume()
			right, err := p.parseMulDiv()
			if err != nil {
				return 0, err
			}
			left += right
		case '-':
			p.consume()
			right, err := p.parseMulDiv()
			if err != nil {
				return 0, err
			}
			left -= right
		default:
			return left, nil
		}
	}
}

func (p *parser) parseMulDiv() (float64, error) {
	left, err := p.parseUnary()
	if err != nil {
		return 0, err
	}
	for {
		switch p.peek() {
		case '*':
			p.consume()
			right, err := p.parseUnary()
			if err != nil {
				return 0, err
			}
			left *= right
		case '/':
			p.consume()
			right, err := p.parseUnary()
			if err != nil {
				return 0, err
			}
			if right == 0 {
				return 0, fmt.Errorf("division by zero")
			}
			left /= right
		default:
			return left, nil
		}
	}
}

func (p *parser) parseUnary() (float64, error) {
	if p.peek() == '-' {
		p.consume()
		v, err := p.parsePrimary()
		if err != nil {
			return 0, err
		}
		return -v, nil
	}
	if p.peek() == '+' {
		p.consume()
	}
	return p.parsePrimary()
}

func (p *parser) parsePrimary() (float64, error) {
	c := p.peek()
	if c == '(' {
		p.consume()
		v, err := p.parseExpr()
		if err != nil {
			return 0, err
		}
		if p.peek() != ')' {
			return 0, fmt.Errorf("expected ')' at %d", p.pos)
		}
		p.consume()
		return v, nil
	}
	if (c >= '0' && c <= '9') || c == '.' {
		start := p.pos
		for {
			c := p.peek()
			if (c >= '0' && c <= '9') || c == '.' {
				p.consume()
			} else {
				break
			}
		}
		v, err := strconv.ParseFloat(p.src[start:p.pos], 64)
		if err != nil {
			return 0, fmt.Errorf("parse number %q: %w", p.src[start:p.pos], err)
		}
		return v, nil
	}
	return 0, fmt.Errorf("unexpected character %q at %d", string(c), p.pos)
}