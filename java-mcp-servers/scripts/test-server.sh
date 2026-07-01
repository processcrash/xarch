#!/usr/bin/env bash
# ===============================================
# test-server.sh — Smoke-test a single MCP server
# ===============================================
# Sends a minimal initialize request + tools/list to the given server
# and prints the responses. Exits 0 on success, 1 on failure.
#
# Usage:
#   ./test-server.sh <server-name>
#   ./test-server.sh database-mcp
#   ./test-server.sh vector-mcp

set -euo pipefail

SERVER="${1:-database-mcp}"
HERE="$(cd "$(dirname "$0")/.." && pwd)"

JAR=$(ls "$HERE/$SERVER/build/install/$SERVER/lib/"*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
    echo "ERROR: built jar not found. Run ./gradlew :$SERVER:installDist first." >&2
    exit 1
fi

echo "==> Testing $SERVER (jar: $JAR)"

INIT='{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"0.0.1"}}}'
LIST='{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
PING='{"jsonrpc":"2.0","id":3,"method":"ping"}'

(
    echo "$INIT"
    echo "$LIST"
    echo "$PING"
) | java -jar "$JAR" 2>/dev/null

EXIT=$?
if [ $EXIT -ne 0 ]; then
    echo "ERROR: server exited with code $EXIT" >&2
    exit 1
fi
echo ""
echo "==> OK: $SERVER responded to initialize, tools/list, ping"
