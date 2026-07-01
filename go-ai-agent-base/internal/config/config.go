// Package config holds the runtime configuration for the agent platform.
// Configuration sources (lowest → highest precedence):
//
//  1. built-in defaults
//  2. YAML file at --config
//  3. Environment variables (XARCH_AGENT_*)
//  4. CLI flags
package config

import (
	"fmt"
	"strings"
	"time"

	"github.com/spf13/viper"
)

// Config is the root configuration object. Sub-structs group related
// concerns so callers can `cfg.Server.Port` rather than fishing for
// flat keys.
type Config struct {
	Server        ServerConfig        `mapstructure:"server"`
	Agent         AgentConfig         `mapstructure:"agent"`
	LLM           LLMConfig           `mapstructure:"llm"`
	Memory        MemoryConfig        `mapstructure:"memory"`
	Session       SessionConfig       `mapstructure:"session"`
	Auth          AuthConfig          `mapstructure:"auth"`
	Observability ObservabilityConfig  `mapstructure:"observability"`
	Tools         ToolsConfig         `mapstructure:"tools"`
	MCP           MCPConfig           `mapstructure:"mcp"`
}

// ServerConfig controls the HTTP listener.
type ServerConfig struct {
	Host            string        `mapstructure:"host"`
	Port            int           `mapstructure:"port"`
	ReadTimeout     time.Duration `mapstructure:"read_timeout"`
	WriteTimeout    time.Duration `mapstructure:"write_timeout"`
	ShutdownTimeout time.Duration `mapstructure:"shutdown_timeout"`
	CORS            CORSConfig    `mapstructure:"cors"`
}

// CORSConfig configures Cross-Origin Resource Sharing.
type CORSConfig struct {
	AllowedOrigins []string `mapstructure:"allowed_origins"`
	AllowedMethods []string `mapstructure:"allowed_methods"`
	AllowedHeaders []string `mapstructure:"allowed_headers"`
}

// AgentConfig selects the default agent and runtime behaviour.
type AgentConfig struct {
	Default       string  `mapstructure:"default"`
	Agents        []Agent `mapstructure:"agents"`
	MaxIterations int     `mapstructure:"max_iterations"`
	Timeout       time.Duration `mapstructure:"timeout"`
}

// Agent is a named agent definition. The Platform loads these at startup
// and exposes them at /api/v1/agents/{name}.
type Agent struct {
	Name         string        `mapstructure:"name"`
	Provider     string        `mapstructure:"provider"` // gemini | openai | anthropic
	Model        string        `mapstructure:"model"`
	SystemPrompt string        `mapstructure:"system_prompt"`
	Temperature  float64       `mapstructure:"temperature"`
	MaxTokens    int           `mapstructure:"max_tokens"`
	Tools        []string      `mapstructure:"tools"` // references by name
	MCPServers   []string      `mapstructure:"mcp_servers"` // references by name
	Timeout      time.Duration `mapstructure:"timeout"`
}

// LLMConfig configures the upstream LLM providers.
type LLMConfig struct {
	DefaultProvider string                 `mapstructure:"default_provider"`
	Gemini          ProviderConfig         `mapstructure:"gemini"`
	OpenAI          ProviderConfig         `mapstructure:"openai"`
	Anthropic       ProviderConfig         `mapstructure:"anthropic"`
	Ollama          ProviderConfig         `mapstructure:"ollama"`
}

// ProviderConfig holds per-provider connection settings.
type ProviderConfig struct {
	APIKey      string        `mapstructure:"api_key"`
	Endpoint    string        `mapstructure:"endpoint"`
	OrgID       string        `mapstructure:"org_id"`
	ProjectID   string        `mapstructure:"project_id"` // for Vertex-style Gemini
	Region      string        `mapstructure:"region"`
	Timeout     time.Duration `mapstructure:"timeout"`
	MaxRetries  int           `mapstructure:"max_retries"`
	RateLimitRPS int          `mapstructure:"rate_limit_rps"`
}

// MemoryConfig configures the long-term memory store.
type MemoryConfig struct {
	Backend string `mapstructure:"backend"` // memory | redis | postgres
	Redis   RedisConfig `mapstructure:"redis"`
	Postgres PostgresConfig `mapstructure:"postgres"`
	MaxEntries int `mapstructure:"max_entries"`
}

// RedisConfig holds Redis connection settings.
type RedisConfig struct {
	URL      string `mapstructure:"url"`
	DB       int    `mapstructure:"db"`
	Password string `mapstructure:"password"`
	Prefix   string `mapstructure:"prefix"`
}

// PostgresConfig holds PostgreSQL connection settings.
type PostgresConfig struct {
	URL   string `mapstructure:"url"`
	Table string `mapstructure:"table"`
}

// SessionConfig configures conversation state storage.
type SessionConfig struct {
	Backend string `mapstructure:"backend"` // memory | redis
	Redis   RedisConfig `mapstructure:"redis"`
	TTL     time.Duration `mapstructure:"ttl"`
	MaxMessages int `mapstructure:"max_messages"`
}

// AuthConfig configures authentication.
type AuthConfig struct {
	Enabled    bool   `mapstructure:"enabled"`
	JWT        JWTConfig `mapstructure:"jwt"`
	APIKeys    []APIKey `mapstructure:"api_keys"`
}

// JWTConfig configures JWT verification.
type JWTConfig struct {
	Algorithm string `mapstructure:"algorithm"` // HS256 | RS256
	Secret    string `mapstructure:"secret"`     // for HS256
	PublicKeyPath string `mapstructure:"public_key_path"` // for RS256
	Issuer    string `mapstructure:"issuer"`
	Audience  string `mapstructure:"audience"`
}

// APIKey is a static API key for service-to-service auth.
type APIKey struct {
	Key       string   `mapstructure:"key"`
	Name      string   `mapstructure:"name"`
	Scopes    []string `mapstructure:"scopes"`
}

// ObservabilityConfig controls logging, tracing, metrics.
type ObservabilityConfig struct {
	LogLevel    string         `mapstructure:"log_level"`     // debug|info|warn|error
	LogFormat   string         `mapstructure:"log_format"`    // json|text
	ServiceName string         `mapstructure:"service_name"`
	Tracing     TracingConfig  `mapstructure:"tracing"`
	Metrics     MetricsConfig  `mapstructure:"metrics"`
}

// TracingConfig configures OpenTelemetry.
type TracingConfig struct {
	Enabled     bool    `mapstructure:"enabled"`
	Exporter    string  `mapstructure:"exporter"`     // otlp | jaeger | zipkin | stdout
	Endpoint    string  `mapstructure:"endpoint"`
	SampleRate  float64 `mapstructure:"sample_rate"`  // 0.0 .. 1.0
}

// MetricsConfig configures Prometheus.
type MetricsConfig struct {
	Enabled bool   `mapstructure:"enabled"`
	Path    string `mapstructure:"path"` // default /metrics
}

// ToolsConfig configures the tool registry.
type ToolsConfig struct {
	Directory string `mapstructure:"directory"` // optional YAML directory
}

// MCPConfig configures connections to MCP servers.
type MCPConfig struct {
	Servers []MCPServer `mapstructure:"servers"`
}

// MCPServer describes how to connect to one MCP server.
type MCPServer struct {
	Name    string            `mapstructure:"name"`
	Command string            `mapstructure:"command"`    // for stdio
	Args    []string          `mapstructure:"args"`
	Env     map[string]string `mapstructure:"env"`
	URL     string            `mapstructure:"url"`        // for HTTP+SSE
}

// Load reads the config file at the given path (if non-empty) and applies
// environment overrides, then unmarshals into Config.
//
// Environment variable mapping: XARCH_AGENT_<UPPERCASE_DOTTED_KEY>
// e.g. XARCH_AGENT_SERVER_PORT=9090 → cfg.Server.Port
func Load(path string) (*Config, error) {
	v := viper.New()

	// Built-in defaults
	v.SetDefault("server.host", "0.0.0.0")
	v.SetDefault("server.port", 8080)
	v.SetDefault("server.read_timeout", "30s")
	v.SetDefault("server.write_timeout", "30s")
	v.SetDefault("server.shutdown_timeout", "15s")
	v.SetDefault("server.cors.allowed_methods", []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
	v.SetDefault("server.cors.allowed_headers", []string{"Authorization", "Content-Type", "X-Request-Id"})

	v.SetDefault("agent.max_iterations", 10)
	v.SetDefault("agent.timeout", "60s")

	v.SetDefault("llm.default_provider", "gemini")
	v.SetDefault("llm.gemini.endpoint", "https://generativelanguage.googleapis.com")
	v.SetDefault("llm.openai.endpoint", "https://api.openai.com/v1")
	v.SetDefault("llm.anthropic.endpoint", "https://api.anthropic.com")
	v.SetDefault("llm.ollama.endpoint", "http://localhost:11434")
	v.SetDefault("llm.gemini.timeout", "60s")
	v.SetDefault("llm.openai.timeout", "60s")
	v.SetDefault("llm.anthropic.timeout", "60s")
	v.SetDefault("llm.gemini.max_retries", 3)

	v.SetDefault("memory.backend", "memory")
	v.SetDefault("memory.max_entries", 1000)

	v.SetDefault("session.backend", "memory")
	v.SetDefault("session.ttl", "24h")
	v.SetDefault("session.max_messages", 100)

	v.SetDefault("auth.enabled", true)
	v.SetDefault("auth.jwt.algorithm", "HS256")

	v.SetDefault("observability.log_level", "info")
	v.SetDefault("observability.log_format", "json")
	v.SetDefault("observability.service_name", "go-ai-agent-base")
	v.SetDefault("observability.tracing.enabled", true)
	v.SetDefault("observability.tracing.exporter", "stdout")
	v.SetDefault("observability.tracing.sample_rate", 1.0)
	v.SetDefault("observability.metrics.enabled", true)
	v.SetDefault("observability.metrics.path", "/metrics")

	// Config file
	if path != "" {
		v.SetConfigFile(path)
		v.SetConfigType("yaml")
		if err := v.ReadInConfig(); err != nil {
			return nil, fmt.Errorf("read config %s: %w", path, err)
		}
	}

	// Env overrides
	v.SetEnvPrefix("XARCH_AGENT")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	v.AutomaticEnv()

	cfg := &Config{}
	if err := v.Unmarshal(cfg); err != nil {
		return nil, fmt.Errorf("unmarshal config: %w", err)
	}
	if err := cfg.validate(); err != nil {
		return nil, fmt.Errorf("validate config: %w", err)
	}
	return cfg, nil
}

// validate performs cross-field validation that the simple defaults
// above can't express.
func (c *Config) validate() error {
	if c.LLM.DefaultProvider != "" {
		switch c.LLM.DefaultProvider {
		case "gemini", "openai", "anthropic", "ollama":
		default:
			return fmt.Errorf("unknown llm.default_provider: %s", c.LLM.DefaultProvider)
		}
	}
	if c.Auth.Enabled && c.Auth.JWT.Algorithm == "HS256" && c.Auth.JWT.Secret == "" {
		return fmt.Errorf("auth.enabled=true with HS256 requires auth.jwt.secret")
	}
	if c.Memory.Backend != "" {
		switch c.Memory.Backend {
		case "memory", "redis", "postgres":
		default:
			return fmt.Errorf("unknown memory.backend: %s", c.Memory.Backend)
		}
	}
	if c.Session.Backend != "" {
		switch c.Session.Backend {
		case "memory", "redis":
		default:
			return fmt.Errorf("unknown session.backend: %s", c.Session.Backend)
		}
	}
	return nil
}

// AgentByName returns the named agent definition. Returns nil if not found.
func (c *Config) AgentByName(name string) *Agent {
	for i := range c.Agent.Agents {
		if c.Agent.Agents[i].Name == name {
			return &c.Agent.Agents[i]
		}
	}
	return nil
}