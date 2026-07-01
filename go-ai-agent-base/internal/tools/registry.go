package tools

import (
	"errors"
	"sort"
	"sync"

	"github.com/xarch/go-ai-agent-base/internal/agent"
)

// Registry is the canonical implementation of agent.ToolRegistry.
// It maps tool names to implementations and produces ToolDef lists
// for the LLM.
type Registry struct {
	mu    sync.RWMutex
	tools map[string]agent.Tool
}

// NewRegistry creates an empty registry.
func NewRegistry() *Registry {
	return &Registry{tools: map[string]agent.Tool{}}
}

// Register implements agent.ToolRegistry.
func (r *Registry) Register(t agent.Tool) error {
	if t == nil {
		return errors.New("tool is nil")
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	if _, exists := r.tools[t.Name()]; exists {
		return errors.New("tool already registered: " + t.Name())
	}
	r.tools[t.Name()] = t
	return nil
}

// MustRegister registers and panics on error — for startup wiring only.
func (r *Registry) MustRegister(t agent.Tool) {
	if err := r.Register(t); err != nil {
		panic(err)
	}
}

// Get implements agent.ToolRegistry.
func (r *Registry) Get(name string) (agent.Tool, bool) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	t, ok := r.tools[name]
	return t, ok
}

// List implements agent.ToolRegistry.
func (r *Registry) List() []agent.ToolDef {
	r.mu.RLock()
	defer r.mu.RUnlock()
	out := make([]agent.ToolDef, 0, len(r.tools))
	for _, t := range r.tools {
		out = append(out, agent.ToolDef{
			Name:        t.Name(),
			Description: t.Description(),
			Parameters:  t.ParametersSchema(),
		})
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Name < out[j].Name })
	return out
}

// Names returns the registered tool names in sorted order.
func (r *Registry) Names() []string {
	r.mu.RLock()
	defer r.mu.RUnlock()
	out := make([]string, 0, len(r.tools))
	for n := range r.tools {
		out = append(out, n)
	}
	sort.Strings(out)
	return out
}