# Quickstart

> Get a working agent running in 5 minutes.

## 0. Prerequisites

- Go 1.23+
- An LLM API key (Google Gemini, OpenAI, Anthropic, or Ollama running locally)
- Docker (only for the dev docker-compose stack)

## 1. Install

```bash
git clone https://github.com/xarch/go-ai-agent-base
cd go-ai-agent-base
go mod download
```

## 2. Configure

```bash
cp configs/agent.example.yaml configs/agent.yaml
$EDITOR configs/agent.yaml   # set GOOGLE_API_KEY / OPENAI_API_KEY / etc.
```

The example config defines a single agent named `assistant` with the
two built-in tools `current_time` and `calculator`.

## 3. Run the CLI

```bash
export GOOGLE_API_KEY=sk-...
go run ./cmd/agent chat --agent assistant --config configs/agent.yaml
```

```
xarch agent chat — agent=assistant  session=<uuid>
(type /exit to quit)

you> What time is it in Tokyo?
assistant> It is 2026-07-01T21:30:00+09:00 in Asia/Tokyo.
  [tokens: 32 prompt + 14 completion = 46, dur=812ms]

you> What is 12*34?
assistant> 408
  [tokens: 28 prompt + 4 completion = 32, dur=540ms]

you> /exit
```

## 4. Run the HTTP server

```bash
go run ./cmd/server --config configs/agent.yaml
# 2026-07-01T12:34:56Z INFO starting server version=0.1.0 agents=1
# 2026-07-01T12:34:56Z INFO HTTP server listening addr=0.0.0.0:8080
```

Mint a JWT and call it:

```bash
# Use any JWT signed with the configured secret (cfg.auth.jwt.secret)
TOKEN=$(go run ./cmd/agent devtoken)        # helper that mints a dev token (HS256)
# Or use jwt.io to craft one manually.

curl -X POST http://localhost:8080/api/v1/agents \
     -H "Authorization: Bearer $TOKEN" | jq

curl -X POST http://localhost:8080/api/v1/agents/assistant/sessions \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"user_id":"alice"}' | jq

SID=...
curl -X POST http://localhost:8080/api/v1/agents/assistant/sessions/$SID/messages \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"content":"What is 2+2?"}' | jq
```

## 5. Stream responses (SSE)

```bash
curl -N -X POST http://localhost:8080/api/v1/agents/assistant/sessions/$SID/stream \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"content":"Tell me a story"}'

event: message
data: {"type":"message","content":"Once upon a time..."}

event: message
data: {"type":"message","content":" there was a small Go binary..."}

event: done
data: {"type":"done"}
```

## 6. Run with docker-compose

```bash
export GOOGLE_API_KEY=sk-...
docker compose -f deployments/docker/docker-compose.yml up -d
# Server running on http://localhost:8080
# Prometheus: http://localhost:9090
# Grafana:   http://localhost:3000 (admin/admin)
# OTel:      OTLP on 4317/4318
```

## 7. Deploy to Kubernetes

```bash
kubectl apply -f deployments/k8s/
kubectl -n gaab port-forward svc/gaab-server 8080:8080
```

For production, use the Helm chart at `deploy/helm/`.

## 8. Next steps

- See `docs/ARCHITECTURE.md` for the full design
- See `examples/` for end-to-end recipes
- Read the source — start with `internal/agent/runtime.go`