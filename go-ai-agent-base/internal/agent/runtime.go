package agent

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"sync"
	"time"

	"github.com/google/uuid"
)

// Runtime orchestrates agent execution: prompt assembly, tool calling,
// memory recall, streaming. It is the single entry point for both the
// HTTP handler and the CLI.
//
// Runtime is safe for concurrent use across many requests — internal
// maps are read-only after Build().
type Runtime struct {
	cfg         Config
	providers   map[string]LLM          // provider name → LLM
	tools       map[string]Tool         // tool name → Tool
	agents      map[string]AgentSpec    // agent name → AgentSpec
	sessions    SessionStore
	memories    Memory
	defaultName string
}

// Config is what Runtime needs in order to construct an agent run.
type Config struct {
	Agents    []AgentSpec
	Providers map[string]LLM
	Tools     []Tool
	Sessions  SessionStore
	Memories  Memory
	DefaultAgent string
}

// AgentSpec is a runtime-side description of an agent. Mirrors
// config.Agent but with concrete references to its provider/tools.
type AgentSpec struct {
	Name         string
	Provider     string
	Model        string
	SystemPrompt string
	Temperature  float64
	MaxTokens    int
	ToolNames    []string
	Timeout      time.Duration
}

// Build assembles a Runtime from a Config.
func Build(cfg Config) (*Runtime, error) {
	if len(cfg.Agents) == 0 {
		return nil, errors.New("at least one agent required")
	}
	if cfg.Sessions == nil {
		return nil, errors.New("sessions required")
	}
	if cfg.Memories == nil {
		return nil, errors.New("memories required")
	}
	r := &Runtime{
		cfg:         cfg,
		providers:   map[string]LLM{},
		tools:       map[string]Tool{},
		agents:      map[string]AgentSpec{},
		sessions:    cfg.Sessions,
		memories:    cfg.Memories,
		defaultName: cfg.DefaultAgent,
	}
	for name, p := range cfg.Providers {
		r.providers[name] = p
	}
	for _, t := range cfg.Tools {
		r.tools[t.Name()] = t
	}
	for _, a := range cfg.Agents {
		r.agents[a.Name] = a
		if r.defaultName == "" {
			r.defaultName = a.Name
		}
	}
	if _, ok := r.agents[r.defaultName]; !ok {
		return nil, fmt.Errorf("default agent %q not found", r.defaultName)
	}
	return r, nil
}

// ListAgents returns the names of all registered agents (sorted).
func (r *Runtime) ListAgents() []string {
	names := make([]string, 0, len(r.agents))
	for n := range r.agents {
		names = append(names, n)
	}
	return names
}

// GetAgent returns the named AgentSpec.
func (r *Runtime) GetAgent(name string) (AgentSpec, bool) {
	a, ok := r.agents[name]
	return a, ok
}

// ListTools returns the names of all registered tools (sorted).
func (r *Runtime) ListTools() []string {
	names := make([]string, 0, len(r.tools))
	for n := range r.tools {
		names = append(names, n)
	}
	return names
}

// Run executes a single agent turn. If opts.Stream is true and emitter
// is non-nil, intermediate events are emitted. The final Response is
// always returned; on streaming, callers should consume events as they
// arrive and treat the returned Response as the summary.
func (r *Runtime) Run(ctx context.Context, opts RunOptions, emitter Emitter) (*Response, error) {
	spec, ok := r.agents[opts.AgentName]
	if !ok {
		spec, ok = r.agents[r.defaultName]
		if !ok {
			return nil, fmt.Errorf("agent %q not found", opts.AgentName)
		}
		opts.AgentName = r.defaultName
	}

	// Bound the entire turn
	if spec.Timeout > 0 {
		var cancel context.CancelFunc
		ctx, cancel = context.WithTimeout(ctx, spec.Timeout)
		defer cancel()
	}

	// Load session
	session, err := r.sessions.Get(ctx, opts.SessionID, opts.UserID)
	if err != nil {
		return nil, fmt.Errorf("load session: %w", err)
	}

	// Memory recall (concatenate relevant facts to system prompt)
	memCtx, _ := r.memories.Search(ctx, lastUserText(opts.Messages), 5)
	systemPrompt := assembleSystemPrompt(spec.SystemPrompt, memCtx)

	// Persist user messages into session
	for _, m := range opts.Messages {
		if err := session.Append(ctx, m); err != nil {
			return nil, fmt.Errorf("append msg: %w", err)
		}
	}

	// Pull prior history
	history, err := session.Get(ctx)
	if err != nil {
		return nil, fmt.Errorf("load history: %w", err)
	}

	// Build LLM request
	req := GenerateRequest{
		Model:        spec.Model,
		SystemPrompt: systemPrompt,
		Messages:     stripSystemMessages(history),
		Tools:        r.toolDefs(spec.ToolNames),
		Temperature:  spec.Temperature,
		MaxTokens:    spec.MaxTokens,
	}

	provider, ok := r.providers[spec.Provider]
	if !ok {
		return nil, fmt.Errorf("provider %q not registered", spec.Provider)
	}

	// Agent loop: call LLM, execute any tool calls, repeat up to N times
	maxIter := r.cfg.MaxIterations
	if maxIter <= 0 {
		maxIter = 10
	}
	for iter := 0; iter < maxIter; iter++ {
		// Optional: emit a thinking event for the iteration
		if emitter != nil {
			_ = emitter.Emit(Event{Type: EventMessage, Time: time.Now()})
		}

		start := time.Now()
		resp, err := provider.Generate(ctx, req)
		if err != nil {
			if emitter != nil {
				_ = emitter.Emit(Event{Type: EventError, Error: err.Error(), Time: time.Now()})
			}
			return nil, fmt.Errorf("llm generate: %w", err)
		}
		resp.Duration = time.Since(start)
		resp.Message.Timestamp = time.Now()

		// Persist assistant message
		if err := session.Append(ctx, resp.Message); err != nil {
			return nil, fmt.Errorf("persist assistant: %w", err)
		}

		// Emit the assistant message
		if emitter != nil {
			_ = emitter.Emit(Event{Type: EventMessage, Message: &resp.Message, Time: time.Now()})
		}

		// If the LLM didn't ask to call any tools, we're done.
		if len(resp.Message.ToolCalls) == 0 {
			if emitter != nil {
				_ = emitter.Emit(Event{Type: EventDone, Time: time.Now()})
			}
			return resp, nil
		}

		// Execute each tool call in parallel, then add the results back
		// to the conversation and continue.
		results := r.executeTools(ctx, resp.Message.ToolCalls, emitter)
		for _, res := range results {
			if err := session.Append(ctx, Message{
				Role:       RoleTool,
				ToolCallID: res.ToolCallID,
				Name:       res.Name,
				Content:    res.Content,
				Timestamp:  time.Now(),
			}); err != nil {
				return nil, fmt.Errorf("persist tool result: %w", err)
			}
		}
		// Refresh history and loop
		history, err = session.Get(ctx)
		if err != nil {
			return nil, fmt.Errorf("reload history: %w", err)
		}
		req.Messages = stripSystemMessages(history)
	}
	return nil, fmt.Errorf("agent %q exceeded max iterations (%d)", opts.AgentName, maxIter)
}

// executeTools runs tool calls concurrently. Returns results in the
// same order as input.
func (r *Runtime) executeTools(ctx context.Context, calls []ToolCall, emitter Emitter) []ToolResult {
	results := make([]ToolResult, len(calls))
	var wg sync.WaitGroup
	for i, c := range calls {
		i, c := i, c
		wg.Add(1)
		go func() {
			defer wg.Done()
			t, ok := r.tools[c.Name]
			if !ok {
				results[i] = ToolResult{
					ToolCallID: c.ID,
					Name:       c.Name,
					Content:    fmt.Sprintf("error: unknown tool %q", c.Name),
					IsError:    true,
				}
				return
			}
			if emitter != nil {
				_ = emitter.Emit(Event{Type: EventToolCall, Tool: &calls[i], Time: time.Now()})
			}
			out, err := t.Execute(ctx, c.Arguments)
			res := ToolResult{ToolCallID: c.ID, Name: c.Name, Content: out}
			if err != nil {
				res.IsError = true
				res.Content = "error: " + err.Error()
			}
			results[i] = res
			if emitter != nil {
				_ = emitter.Emit(Event{Type: EventToolResult, Result: &res, Time: time.Now()})
			}
		}()
	}
	wg.Wait()
	return results
}

func (r *Runtime) toolDefs(names []string) []ToolDef {
	out := make([]ToolDef, 0, len(names))
	for _, n := range names {
		if t, ok := r.tools[n]; ok {
			out = append(out, ToolDef{
				Name:        t.Name(),
				Description: t.Description(),
				Parameters:  t.ParametersSchema(),
			})
		}
	}
	return out
}

func assembleSystemPrompt(base string, memories []MemoryHit) string {
	if len(memories) == 0 {
		return base
	}
	prefix := base + "\n\n## Relevant long-term memory\n"
	for _, m := range memories {
		prefix += fmt.Sprintf("- %s\n", m.Value)
	}
	return prefix
}

func lastUserText(msgs []Message) string {
	for i := len(msgs) - 1; i >= 0; i-- {
		if msgs[i].Role == RoleUser {
			return msgs[i].Content
		}
	}
	return ""
}

func stripSystemMessages(msgs []Message) []Message {
	out := make([]Message, 0, len(msgs))
	for _, m := range msgs {
		if m.Role != RoleSystem {
			out = append(out, m)
		}
	}
	return out
}

// NewSessionID is a small helper exposed so the HTTP handler and CLI
// can mint fresh IDs without sharing helper packages.
func NewSessionID() string { return uuid.NewString() }

// ToJSON marshals v to indented JSON for log fields.
func ToJSON(v any) string {
	b, err := json.MarshalIndent(v, "", "  ")
	if err != nil {
		return fmt.Sprintf("%v", v)
	}
	return string(b)
}