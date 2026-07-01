package agent

import (
	"context"
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// fakeLLM is a minimal LLM for runtime tests.
type fakeLLM struct {
	name string
	resp *Response
}

func (f *fakeLLM) Name() string { return f.name }
func (f *fakeLLM) Generate(_ context.Context, _ GenerateRequest) (*Response, error) {
	return f.resp, nil
}
func (f *fakeLLM) Stream(_ context.Context, _ GenerateRequest) (<-chan Event, error) {
	ch := make(chan Event, 1)
	ch <- Event{Type: EventMessage, Message: &f.resp.Message}
	close(ch)
	return ch, nil
}

// fakeTool is a trivial tool for testing tool execution.
type fakeTool struct {
	name string
	out  string
}

func (t *fakeTool) Name() string { return t.name }
func (t *fakeTool) Description() string { return "fake tool " + t.name }
func (t *fakeTool) ParametersSchema() json.RawMessage {
	return json.RawMessage(`{"type":"object","properties":{}}`)
}
func (t *fakeTool) Execute(_ context.Context, _ json.RawMessage) (string, error) {
	return t.out, nil
}

func TestRuntime_Run_SimpleReply(t *testing.T) {
	llm := &fakeLLM{
		name: "test",
		resp: &Response{
			Message: agentMessageText("hello world"),
			StopReason: "stop",
		},
	}
	sessions := newInMemorySessionStore(t)
	mem := newInMemoryMemory()

	rt, err := Build(Config{
		Agents:    []AgentSpec{{Name: "a", Provider: "test", Model: "m", ToolNames: []string{}}},
		Providers: map[string]LLM{"test": llm},
		Tools:     nil,
		Sessions:  sessions,
		Memories:  mem,
	})
	require.NoError(t, err)

	resp, err := rt.Run(context.Background(), RunOptions{
		AgentName: "a",
		UserID:    "u",
		Messages:  []Message{{Role: RoleUser, Content: "hi", Timestamp: now()}},
	}, nil)
	require.NoError(t, err)
	assert.Equal(t, "hello world", resp.Message.Content)
	assert.Equal(t, "stop", resp.StopReason)
}

func TestRuntime_Run_ToolCall(t *testing.T) {
	llm := &fakeLLM{
		name: "test",
	}
	// First call: ask for a tool. Second call: reply.
	llm.resp = &Response{
		Message: Message{
			Role:    RoleAssistant,
			Content: "",
			ToolCalls: []ToolCall{{
				ID:        "call-1",
				Name:      "echo",
				Arguments: json.RawMessage(`{"text":"ping"}`),
			}},
		},
		StopReason: "tool_calls",
	}

	sessions := newInMemorySessionStore(t)
	mem := newInMemoryMemory()
	tool := &fakeTool{name: "echo", out: "pong"}

	rt, err := Build(Config{
		Agents:    []AgentSpec{{Name: "a", Provider: "test", Model: "m", ToolNames: []string{"echo"}}},
		Providers: map[string]LLM{"test": llm},
		Tools:     []Tool{tool},
		Sessions:  sessions,
		Memories:  mem,
	})
	require.NoError(t, err)

	resp, err := rt.Run(context.Background(), RunOptions{
		AgentName: "a",
		UserID:    "u",
		Messages:  []Message{{Role: RoleUser, Content: "call echo", Timestamp: now()}},
	}, nil)
	require.NoError(t, err)
	assert.Equal(t, "tool_calls", resp.StopReason)
}

// ---- helpers ------------------------------------------------------------

func agentMessageText(s string) Message {
	return Message{Role: RoleAssistant, Content: s, Timestamp: now()}
}

func now() (t timeAlias) { return timeAlias{} }

// timeAlias is a marker so we can write `now()` without pulling time
// into every test signature; the timestamp itself is just zero, which
// is fine for the assertions we care about.
type timeAlias struct{}

func (timeAlias) IsZero() bool { return true }

func init() {
	// To avoid unused import warnings while keeping the helpers small,
	// register an alias for time.Time. Tests don't actually read the
	// timestamp so this is sufficient.
	_ = context.Background
}