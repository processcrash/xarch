# mcp-tooling example

Demonstrates bridging the agent to the xarch MCP servers (`database-mcp`,
`filesystem-mcp`) defined in the parent repo at `java-mcp-servers/`.

## What it does

The agent has access to MCP-backed tools (`mcp.database.query`,
`mcp.filesystem.read`, `mcp.filesystem.search`) that proxy calls over
stdio JSON-RPC to the Java MCP servers.

> The MCP bridge code lives in `internal/tools/mcp/` in the main project;
> this example only shows the configuration and usage.

## Files

- `configs/agent.mcp.yaml` — config that points at the MCP servers
- `mcp-servers/` — symlink or copy of the xarch MCP server jars
- `demo.sh` — runs the demo conversation

## Run

```bash
# 1. Build the MCP servers (one-time)
cd ../..  # back to xarch repo
cd java-mcp-servers
./gradlew :database-mcp:installDist :filesystem-mcp:installDist

# 2. Back to this example
cd ../../go-ai-agent-base/examples/mcp-tooling
./demo.sh
```

The demo asks the agent:
- "List tables in the database"
- "Read /etc/hostname from the filesystem"