package llm

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/config"
)

// Anthropic implements agent.LLM against Anthropic's Messages API.
//
// API reference: https://docs.anthropic.com/en/api/messages
type Anthropic struct {
	cfg    config.ProviderConfig
	apiKey string
	client *http.Client
}

// NewAnthropic constructs an Anthropic provider.
func NewAnthropic(cfg config.ProviderConfig) (*Anthropic, error) {
	if cfg.APIKey == "" {
		return nil, fmt.Errorf("anthropic.api_key is required")
	}
	timeout := cfg.Timeout
	if timeout == 0 {
		timeout = 60 * time.Second
	}
	return &Anthropic{
		cfg:    cfg,
		apiKey: cfg.APIKey,
		client: &http.Client{Timeout: timeout},
	}, nil
}

// Name implements agent.LLM.
func (a *Anthropic) Name() string { return "anthropic" }

// Generate implements agent.LLM.
func (a *Anthropic) Generate(ctx context.Context, req agent.GenerateRequest) (*agent.Response, error) {
	body := a.toWireFormat(req)
	raw, err := a.doRequest(ctx, req.Model, body)
	if err != nil {
		return nil, err
	}
	return a.fromWireFormat(raw)
}

// Stream implements agent.LLM via single-event emulation.
func (a *Anthropic) Stream(ctx context.Context, req agent.GenerateRequest) (<-chan agent.Event, error) {
	out := make(chan agent.Event, 16)
	go func() {
		defer close(out)
		resp, err := a.Generate(ctx, req)
		if err != nil {
			out <- agent.Event{Type: agent.EventError, Error: err.Error(), Time: time.Now()}
			return
		}
		out <- agent.Event{Type: agent.EventMessage, Message: &resp.Message, Time: time.Now()}
		out <- agent.Event{Type: agent.EventDone, Time: time.Now()}
	}()
	return out, nil
}

// toWireFormat maps our canonical request to Anthropic's /v1/messages.
func (a *Anthropic) toWireFormat(req agent.GenerateRequest) map[string]any {
	messages := make([]map[string]any, 0, len(req.Messages))
	for _, m := range req.Messages {
		if m.Role == agent.RoleSystem {
			continue // Anthropic uses top-level system field
		}
		mm := map[string]any{
			"role": string(m.Role),
		}
		if m.Role == agent.RoleTool {
			mm["role"] = "user"
			mm["content"] = []map[string]any{{
				"type":        "tool_result",
				"tool_use_id":  m.ToolCallID,
				"content":     m.Content,
			}}
		} else if len(m.ToolCalls) > 0 {
			content := []map[string]any{}
			if m.Content != "" {
				content = append(content, map[string]any{"type": "text", "text": m.Content})
			}
			for _, tc := range m.ToolCalls {
				var args any
				_ = json.Unmarshal(tc.Arguments, &args)
				content = append(content, map[string]any{
					"type":  "tool_use",
					"id":    tc.ID,
					"name":  tc.Name,
					"input": args,
				})
			}
			mm["content"] = content
		} else {
			mm["content"] = m.Content
		}
		messages = append(messages, mm)
	}
	body := map[string]any{
		"model":      req.Model,
		"messages":   messages,
		"max_tokens": req.MaxTokens,
	}
	if req.Temperature > 0 {
		body["temperature"] = req.Temperature
	}
	if req.SystemPrompt != "" {
		body["system"] = req.SystemPrompt
	}
	if len(req.Tools) > 0 {
		tools := make([]map[string]any, 0, len(req.Tools))
		for _, t := range req.Tools {
			var schema any
			_ = json.Unmarshal(t.Parameters, &schema)
			tools = append(tools, map[string]any{
				"name":         t.Name,
				"description":  t.Description,
				"input_schema": schema,
			})
		}
		body["tools"] = tools
	}
	return body
}

// fromWireFormat parses an Anthropic messages response.
func (a *Anthropic) fromWireFormat(raw []byte) (*agent.Response, error) {
	var wire struct {
		Content []struct {
			Type  string          `json:"type"`
			Text  string          `json:"text,omitempty"`
			ID    string          `json:"id,omitempty"`
			Name  string          `json:"name,omitempty"`
			Input json.RawMessage `json:"input,omitempty"`
		} `json:"content"`
		StopReason   string `json:"stop_reason"`
		Usage        struct {
			InputTokens  int `json:"input_tokens"`
			OutputTokens int `json:"output_tokens"`
		} `json:"usage"`
	}
	if err := json.Unmarshal(raw, &wire); err != nil {
		return nil, fmt.Errorf("decode anthropic response: %w", err)
	}
	resp := &agent.Response{
		StopReason: anthropicStopReason(wire.StopReason),
		Usage: agent.Usage{
			PromptTokens:     wire.Usage.InputTokens,
			CompletionTokens: wire.Usage.OutputTokens,
			TotalTokens:      wire.Usage.InputTokens + wire.Usage.OutputTokens,
		},
	}
	msg := agent.Message{Role: agent.RoleAssistant, Timestamp: time.Now()}
	var textBuf bytes.Buffer
	for _, c := range wire.Content {
		switch c.Type {
		case "text":
			textBuf.WriteString(c.Text)
		case "tool_use":
			msg.ToolCalls = append(msg.ToolCalls, agent.ToolCall{
				ID:        c.ID,
				Name:      c.Name,
				Arguments: c.Input,
			})
		}
	}
	msg.Content = textBuf.String()
	resp.Message = msg
	return resp, nil
}

func anthropicStopReason(reason string) string {
	switch reason {
	case "end_turn":
		return "stop"
	case "tool_use":
		return "tool_calls"
	case "max_tokens":
		return "length"
	default:
		return reason
	}
}

func (a *Anthropic) doRequest(ctx context.Context, model string, body map[string]any) ([]byte, error) {
	payload, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("encode request: %w", err)
	}
	endpoint := strings.TrimRight(a.cfg.Endpoint, "/") + "/v1/messages"
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(payload))
	if err != nil {
		return nil, fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("x-api-key", a.apiKey)
	req.Header.Set("anthropic-version", "2023-06-01")
	resp, err := a.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("call anthropic: %w", err)
	}
	defer resp.Body.Close()
	raw, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("anthropic status %d: %s", resp.StatusCode, string(raw))
	}
	return raw, nil
}