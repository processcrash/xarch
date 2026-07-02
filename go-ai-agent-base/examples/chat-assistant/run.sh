#!/usr/bin/env bash
# Convenience launcher: starts the HTTP server with this example's config.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE/../.."

export GOOGLE_API_KEY="${GOOGLE_API_KEY:-}"
[ -z "$GOOGLE_API_KEY" ] && echo "WARN: GOOGLE_API_KEY not set" >&2

go run ./cmd/server --config "$HERE/configs/agent.assistant.yaml"