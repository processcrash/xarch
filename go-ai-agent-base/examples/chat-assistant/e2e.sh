#!/usr/bin/env bash
# Smoke test for the chat-assistant example. Requires the server running
# on http://localhost:8080 (use run.sh).
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"

echo "==> Health"
curl -fs "$BASE/api/v1/health" | tee /tmp/health.json
echo

echo "==> Create session"
SID=$(curl -fs -X POST "$BASE/api/v1/agents/assistant/sessions" \
        -H "Content-Type: application/json" \
        -d '{"user_id":"e2e"}' | sed -n 's/.*"session_id":"\([^"]*\)".*/\1/p')
echo "session: $SID"

echo "==> Send message"
curl -fs -X POST "$BASE/api/v1/agents/assistant/sessions/$SID/messages" \
        -H "Content-Type: application/json" \
        -d '{"content":"What is 12*34?"}'
echo

echo "==> Send stream (SSE)"
curl -fs -X POST "$BASE/api/v1/agents/assistant/sessions/$SID/stream" \
        -H "Accept: text/event-stream" -H "Content-Type: application/json" \
        -d '{"content":"What time is it?"}' \
        | head -10