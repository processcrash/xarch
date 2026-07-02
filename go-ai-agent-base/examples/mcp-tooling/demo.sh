#!/usr/bin/env bash
# Demo for the mcp-tooling example.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE/../.."

export OPENAI_API_KEY="${OPENAI_API_KEY:-}"
[ -z "$OPENAI_API_KEY" ] && echo "WARN: OPENAI_API_KEY not set" >&2

echo "Starting agent on http://localhost:8081 ..."
go run ./cmd/agent chat --agent dataops --config "$HERE/configs/agent.mcp.yaml"