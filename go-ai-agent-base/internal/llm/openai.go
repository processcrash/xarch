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

// OpenAI implements agent.LLM against OpenAI's chat completions API.
// Also works with any OpenAI-compatible endpoint (Azure OpenAI,
// Together, vLLM, etc.) by setting config.endpoint.
type OpenAI struct {
	cfg    config.ProviderConfig
	apiKey string
	client *http.Client
}

// NewOpenAI constructs an OpenAI provider.
func NewOpenAI(cfg config.ProviderConfig) (*OpenAI, error) {
	if cfg.APIKey == "" {
		return nil, fmt.Errorf("openai.api_key is required")
	}
	timeout := cfg.Timeout
	if timeout == 0 {
		timeout = 60 * time.Second
	}
	return &OpenAI{
		cfg:    cfg,
		apiKey: cfg.APIKey,
		client: &http.Client{Timeout: timeout},
	}, nil
}

// Name implements agent.LLM.
func (o *OpenAI) Name() string { return "openai" }

// Generate implements agent.LLM.
func (o *OpenAI) Generate(ctx context.Context, req agent.GenerateRequest) (*agent.Response, error) {
	body := o.toWireFormat(req)
	raw, err := o.doRequest(ctx, req.Model, body)
	if err != nil {
		return nil, err
	}
	return o.fromWireFormat(raw)
}

// Stream implements agent.LLM via a single-event emulation.
func (o *OpenAI) Stream(ctx context.Context, req agent.GenerateRequest) (<-chan agent.Event, error) {
	out := make(chan agent.Event, 16)
	go func() {
		defer close(out)
		resp, err := o.Generate(ctx, req)
		if err != nil {
			out <- agent.Event{Type: agent.EventError, Error: err.Error(), Time: time.Now()}
			return
		}
		out <- agent.Event{Type: agent.EventMessage, Message: &resp.Message, Time: time.Now()}
		out <- agent.Event{Type: agent.EventDone, Time: time.Now()}
	}()
	return out, nil
}

// toWireFormat maps our canonical request to OpenAI's chat.completions
// request body.
func (o *OpenAI) toWireFormat(req agent.GenerateRequest) map[string]any {
	msgs := make([]map[string]any, 0, len(req.Messages)+1)
	if req.SystemPrompt != "" {
		msgs = append(msgs, map[string]any{
			"role":    "system",
			"content": req.SystemPrompt,
		})
	}
	for _, m := range req.Messages {
		mm := map[string]any{
			"role":    string(m.Role),
			"content": m.Content,
		}
		if m.Name != "" {
			mm["name"] = m.Name
		}
		if len(m.ToolCalls) > 0 {
			tcs := make([]map[string]any, 0, len(m.ToolCalls))
			for _, tc := range m.ToolCalls {
				tcs = append(tcs, map[string]any{
					"id":       tc.ID,
					"type":     "function",
					"function": map[string]any{"name": tc.Name, "arguments": string(tc.Arguments)},
				})
			}
			mm["tool_calls"] = tcs
		}
		if m.Role == agent.RoleTool && m.ToolCallID != "" {
			mm["tool_call_id"] = m.ToolCallID
		}
		msgs = append(msgs, mm)
	}

	body := map[string]any{
		"model":       req.Model,
		"messages":    msgs,
		"temperature": req.Temperature,
	}
	if req.MaxTokens > 0 {
		body["max_tokens"] = req.MaxTokens
	}
	if len(req.Tools) > 0 {
		tools := make([]map[string]any, 0, len(req.Tools))
		for _, t := range req.Tools {
			tools = append(tools, map[string]any{
				"type": "function",
				"function": map[string]any{
					"name":        t.Name,
					"description": t.Description,
					"parameters":  json.RawMessage(t.Parameters),
				},
			})
		}
		body["tools"] = tools
	}
	return body
}

// fromWireFormat parses an OpenAI chat.completion response.
func (o *OpenAI) fromWireFormat(raw []byte) (*agent.Response, error) {
	var wire struct {
		Choices []struct {
			Message struct {
				Role      string `json:"role"`
				Content   string `json:"content"`
				ToolCalls []struct {
					ID       string `json:"id"`
					Type     string `json:"type"`
					Function struct {
						Name      string `json:"name"`
						Arguments string `json:"arguments"`
					} `json:"function"`
				} `json:"tool_calls"`
			} `json:"message"`
			FinishReason string `json:"finish_reason"`
		} `json:"choices"`
		Usage struct {
			PromptTokens     int `json:"prompt_tokens"`
			CompletionTokens int `json:"completion_tokens"`
			TotalTokens      int `json:"total_tokens"`
		} `json:"usage"`
	}
	if err := json.Unmarshal(raw, &wire); err != nil {
		return nil, fmt.Errorf("decode openai response: %w", err)
	}
	resp := &agent.Response{
		Usage: agent.Usage{
			PromptTokens:     wire.Usage.PromptTokens,
			CompletionTokens: wire.Usage.CompletionTokens,
			TotalTokens:      wire.Usage.TotalTokens,
		},
	}
	if len(wire.Choices) == 0 {
		return resp, nil
	}
	c := wire.Choices[0]
	msg := agent.Message{Role: agent.RoleAssistant, Content: c.Message.Content, Timestamp: time.Now()}
	for _, tc := range c.Message.ToolCalls {
		msg.ToolCalls = append(msg.ToolCalls, agent.ToolCall{
			ID:        tc.ID,
			Name:      tc.Function.Name,
			Arguments: json.RawMessage(tc.Function.Arguments),
		})
	}
	resp.Message = msg
	resp.StopReason = openAIStopReason(c.FinishReason)
	return resp, nil
}

func openAIStopReason(reason string) string {
	switch reason {
	case "stop":
		return "stop"
	case "tool_calls":
		return "tool_calls"
	case "length", "max_tokens":
		return "length"
	default:
		return reason
	}
}

func (o *OpenAI) doRequest(ctx context.Context, model string, body map[string]any) ([]byte, error) {
	payload, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("encode request: %w", err)
	}
	endpoint := o.cfg.Endpoint + "/chat/completions"
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(payload))
	if err != nil {
		return nil, fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+o.apiKey)
	if o.cfg.OrgID != "" {
		req.Header.Set("OpenAI-Organization", o.cfg.OrgID)
	}
	resp, err := o.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("call openai: %w", err)
	}
	defer resp.Body.Close()
	raw, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("openai status %d: %s", resp.StatusCode, string(raw))
	}
	return raw, nil
}

// SetEndpoint allows tests to override the endpoint.
func (o *OpenAI) SetEndpoint(ep string) { o.cfg.Endpoint = ep }