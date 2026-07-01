// Package llm provides LLM providers (Gemini, OpenAI, Anthropic, Ollama)
// behind the agent.LLM interface.
//
// All providers share the same HTTP-client + retry plumbing. Each
// provider implements a GenerateRequest → wire-format → wire-format →
// Response mapping.
package llm

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/config"
)

// Gemini implements agent.LLM against Google's Generative Language API.
//
// API reference:
// https://ai.google.dev/api/generate-content
type Gemini struct {
	cfg     config.ProviderConfig
	apiKey  string
	client  *http.Client
}

// NewGemini constructs a Gemini provider from configuration.
func NewGemini(cfg config.ProviderConfig) (*Gemini, error) {
	if cfg.APIKey == "" {
		return nil, fmt.Errorf("gemini.api_key is required")
	}
	timeout := cfg.Timeout
	if timeout == 0 {
		timeout = 60 * time.Second
	}
	return &Gemini{
		cfg:    cfg,
		apiKey: cfg.APIKey,
		client: &http.Client{Timeout: timeout},
	}, nil
}

// Name implements agent.LLM.
func (g *Gemini) Name() string { return "gemini" }

// Generate implements agent.LLM.
func (g *Gemini) Generate(ctx context.Context, req agent.GenerateRequest) (*agent.Response, error) {
	body := g.toWireFormat(req)
	raw, err := g.doRequest(ctx, req.Model, body)
	if err != nil {
		return nil, err
	}
	return g.fromWireFormat(raw)
}

// Stream implements agent.LLM by emitting a single message event from a
// non-streaming call. For real streaming, swap in SSE parsing.
func (g *Gemini) Stream(ctx context.Context, req agent.GenerateRequest) (<-chan agent.Event, error) {
	out := make(chan agent.Event, 16)
	go func() {
		defer close(out)
		resp, err := g.Generate(ctx, req)
		if err != nil {
			out <- agent.Event{Type: agent.EventError, Error: err.Error(), Time: time.Now()}
			return
		}
		out <- agent.Event{Type: agent.EventMessage, Message: &resp.Message, Time: time.Now()}
		out <- agent.Event{Type: agent.EventDone, Time: time.Now()}
	}()
	return out, nil
}

// toWireFormat maps our canonical request to Gemini's
// generateContent request shape.
func (g *Gemini) toWireFormat(req agent.GenerateRequest) map[string]any {
	contents := make([]map[string]any, 0, len(req.Messages))
	for _, m := range req.Messages {
		parts := []map[string]any{{"text": m.Content}}
		role := "user"
		switch m.Role {
		case agent.RoleUser:
			role = "user"
		case agent.RoleAssistant:
			role = "model"
		case agent.RoleTool:
			// Gemini uses functionResponse part for tool messages.
			parts = []map[string]any{{
				"functionResponse": map[string]any{
					"name":     m.Name,
					"response": map[string]any{"result": m.Content},
				},
			}}
			role = "function"
		}
		contents = append(contents, map[string]any{
			"role":  role,
			"parts": parts,
		})
	}

	out := map[string]any{
		"contents": contents,
		"generationConfig": map[string]any{
			"temperature":    req.Temperature,
			"maxOutputTokens": req.MaxTokens,
		},
	}
	if req.SystemPrompt != "" {
		out["systemInstruction"] = map[string]any{
			"parts": []map[string]any{{"text": req.SystemPrompt}},
		}
	}
	if len(req.Tools) > 0 {
		declarations := make([]map[string]any, 0, len(req.Tools))
		for _, t := range req.Tools {
			declarations = append(declarations, map[string]any{
				"name":        t.Name,
				"description": t.Description,
				"parameters":  json.RawMessage(t.Parameters),
			})
		}
		out["tools"] = []map[string]any{{
			"functionDeclarations": declarations,
		}}
	}
	return out
}

// fromWireFormat parses a Gemini response into the canonical agent.Response.
func (g *Gemini) fromWireFormat(raw []byte) (*agent.Response, error) {
	var wire struct {
		Candidates []struct {
			Content struct {
				Role  string `json:"role"`
				Parts []struct {
					Text         string                 `json:"text,omitempty"`
					FunctionCall map[string]any         `json:"functionCall,omitempty"`
				} `json:"parts"`
			} `json:"content"`
			FinishReason string `json:"finishReason"`
		} `json:"candidates"`
		UsageMetadata struct {
			PromptTokenCount     int `json:"promptTokenCount"`
			CandidatesTokenCount int `json:"candidatesTokenCount"`
			TotalTokenCount      int `json:"totalTokenCount"`
		} `json:"usageMetadata"`
	}
	if err := json.Unmarshal(raw, &wire); err != nil {
		return nil, fmt.Errorf("decode gemini response: %w", err)
	}
	resp := &agent.Response{
		StopReason: normalizeStopReason(wire.Candidates, "stop"),
		Usage: agent.Usage{
			PromptTokens:     wire.UsageMetadata.PromptTokenCount,
			CompletionTokens: wire.UsageMetadata.CandidatesTokenCount,
			TotalTokens:      wire.UsageMetadata.TotalTokenCount,
		},
	}
	if len(wire.Candidates) == 0 {
		return resp, nil
	}
	c := wire.Candidates[0]
	msg := agent.Message{Role: agent.RoleAssistant, Timestamp: time.Now()}
	var textBuf bytes.Buffer
	for _, p := range c.Content.Parts {
		if p.Text != "" {
			textBuf.WriteString(p.Text)
		}
		if p.FunctionCall != nil {
			args, _ := json.Marshal(p.FunctionCall["args"])
			tcID, _ := p.FunctionCall["id"].(string)
			if tcID == "" {
				tcID = fmt.Sprintf("call_%d", len(msg.ToolCalls))
			}
			name, _ := p.FunctionCall["name"].(string)
			msg.ToolCalls = append(msg.ToolCalls, agent.ToolCall{
				ID:        tcID,
				Name:      name,
				Arguments: args,
			})
		}
	}
	msg.Content = textBuf.String()
	resp.Message = msg
	if c.FinishReason == "MAX_TOKENS" {
		resp.StopReason = "length"
	}
	return resp, nil
}

func normalizeStopReason[T any](_ []T, def string) string {
	// Reserved for future expansion (we map MAX_TOKENS → length in
	// fromWireFormat for the model that has it).
	return def
}

// doRequest executes a POST against the Gemini REST endpoint.
func (g *Gemini) doRequest(ctx context.Context, model string, body map[string]any) ([]byte, error) {
	payload, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("encode request: %w", err)
	}
	endpoint := fmt.Sprintf("%s/v1beta/models/%s:generateContent?key=%s",
		g.cfg.Endpoint, model, g.apiKey)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(payload))
	if err != nil {
		return nil, fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := g.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("call gemini: %w", err)
	}
	defer resp.Body.Close()
	raw, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("gemini status %d: %s", resp.StatusCode, string(raw))
	}
	return raw, nil
}