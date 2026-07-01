// Package sdk is a thin Go client for the go-ai-agent-base HTTP API.
// It is the recommended way for downstream services and tests to talk
// to a running agent platform.
//
// Usage:
//
//	client := sdk.New("http://localhost:8080", sdk.WithToken("eyJ..."))
//	resp, err := client.Send(ctx, "assistant", sessionID, "Hello!")
package sdk

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

// Client talks to a running go-ai-agent-base HTTP server.
type Client struct {
	baseURL string
	token   string
	apiKey  string
	http    *http.Client
}

// Option configures the Client.
type Option func(*Client)

// WithToken sets a Bearer JWT (sent as Authorization header).
func WithToken(t string) Option { return func(c *Client) { c.token = t } }

// WithAPIKey sets a static API key (sent as X-API-Key).
func WithAPIKey(k string) Option { return func(c *Client) { c.apiKey = k } }

// WithTimeout overrides the default 60s HTTP timeout.
func WithTimeout(d time.Duration) Option { return func(c *Client) { c.http.Timeout = d } }

// New constructs a Client targeting baseURL.
func New(baseURL string, opts ...Option) *Client {
	c := &Client{
		baseURL: strings.TrimRight(baseURL, "/"),
		http:    &http.Client{Timeout: 60 * time.Second},
	}
	for _, o := range opts {
		o(c)
	}
	return c
}

// Health fetches /api/v1/health.
func (c *Client) Health(ctx context.Context) (map[string]any, error) {
	var out map[string]any
	if err := c.doJSON(ctx, http.MethodGet, "/api/v1/health", nil, &out); err != nil {
		return nil, err
	}
	return out, nil
}

// ListAgents fetches /api/v1/agents.
func (c *Client) ListAgents(ctx context.Context) ([]string, error) {
	var out struct {
		Agents []string `json:"agents"`
	}
	if err := c.doJSON(ctx, http.MethodGet, "/api/v1/agents", nil, &out); err != nil {
		return nil, err
	}
	return out.Agents, nil
}

// CreateSession opens a new session for the named agent.
func (c *Client) CreateSession(ctx context.Context, agentName, userID string) (string, error) {
	body, _ := json.Marshal(map[string]string{"user_id": userID})
	var out struct {
		SessionID string `json:"session_id"`
	}
	if err := c.doJSON(ctx, http.MethodPost,
		fmt.Sprintf("/api/v1/agents/%s/sessions", agentName),
		bytes.NewReader(body), &out); err != nil {
		return "", err
	}
	return out.SessionID, nil
}

// Send posts a message and waits for the final assistant reply.
func (c *Client) Send(ctx context.Context, agentName, sessionID, content string) (*Response, error) {
	body, _ := json.Marshal(map[string]string{"content": content})
	var out Response
	if err := c.doJSON(ctx, http.MethodPost,
		fmt.Sprintf("/api/v1/agents/%s/sessions/%s/messages", agentName, sessionID),
		bytes.NewReader(body), &out); err != nil {
		return nil, err
	}
	return &out, nil
}

// Response is the JSON shape returned by POST /sessions/{sid}/messages.
type Response struct {
	Message struct {
		Role       string `json:"role"`
		Content    string `json:"content"`
		ToolCalls  []struct {
			ID        string          `json:"id"`
			Name      string          `json:"name"`
			Arguments json.RawMessage `json:"arguments"`
		} `json:"tool_calls"`
	} `json:"message"`
	Usage struct {
		PromptTokens     int `json:"prompt_tokens"`
		CompletionTokens int `json:"completion_tokens"`
		TotalTokens      int `json:"total_tokens"`
	} `json:"usage"`
	StopReason string `json:"stop_reason"`
	Duration   string `json:"duration"`
}

func (c *Client) doJSON(ctx context.Context, method, path string, body io.Reader, out any) error {
	req, err := http.NewRequestWithContext(ctx, method, c.baseURL+path, body)
	if err != nil {
		return fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Accept", "application/json")
	if c.token != "" {
		req.Header.Set("Authorization", "Bearer "+c.token)
	}
	if c.apiKey != "" {
		req.Header.Set("X-API-Key", c.apiKey)
	}
	resp, err := c.http.Do(req)
	if err != nil {
		return fmt.Errorf("http: %w", err)
	}
	defer resp.Body.Close()
	raw, _ := io.ReadAll(resp.Body)
	if resp.StatusCode >= 400 {
		return fmt.Errorf("status %d: %s", resp.StatusCode, string(raw))
	}
	if out != nil {
		if err := json.Unmarshal(raw, out); err != nil {
			return fmt.Errorf("decode: %w", err)
		}
	}
	return nil
}