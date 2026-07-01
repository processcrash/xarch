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

// Ollama implements agent.LLM against a local Ollama instance.
// Reuses the OpenAI wire format because Ollama exposes a
// /v1/chat/completions compatible endpoint.
type Ollama struct {
	cfg    config.ProviderConfig
	client *http.Client
}

// NewOllama constructs an Ollama provider (no API key needed; localhost assumed).
func NewOllama(cfg config.ProviderConfig) (*Ollama, error) {
	if cfg.Endpoint == "" {
		cfg.Endpoint = "http://localhost:11434"
	}
	timeout := cfg.Timeout
	if timeout == 0 {
		timeout = 120 * time.Second
	}
	return &Ollama{
		cfg:    cfg,
		client: &http.Client{Timeout: timeout},
	}, nil
}

// Name implements agent.LLM.
func (o *Ollama) Name() string { return "ollama" }

// Generate implements agent.LLM by delegating to the OpenAI-compatible
// /v1/chat/completions endpoint.
func (o *Ollama) Generate(ctx context.Context, req agent.GenerateRequest) (*agent.Response, error) {
	body := o.toWireFormat(req)
	payload, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("encode request: %w", err)
	}
	endpoint := o.cfg.Endpoint + "/v1/chat/completions"
	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(payload))
	if err != nil {
		return nil, fmt.Errorf("build request: %w", err)
	}
	httpReq.Header.Set("Content-Type", "application/json")
	resp, err := o.client.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("call ollama: %w", err)
	}
	defer resp.Body.Close()
	raw, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("ollama status %d: %s", resp.StatusCode, string(raw))
	}
	var wire struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
			FinishReason string `json:"finish_reason"`
		} `json:"choices"`
	}
	if err := json.Unmarshal(raw, &wire); err != nil {
		return nil, fmt.Errorf("decode ollama response: %w", err)
	}
	resp2 := &agent.Response{StopReason: "stop"}
	if len(wire.Choices) > 0 {
		resp2.Message = agent.Message{
			Role:      agent.RoleAssistant,
			Content:   wire.Choices[0].Message.Content,
			Timestamp: time.Now(),
		}
	}
	return resp2, nil
}

// Stream implements agent.LLM via a single-event emulation.
func (o *Ollama) Stream(ctx context.Context, req agent.GenerateRequest) (<-chan agent.Event, error) {
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

func (o *Ollama) toWireFormat(req agent.GenerateRequest) map[string]any {
	msgs := make([]map[string]any, 0, len(req.Messages)+1)
	if req.SystemPrompt != "" {
		msgs = append(msgs, map[string]any{"role": "system", "content": req.SystemPrompt})
	}
	for _, m := range req.Messages {
		msgs = append(msgs, map[string]any{"role": string(m.Role), "content": m.Content})
	}
	body := map[string]any{
		"model":    req.Model,
		"messages": msgs,
		"stream":   false,
	}
	if req.Temperature > 0 {
		body["temperature"] = req.Temperature
	}
	return body
}