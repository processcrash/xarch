# chat-assistant example

A minimal single-agent chat assistant powered by Gemini. Demonstrates:

- One configured agent (`assistant`) with built-in `current_time` + `calculator` tools
- CLI usage (`agent chat --agent assistant`)
- HTTP usage (`server` + `curl`)

## Run

```bash
cd go-ai-agent-base
export GOOGLE_API_KEY=sk-...

# CLI
go run ./cmd/agent chat --agent assistant --config configs/agent.example.yaml
# > you> What time is it in Tokyo?
# > assistant> It is 2026-07-01T21:30:00+09:00 in Asia/Tokyo.
# > > [tokens: 32 prompt + 14 completion = 46, dur=812ms]

# HTTP
go run ./cmd/server --config configs/agent.example.yaml &
curl -X POST http://localhost:8080/api/v1/agents/assistant/sessions \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"user_id":"alice"}'
# {"session_id":"...","user_id":"alice","agent":"assistant"}

curl -X POST http://localhost:8080/api/v1/agents/assistant/sessions/<sid>/messages \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"content":"What is 12*34?"}'
# {"message":{"role":"assistant","content":"408"}, ...}
```

## Files

- `configs/agent.assistant.yaml` — minimal agent config (copy of
  the top-level `configs/agent.example.yaml` with a single agent)
- `run.sh` — helper to start the server with this config
- `e2e.sh` — curl-based smoke test (start server first)