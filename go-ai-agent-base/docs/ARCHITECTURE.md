# Architecture

## Why this exists

The Go AI agent ecosystem is sparse. Python has Google ADK, LangChain,
LlamaIndex — but productionizing them in Python brings GIL pain, cold
start latency, and packaging headaches. Go gives us excellent runtimes
(small binaries, fast start, simple deploys) but no coherent framework
that handles enterprise concerns: auth, sessions, observability, multi-LLM,
MCP tools, deployment.

`go-ai-agent-base` is the foundation. It builds on top of
[google/adk-go](https://github.com/google/adk-go) and packages the
concerns every production team ends up writing themselves.

## High-level architecture

```
                       ┌─────────────────────┐
                       │   Clients             │
                       │  (curl / SDK / UI)    │
                       └──────────┬──────────┘
                                  │ HTTPS + SSE
                       ┌──────────▼──────────┐
                       │   Gin HTTP server    │
                       │  (internal/server)   │
                       │  - auth middleware   │
                       │  - request logger    │
                       │  - metrics recorder  │
                       │  - CORS              │
                       └──────────┬──────────┘
                                  │
                       ┌──────────▼──────────┐
                       │   Agent Runtime      │
                       │  (internal/agent)    │
                       │  ┌────────────────┐ │
                       │  │  LLM Provider   │ │  ← Gemini / OpenAI / Anthropic / Ollama
                       │  │  (pluggable)    │ │
                       │  └────────────────┘ │
                       │  ┌────────────────┐ │
                       │  │  Tool Registry  │ │  ← builtins + MCP bridge
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

## Why adk-go

We chose `google/adk-go` as the runtime substrate because:

1. **Vendor-grade** — Google uses it internally for Gemini tooling; battle-tested.
2. **Provider-pluggable** — same Agent interface works with Gemini, OpenAI, Anthropic, Ollama.
3. **Multi-agent ready** — orchestrator + sub-agent pattern out of the box.
4. **Tool framework** — function tools, MCP tools, agent-as-tool.
5. **Memory** — long-term + working memory abstractions.
6. **Open spec** — Model Context Protocol (MCP) compatible.

We wrap adk-go in a thin layer (see `internal/agent/runtime.go`) so the
rest of the codebase doesn't import it directly. This lets us swap the
underlying agent engine without touching HTTP/CLI/SDK code.

## Key design decisions

### Canonical types

The package `internal/agent/types.go` defines the platform's canonical
types: `Message`, `ToolCall`, `Response`, `Event`, etc. Everything
else (LLM providers, tools, memory, sessions) implements these
interfaces. The adk-go types are adapted internally.

### Pluggable backends

Every long-running concern (memory, session) has a factory function
that picks a backend from config:

```go
mem, err := memory.Factory(cfg.Memory.Backend, opts) // memory | redis | postgres
sessions, err := session.Factory(cfg.Session.Backend, ...)  // memory | redis
```

Production wires Redis/Postgres; dev falls back to in-memory.

### LLM provider isolation

Each provider (Gemini, OpenAI, Anthropic, Ollama) lives in its own
file under `internal/llm/` and implements the same `agent.LLM`
interface. New providers are added by implementing 4 methods
(Name, Generate, Stream, doRequest).

### Tool framework

Built-in tools (`current_time`, `calculator`, `http_fetch`) are
registered at startup. MCP-bridged tools are loaded lazily when the
runtime sees an MCP server reference in `cfg.mcp.servers`. Each
becomes a callable `agent.Tool` that proxies over stdio JSON-RPC.

### Auth

JWT (HS256 or RS256) with optional static API keys. JWTs are
verified at the Gin middleware layer; downstream handlers read
claims via `c.Get(auth.CtxClaims)`.

### Observability

Three pillars, configured independently:

- **Logging**: Zap with json/text format
- **Tracing**: OTel with OTLP or stdout exporter
- **Metrics**: Prometheus with 9 collectors covering HTTP,
  agent, LLM, tools, errors, sessions

### Streaming

Agent runs expose their progress through `agent.Emitter`. The HTTP
layer maps these to SSE events with `Content-Type: text/event-stream`.
Each event has a `type` (message, tool_call, tool_result, error, done)
and a JSON body.

## Concurrency model

- HTTP server: each request runs in its own goroutine, handled by Gin.
- Agent loop: tool calls execute concurrently with goroutine fan-out
  (`sync.WaitGroup`), then results are appended in order.
- SSE writer: the emitter's `http.Flusher` flushes after each event;
  no buffering by intermediate proxies (sets `X-Accel-Buffering: no`).
- LLM calls: `context.Context`-bound, respect server timeouts.

## Data flow

A single agent invocation:

1. HTTP request arrives → auth middleware verifies JWT → handler parses body
2. `Runtime.Run(ctx, opts, emitter)` is called
3. Session is loaded (from in-memory map or Redis)
4. Long-term memory is searched for relevant facts → appended to system prompt
5. User message is appended to session
6. Provider is looked up by name
7. Agent loop (max N iterations):
   a. LLM is called with full history + tool definitions
   b. If response has tool calls → execute them concurrently → append results
   c. Else → break
8. Final response is persisted to session and returned
9. Metrics updated (agent_runs_total, llm_tokens_total, etc.)
10. Span closed; logs flushed

## Where to start reading the code

If you're new to the codebase, read these files in order:

1. `configs/agent.example.yaml` — full configuration reference
2. `internal/config/config.go` — typed config + loading
3. `internal/agent/types.go` — canonical types
4. `internal/agent/runtime.go` — execution loop
5. `internal/server/server.go` — HTTP handlers
6. `cmd/server/main.go` — entry point that wires it all together

## Comparison with alternatives

| Concern | LangChain (Py) | ADK (Py) | Semantic Kernel (.NET) | **go-ai-agent-base** |
|---------|----------------|----------|------------------------|---------------------|
| Multi-LLM | Yes | Yes | Yes | Yes |
| Auth | DIY | DIY | AD built-in | **Built-in JWT + API keys** |
| Session store | DIY | Built-in | Built-in | **Pluggable in-mem/Redis** |
| Memory | DIY | Built-in | Built-in | **Pluggable in-mem/Redis/PG** |
| MCP tools | Partial | Yes | No | **Yes (via adk-go)** |
| HTTP streaming | DIY | Yes | Yes | **SSE out of the box** |
| Metrics | DIY | DIY | AD | **Prometheus + 9 collectors** |
| Tracing | OTel | OTel | App Insights | **OTel SDK with OTLP/Zipkin** |
| Cold start | ~1s (Python) | ~1s | ~500ms (CLR) | **<50ms (Go)** |
| Static binary | No | No | No | **Yes (distroless ~15MB)** |

## Future

- Multi-agent orchestration (sub-agents as tools)
- Vector store integration (Qdrant, Milvus, pgvector)
- Streaming tool results for large outputs
- WASM-based sandboxing for untrusted tool code
- Real-time voice / video input via WebRTC