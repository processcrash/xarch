# REST API reference

Base URL: `http://<host>:<port>/api/v1`

All endpoints (except `/health` and `/metrics`) require auth:
- `Authorization: Bearer <jwt>` OR
- `X-API-Key: <key>`

## Health

### `GET /health`

Liveness + readiness. Always public.

**Response 200**
```json
{
  "status": "UP",
  "service": "go-ai-agent-base",
  "version": "0.1.0",
  "agents": ["assistant", "coder"],
  "tools": ["current_time", "calculator"],
  "timestamp": "2026-07-01T12:34:56Z"
}
```

## Agents

### `GET /agents`

List configured agent names.

**Response 200**
```json
{ "agents": ["assistant", "coder"] }
```

### `POST /agents/{name}/sessions`

Open a new conversation session.

**Request body**
```json
{ "user_id": "alice" }
```
`user_id` defaults to the JWT subject if omitted.

**Response 201**
```json
{
  "session_id": "9c2b...",
  "user_id": "alice",
  "agent": "assistant"
}
```

### `GET /agents/{name}/sessions/{sid}/messages`

List messages in a session.

**Response 200**
```json
{
  "session_id": "9c2b...",
  "messages": [
    {"role":"user", "content":"hi"},
    {"role":"assistant", "content":"hello"}
  ]
}
```

### `POST /agents/{name}/sessions/{sid}/messages`

Run a single agent turn synchronously.

**Request body**
```json
{
  "content": "What time is it?",
  "metadata": {"client": "web"}
}
```

**Response 200**
```json
{
  "message": {
    "role": "assistant",
    "content": "It is 2026-07-01T12:34:56Z in UTC.",
    "tool_calls": []
  },
  "usage": {
    "prompt_tokens": 32,
    "completion_tokens": 14,
    "total_tokens": 46
  },
  "stop_reason": "stop",
  "duration": "812ms"
}
```

### `POST /agents/{name}/sessions/{sid}/stream`

Run a turn with Server-Sent Events streaming.

**Response 200 (text/event-stream)**
```
event: message
data: {"type":"message","content":"It is..."}

event: tool_call
data: {"type":"tool_call","tool_call":{"id":"...","name":"current_time"}}

event: tool_result
data: {"type":"tool_result","tool_result":{...}}

event: message
data: {"type":"message","content":" 12:34:56 UTC"}

event: done
data: {"type":"done"}
```

### `DELETE /agents/{name}/sessions/{sid}`

Delete a session.

**Response 200**
```json
{ "deleted": "9c2b..." }
```

## Metrics

### `GET /metrics`

Prometheus exposition. Public by default (gated by
`observability.metrics.enabled`).

## Error format

All errors return JSON with `error` key:

```json
{ "error": "invalid token: token expired" }
```

Status codes:
- `400` — invalid request body / parameters
- `401` — missing or invalid auth
- `403` — authorized but missing required scope
- `404` — agent or session not found
- `429` — rate limited
- `500` — LLM / tool / runtime error
- `503` — upstream provider unreachable

## SDK

Go clients should use `pkg/sdk`:

```go
import "github.com/xarch/go-ai-agent-base/pkg/sdk"

c := sdk.New("http://localhost:8080", sdk.WithToken(jwt))
agents, err := c.ListAgents(ctx)
resp, err := c.Send(ctx, "assistant", sessionID, "Hello")
fmt.Println(resp.Message.Content, resp.Usage.TotalTokens)
```

For other languages, hit the REST API directly — the JSON shape is
documented above and stable within a major version.