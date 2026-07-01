// Package agent is the core runtime that wires together LLM providers,
// tools, memory, and sessions.
//
// The types defined here are the platform's canonical model. They wrap
// (and adapt when needed) the underlying google/adk-go types so the
// rest of the platform never imports adk-go directly.
package agent

import (
	"context"
	"encoding/json"
	"time"
)

// Message represents a single conversational turn.
type Message struct {
	Role       Role     `json:"role"`        // system | user | assistant | tool
	Content    string   `json:"content"`
	Name       string   `json:"name,omitempty"` // for tool messages: tool call id
	ToolCalls  []ToolCall `json:"tool_calls,omitempty"`
	ToolCallID string   `json:"tool_call_id,omitempty"`
	Timestamp  time.Time `json:"timestamp"`
}

// Role of a message in a conversation.
type Role string

const (
	RoleSystem    Role = "system"
	RoleUser      Role = "user"
	RoleAssistant Role = "assistant"
	RoleTool      Role = "tool"
)

// ToolCall is the LLM's request to invoke a tool.
type ToolCall struct {
	ID        string          `json:"id"`
	Name      string          `json:"name"`
	Arguments json.RawMessage `json:"arguments"`
}

// ToolResult is what a tool returned after execution.
type ToolResult struct {
	ToolCallID string `json:"tool_call_id"`
	Name       string `json:"name"`
	Content    string `json:"content"`
	IsError    bool   `json:"is_error,omitempty"`
}

// Response is the LLM's reply for a single turn.
type Response struct {
	Message   Message     `json:"message"`
	Usage     Usage       `json:"usage"`
	Duration  time.Duration `json:"duration"`
	StopReason string     `json:"stop_reason"` // "stop" | "tool_calls" | "length"
	Metadata  map[string]any `json:"metadata,omitempty"`
}

// Usage tracks token consumption.
type Usage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

// Event is a typed message emitted during agent execution. The HTTP
// layer streams these as SSE events to clients.
type Event struct {
	Type    EventType       `json:"type"`
	Content string          `json:"content,omitempty"`
	Message *Message        `json:"message,omitempty"`
	Tool    *ToolCall       `json:"tool_call,omitempty"`
	Result  *ToolResult     `json:"tool_result,omitempty"`
	Error   string          `json:"error,omitempty"`
	Time    time.Time       `json:"time"`
}

// EventType classifies events emitted by the runtime.
type EventType string

const (
	EventMessage     EventType = "message"
	EventToolCall    EventType = "tool_call"
	EventToolResult  EventType = "tool_result"
	EventError       EventType = "error"
	EventDone        EventType = "done"
)

// RunOptions configures a single agent invocation.
type RunOptions struct {
	AgentName  string
	SessionID  string
	UserID     string
	Messages   []Message
	Stream     bool             // if true, Emit() is called for each event
	Metadata   map[string]any   // passed through to events
}

// LLM is the provider-agnostic interface for text generation.
type LLM interface {
	// Name returns the provider name (gemini, openai, anthropic, ollama).
	Name() string
	// Generate produces a single Response for the given messages + tools.
	Generate(ctx context.Context, req GenerateRequest) (*Response, error)
	// Stream produces a sequence of Events (partial Response chunks,
	// tool calls, etc.) for the given request. Used for SSE.
	Stream(ctx context.Context, req GenerateRequest) (<-chan Event, error)
}

// GenerateRequest is the input to LLM.Generate / LLM.Stream.
type GenerateRequest struct {
	Model        string    `json:"model"`
	SystemPrompt string    `json:"system_prompt"`
	Messages     []Message `json:"messages"`
	Tools        []ToolDef `json:"tools,omitempty"`
	Temperature  float64   `json:"temperature"`
	MaxTokens    int       `json:"max_tokens"`
	StopSequences []string `json:"stop_sequences,omitempty"`
}

// ToolDef describes a tool to the LLM (name, description, JSON Schema).
type ToolDef struct {
	Name        string          `json:"name"`
	Description string          `json:"description"`
	Parameters  json.RawMessage `json:"parameters"` // JSON Schema object
}

// Tool is a single callable capability exposed to the LLM.
type Tool interface {
	// Name returns the unique tool identifier.
	Name() string
	// Description returns a human + LLM-readable description.
	Description() string
	// ParametersSchema returns a JSON Schema object describing the
	// tool's accepted arguments.
	ParametersSchema() json.RawMessage
	// Execute runs the tool with the given arguments.
	Execute(ctx context.Context, args json.RawMessage) (string, error)
}

// Memory is a long-term key-value store with optional similarity search.
// Use it for cross-session facts ("user prefers Go over Python"),
// user profile data, semantic recall, etc.
type Memory interface {
	Get(ctx context.Context, key string) (string, bool, error)
	Put(ctx context.Context, key, value string) error
	Delete(ctx context.Context, key string) error
	Search(ctx context.Context, query string, limit int) ([]MemoryHit, error)
	Close() error
}

// MemoryHit is a single result from a Memory.Search call.
type MemoryHit struct {
	Key   string  `json:"key"`
	Value string  `json:"value"`
	Score float64 `json:"score"`
}

// Session is a conversation-scoped state container.
type Session interface {
	ID() string
	UserID() string
	Get(ctx context.Context) ([]Message, error)
	Append(ctx context.Context, msg Message) error
	Clear(ctx context.Context) error
	Close() error
}

// SessionStore creates / loads Sessions by ID.
type SessionStore interface {
	Get(ctx context.Context, sessionID, userID string) (Session, error)
	Create(ctx context.Context, userID string) (Session, error)
	Delete(ctx context.Context, sessionID string) error
	Close() error
}

// ToolRegistry is the catalogue of available tools. Agents pull tools
// by name from the registry.
type ToolRegistry interface {
	Register(t Tool) error
	Get(name string) (Tool, bool)
	List() []ToolDef
}

// Emitter is how the agent runtime pushes events to a consumer
// (typically an SSE handler).
type Emitter interface {
	Emit(event Event) error
	Close() error
}

// EventHandlerFunc is a function adapter for Emitter.
type EventHandlerFunc func(event Event) error

// Emit implements Emitter.
func (f EventHandlerFunc) Emit(event Event) error { return f(event) }

// Close implements Emitter.
func (f EventHandlerFunc) Close() error { return nil }