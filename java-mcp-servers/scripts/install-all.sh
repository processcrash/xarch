#!/usr/bin/env bash
# ===============================================
# install-all.sh — Install java-mcp-servers to /opt/xarch/mcp
# ===============================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")"" && pwd)"
TARGET="${XARCH_INSTALL_DIR:-/opt/xarch/mcp}"

echo "==> Installing java-mcp-servers to $TARGET"
mkdir -p "$TARGET"

for server in database-mcp knowledge-mcp filesystem-mcp vector-mcp; do
    BIN="$HERE/$server/build/install/$server/bin/$server"
    if [ -x "$BIN" ]; then
        cp -r "$HERE/$server/build/install/$server" "$TARGET/$server"
        chmod +x "$TARGET/$server/bin/$server"
        echo "  -> installed $server"
    else
        echo "  -> SKIP $server (build first: ./gradlew :$server:installDist)" >&2
    fi
done

echo ""
echo "==> Done. Configure your MCP client to use:"
echo "    $TARGET/database-mcp/bin/database-mcp"
echo "    $TARGET/knowledge-mcp/bin/knowledge-mcp"
echo "    $TARGET/filesystem-mcp/bin/filesystem-mcp"
echo "    $TARGET/vector-mcp/bin/vector-mcp"