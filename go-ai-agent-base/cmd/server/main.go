// server is the HTTP entry point for the agent platform.
// It loads config, wires all subsystems (LLM providers, tools,
// memory, sessions, observability, auth), and starts the Gin server.
package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"go.uber.org/zap"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/auth"
	"github.com/xarch/go-ai-agent-base/internal/config"
	"github.com/xarch/go-ai-agent-base/internal/llm"
	"github.com/xarch/go-ai-agent-base/internal/memory"
	"github.com/xarch/go-ai-agent-base/internal/observability"
	"github.com/xarch/go-ai-agent-base/internal/server"
	"github.com/xarch/go-ai-agent-base/internal/session"
	"github.com/xarch/go-ai-agent-base/internal/tools"
)

// version is overridden via -ldflags at build time.
var version = "0.1.0-dev"

func main() {
	if err := run(); err != nil {
		fmt.Fprintf(os.Stderr, "fatal: %v\n", err)
		os.Exit(1)
	}
}

func run() error {
	configPath := ""
	for i, arg := range os.Args {
		if arg == "--config" && i+1 < len(os.Args) {
			configPath = os.Args[i+1]
		}
	}
	cfg, err := config.Load(configPath)
	if err != nil {
		return fmt.Errorf("load config: %w", err)
	}

	logger, err := observability.NewLogger(cfg.Observability)
	if err != nil {
		return fmt.Errorf("logger: %w", err)
	}
	defer logger.Sync() //nolint:errcheck

	shutdownTracer, err := observability.SetupTracing(context.Background(), cfg.Observability)
	if err != nil {
		logger.Warn("tracing setup failed", zap.Error(err))
	} else {
		defer func() { _ = shutdownTracer(context.Background()) }()
	}

	metrics := observability.NewMetrics()

	// Build LLM providers
	llmFactory := llm.NewFactory(cfg.LLM)
	providers, err := llmFactory.BuildAll()
	if err != nil {
		return fmt.Errorf("build LLMs: %w", err)
	}

	// Build tools registry
	registry := tools.NewRegistry()
	for _, t := range tools.Builtins() {
		_ = registry.Register(t)
	}

	// Build memory + session
	mem, err := memory.Factory(cfg.Memory.Backend, memory.Opts{
		MaxEntries:    cfg.Memory.MaxEntries,
		RedisURL:      cfg.Memory.Redis.URL,
		RedisPrefix:   cfg.Memory.Redis.Prefix,
		PostgresURL:   cfg.Memory.Postgres.URL,
		PostgresTable: cfg.Memory.Postgres.Table,
	})
	if err != nil {
		return fmt.Errorf("memory: %w", err)
	}
	defer mem.Close() //nolint:errcheck

	sessions, err := session.Factory(cfg.Session.Backend, cfg.Session.TTL, cfg.Session.MaxMessages, cfg.Session.Redis.URL)
	if err != nil {
		return fmt.Errorf("sessions: %w", err)
	}
	defer sessions.Close() //nolint:errcheck
	server.SetSessionStore(sessions)

	// Wire agents
	agentSpecs := make([]agent.AgentSpec, 0, len(cfg.Agent.Agents))
	for _, a := range cfg.Agent.Agents {
		spec := agent.AgentSpec{
			Name:         a.Name,
			Provider:     a.Provider,
			Model:        a.Model,
			SystemPrompt: a.SystemPrompt,
			Temperature:  a.Temperature,
			MaxTokens:    a.MaxTokens,
			ToolNames:    a.Tools,
			Timeout:      a.Timeout,
		}
		agentSpecs = append(agentSpecs, spec)
	}

	rt, err := agent.Build(agent.Config{
		Agents:       agentSpecs,
		Providers:    providers,
		Tools:        registryTools(registry),
		Sessions:     sessions,
		Memories:     mem,
		DefaultAgent: cfg.Agent.Default,
	})
	if err != nil {
		return fmt.Errorf("build runtime: %w", err)
	}

	// Auth verifier
	var verifier *auth.Verifier
	if cfg.Auth.Enabled {
		verifier, err = auth.NewVerifier(
			cfg.Auth.JWT.Algorithm,
			cfg.Auth.JWT.Secret,
			cfg.Auth.JWT.PublicKeyPath,
			cfg.Auth.JWT.Issuer,
			cfg.Auth.JWT.Audience,
		)
		if err != nil {
			return fmt.Errorf("auth verifier: %w", err)
		}
	}

	// Build HTTP server
	srv := server.New(cfg, rt, logger, metrics, verifier)

	// Graceful shutdown on SIGINT/SIGTERM
	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	logger.Info("starting server", zap.String("version", version), zap.Int("agents", len(agentSpecs)))
	if err := srv.Start(ctx); err != nil {
		return fmt.Errorf("server start: %w", err)
	}
	logger.Info("shutdown complete")
	return nil
}

func registryTools(r *tools.Registry) []agent.Tool {
	all := r.List()
	out := make([]agent.Tool, 0, len(all))
	for _, td := range all {
		t, _ := r.Get(td.Name)
		if t != nil {
			out = append(out, t)
		}
	}
	return out
}