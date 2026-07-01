// agent is the CLI entry point for local development. It runs a
// REPL-style chat session against a single configured agent.
package main

import (
	"bufio"
	"context"
	"fmt"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"go.uber.org/zap"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/config"
	"github.com/xarch/go-ai-agent-base/internal/llm"
	"github.com/xarch/go-ai-agent-base/internal/memory"
	"github.com/xarch/go-ai-agent-base/internal/observability"
	"github.com/xarch/go-ai-agent-base/internal/session"
	"github.com/xarch/go-ai-agent-base/internal/tools"
)

var version = "0.1.0-dev"

const usage = `xarch agent CLI — local REPL

Usage:
  agent chat [--agent NAME] [--config FILE]
  agent --help
  agent --version

Subcommands:
  chat     Start an interactive chat session with the named agent

Environment:
  XARCH_AGENT_<KEY>   Override any config setting (KEY is uppercased, dots → underscores)
`

func main() {
	if len(os.Args) < 2 {
		fmt.Print(usage)
		os.Exit(1)
	}
	args := os.Args[1:]
	cmd := args[0]
	switch cmd {
	case "chat":
		if err := runChat(args[1:]); err != nil {
			fmt.Fprintf(os.Stderr, "fatal: %v\n", err)
			os.Exit(1)
		}
	case "-h", "--help", "help":
		fmt.Print(usage)
	case "-v", "--version", "version":
		fmt.Println("agent", version)
	default:
		fmt.Fprintf(os.Stderr, "unknown command %q\n\n%s", cmd, usage)
		os.Exit(1)
	}
}

func runChat(args []string) error {
	agentName := ""
	configPath := ""
	for i := 0; i < len(args); i++ {
		switch args[i] {
		case "--agent", "-a":
			if i+1 >= len(args) {
				return fmt.Errorf("--agent requires a value")
			}
			agentName = args[i+1]
			i++
		case "--config", "-c":
			if i+1 >= len(args) {
				return fmt.Errorf("--config requires a value")
			}
			configPath = args[i+1]
			i++
		}
	}

	cfg, err := config.Load(configPath)
	if err != nil {
		return fmt.Errorf("load config: %w", err)
	}
	if agentName == "" {
		agentName = cfg.Agent.Default
	}
	if agentName == "" {
		return fmt.Errorf("no agent specified (use --agent NAME or set agent.default in config)")
	}

	logger, err := observability.NewLogger(cfg.Observability)
	if err != nil {
		return fmt.Errorf("logger: %w", err)
	}
	defer logger.Sync() //nolint:errcheck

	llmFactory := llm.NewFactory(cfg.LLM)
	providers, err := llmFactory.BuildAll()
	if err != nil {
		return fmt.Errorf("build LLMs: %w", err)
	}

	registry := tools.NewRegistry()
	for _, t := range tools.Builtins() {
		_ = registry.Register(t)
	}

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

	spec := findAgent(cfg, agentName)
	if spec == nil {
		return fmt.Errorf("agent %q not found", agentName)
	}

	rt, err := agent.Build(agent.Config{
		Agents:       []agent.AgentSpec{*spec},
		Providers:    providers,
		Tools:        toolsToSlice(registry),
		Sessions:     sessions,
		Memories:     mem,
		DefaultAgent: agentName,
	})
	if err != nil {
		return fmt.Errorf("build runtime: %w", err)
	}

	user := os.Getenv("USER")
	if user == "" {
		user = "cli-user"
	}
	sess, err := sessions.Create(context.Background(), user)
	if err != nil {
		return fmt.Errorf("create session: %w", err)
	}

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	logger.Info("chat session started", zap.String("agent", agentName), zap.String("session", sess.ID()))
	fmt.Printf("xarch agent chat — agent=%s  session=%s\n(type /exit to quit)\n\n", agentName, sess.ID())

	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1024*1024), 1024*1024)

	for {
		fmt.Print("you> ")
		if !scanner.Scan() {
			fmt.Println()
			return nil
		}
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}
		switch line {
		case "/exit", "/quit":
			return nil
		case "/help":
			fmt.Println("commands: /exit /help /clear")
			continue
		case "/clear":
			_ = sess.Clear(ctx)
			fmt.Println("(session cleared)")
			continue
		}

		opts := agent.RunOptions{
			AgentName: agentName,
			SessionID: sess.ID(),
			UserID:    user,
			Messages: []agent.Message{{
				Role:      agent.RoleUser,
				Content:   line,
				Timestamp: time.Now(),
			}},
		}
		start := time.Now()
		resp, err := rt.Run(ctx, opts, nil)
		if err != nil {
			fmt.Printf("[error] %v\n", err)
			continue
		}
		fmt.Printf("assistant> %s\n", resp.Message.Content)
		if len(resp.Message.ToolCalls) > 0 {
			fmt.Printf("  [tools used: %d, tokens=%d, dur=%s]\n",
				len(resp.Message.ToolCalls), resp.Usage.TotalTokens, resp.Duration)
		}
		fmt.Printf("  [tokens: %d prompt + %d completion = %d, dur=%s]\n\n",
			resp.Usage.PromptTokens, resp.Usage.CompletionTokens, resp.Usage.TotalTokens, time.Since(start))
	}
}

func findAgent(cfg *config.Config, name string) *agent.AgentSpec {
	for i := range cfg.Agent.Agents {
		if cfg.Agent.Agents[i].Name == name {
			a := cfg.Agent.Agents[i]
			return &agent.AgentSpec{
				Name:         a.Name,
				Provider:     a.Provider,
				Model:        a.Model,
				SystemPrompt: a.SystemPrompt,
				Temperature:  a.Temperature,
				MaxTokens:    a.MaxTokens,
				ToolNames:    a.Tools,
				Timeout:      a.Timeout,
			}
		}
	}
	return nil
}

func toolsToSlice(r *tools.Registry) []agent.Tool {
	defs := r.List()
	out := make([]agent.Tool, 0, len(defs))
	for _, d := range defs {
		if t, _ := r.Get(d.Name); t != nil {
			out = append(out, t)
		}
	}
	return out
}