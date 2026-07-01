# go-ai-agent-base

> Enterprise-grade AI Agent development platform foundation, built on
> Google's [ADK-Go](https://github.com/google/adk-go) framework and the
> Go ecosystem.

Build production-ready, observable, multi-LLM agents in Go — with
HTTP API, session persistence, MCP tool bridging, and a CLI for local
dev — all in a single `go install` away.

---

## ✨ Why this project?

The AI agent ecosystem is fragmented:
- Python ADK has Google ADK, LangChain, LlamaIndex — but productionizing
  them in Python means dealing with GIL, packaging, deployment surprises.
- Go has excellent runtimes (low latency, fast cold start, single static
  binary), but **no coherent agent framework** that handles the
  enterprise concerns: auth, sessions, observability, multi-LLM,
  MCP tools, deployment.

`go-ai-agent-base` fills that gap: **a Google ADK-Go based foundation
that gets you to "production-shaped" in minutes**.

## 🎯 What's inside

| Concern | Implementation |
|---------|----------------|
| Agent runtime | `google/adk-go` core, wrapped in `internal/agent` |
| LLM providers | Gemini / OpenAI / Anthropic pluggable via `internal/llm` |
| Tools | Custom Go functions + MCP bridge via `internal/tools` |
| Memory | In-memory (dev) / Redis / Postgres via `internal/memory` |
| Sessions | Stateless / Redis-backed via `internal/session` |
| HTTP API | Gin REST + SSE streaming via `internal/server` |
| CLI | Cobra with `serve`, `agent`, `eval`, `migrate` commands |
| Auth | JWT (HS256 / RS256) middleware |
| Observability | OTel traces + Prometheus metrics + Zap structured logs |
| Configuration | YAML + env vars (Viper) with hot reload |
| Deployment | Multi-stage Docker, K8s manifests, Helm chart |
| Testing | Unit + integration (testcontainers-go) |

## 🚀 60-second quick start

```bash
# 1. Install
git clone https://github.com/xarch/go-ai-agent-base
cd go-ai-agent-base
go mod download

# 2. Configure (LLM key, optional Redis/Postgres)
cp configs/agent.example.yaml configs/agent.yaml
$EDITOR configs/agent.yaml   # add GOOGLE_API_KEY / OPENAI_API_KEY

# 3. Run the CLI agent (single user, in-memory)
export GOOGLE_API_KEY=...
go run ./cmd/agent chat --agent assistant --config configs/agent.yaml

# 4. Or run the HTTP server (multi-user)
go run ./cmd/server --config configs/agent.yaml
# then POST /api/v1/agents/assistant/sessions/{id}/messages
```

## 📦 Architecture

```
                       ┌─────────────────────┐
                       │   HTTP clients       │
                       │  (curl / SDK / UI)    │
                       └──────────┬──────────┘
                                  │
                       ┌──────────▼──────────┐
                       │   Gin HTTP server    │
                       │  /api/v1/agents/...  │
                       │  + SSE streaming     │
                       │  + JWT middleware    │
                       └──────────┬──────────┘
                                  │
                       ┌──────────▼──────────┐
                       │   Agent Runtime      │
                       │  ┌────────────────┐ │
                       │  │  LLM Provider   │ │  ← Gemini / OpenAI / Anthropic
                       │  │  (pluggable)    │ │
                       │  └────────────────┘ │
                       │  ┌────────────────┐ │
                       │  │  Tool Registry  │ │  ← custom + MCP bridge
                       │  └────────────────┘ │
                       │  ┌────────────────┐ │
                       │  │  Memory         │ │  ← in-mem / Redis / Postgres
                       │  └────────────────┘ │
                       │  ┌────────────────┐ │
                       │  │  Session Store  │ │  ← in-mem / Redis
                       │  └────────────────┘ │
                       └──────────┬──────────┘
                                  │
        ┌────────────────┬─────────┴────────┬────────────────┐
        ▼                ▼                  ▼                ▼
   PostgreSQL         Redis            Prometheus        OTel Collector
   (sessions /       (sessions /       (metrics)         (traces)
    memories)         cache)
```

## 📜 Examples

- `examples/chat-assistant` — single-agent Gemini chat with personality
- `examples/rag-agent` — RAG over a vector store
- `examples/mcp-tooling` — agent that calls MCP servers (database-mcp, filesystem-mcp)
- `examples/multi-agent` — orchestrator + sub-agents
- `examples/code-reviewer` — reads GitHub PR, runs review tools

## 🛠️ Tech stack

| Layer | Tech |
|-------|------|
| Agent framework | [google/adk-go](https://github.com/google/adk-go) |
| Language | Go 1.23+ |
| HTTP | Gin |
| CLI | Cobra + Viper |
| Logging | Uber Zap (structured JSON) |
| Tracing | OpenTelemetry |
| Metrics | Prometheus |
| Auth | JWT (golang-jwt v5) |
| Session/Memory | In-memory / Redis / Postgres |
| Testing | Testify + Testcontainers |
| Lint | golangci-lint |

## 📚 Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — design rationale
- [docs/QUICKSTART.md](docs/QUICKSTART.md) — step-by-step setup
- [docs/API.md](docs/API.md) — REST API reference
- [docs/MCP.md](docs/MCP.md) — wiring to MCP servers
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) — Docker / K8s / Helm

## 🤝 Contributing

Issues, PRs, and discussions welcome. See [CONTRIBUTING.md](CONTRIBUTING.md)
for the workflow (we follow a "discuss before code" pattern).

## 📄 License

[MIT](LICENSE)

---

> Built with care by the xarch team. ⭐ star us if this saves you
> engineering time on your next agent platform.