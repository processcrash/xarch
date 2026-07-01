package llm

import (
	"fmt"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/config"
)

// Factory builds LLM providers from config.Providers. Unknown names
// produce an error so configuration mistakes surface at startup, not
// at first request.
type Factory struct {
	providers map[string]config.ProviderConfig
}

// NewFactory creates a Factory from the top-level LLM config.
func NewFactory(cfg config.LLMConfig) *Factory {
	return &Factory{providers: map[string]config.ProviderConfig{
		"gemini":    cfg.Gemini,
		"openai":    cfg.OpenAI,
		"anthropic": cfg.Anthropic,
		"ollama":    cfg.Ollama,
	}}
}

// Build returns the named provider, constructing it lazily.
func (f *Factory) Build(name string) (agent.LLM, error) {
	cfg, ok := f.providers[name]
	if !ok {
		return nil, fmt.Errorf("unknown llm provider %q", name)
	}
	switch name {
	case "gemini":
		return NewGemini(cfg)
	case "openai":
		return NewOpenAI(cfg)
	case "anthropic":
		return NewAnthropic(cfg)
	case "ollama":
		return NewOllama(cfg)
	default:
		return nil, fmt.Errorf("no constructor for provider %q", name)
	}
}

// BuildAll constructs every configured provider. Providers missing
// their credentials are skipped silently (the runtime can still pick a
// different one).
func (f *Factory) BuildAll() (map[string]agent.LLM, error) {
	out := map[string]agent.LLM{}
	for name, cfg := range f.providers {
		_ = cfg // silence linter for "declared and not used" if all are empty
		p, err := f.Build(name)
		if err != nil {
			continue
		}
		out[name] = p
	}
	if len(out) == 0 {
		return nil, fmt.Errorf("no LLM providers configured (set API keys via env: GOOGLE_API_KEY, OPENAI_API_KEY, ANTHROPIC_API_KEY)")
	}
	return out, nil
}