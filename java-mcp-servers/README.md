# java-mcp-servers

<div align="center">

**Java implementations of the xarch MCP servers, speaking the same stdio JSON-RPC 2.0 / MCP 2024-11-05 protocol as the Node.js and Python siblings.**

Drop-in alternatives for Claude Desktop, Cursor, or any MCP-compatible client.

</div>

---

## Why a Java stdio implementation?

The xarch project ships MCP servers in **three** languages:

| Language | When to use | Where |
|----------|-------------|-------|
| **Node.js / TypeScript** | JS-first teams, Claude Desktop | `node-mcp-servers/` |
| **Python** | Python-first teams, AI research | `py-mcp-servers/` |
| **Java** | JVM-first teams, tight integration with Spring services | `java-mcp-servers/` ← you are here |

The protocol is **identical** (MCP 2024-11-05 stdio transport), so you can swap implementations without changing client config.

## Servers

| Server | Tools | Use for |
|--------|-------|---------|
| `database-mcp` | configure, query_execute, execute_update, schema_get, table_list, table_describe, index_list, health | SQL queries, schema inspection |
| `knowledge-mcp` | kb_index_document, kb_index_file, kb_search, kb_get_document, kb_delete, kb_list, kb_update, kb_stats, health | RAG, document search |
| `filesystem-mcp` | list_directory, read_file, write_file, delete, create_directory, search_files, get_file_info, copy_file, move_file, health | File ops with path-traversal protection |
| `vector-mcp` | configure, create_collection, upsert, search, text_search, delete, list_collections, get_stats, health | Vector DB, KNN search |

## Quick start

### Build

```bash
cd java-mcp-servers
./gradlew installDist
```

### Run

```bash
./database-mcp/build/install/database-mcp/bin/database-mcp
# or
java -jar database-mcp/build/install/database-mcp/lib/database-mcp-1.0.0.jar
```

### Smoke test

```bash
./scripts/test-server.sh database-mcp
```

### Wire into Claude Desktop

See `claude-desktop-config.json` for an example. Drop it into your Claude Desktop config (or merge into the existing one).

## Module layout

```
java-mcp-servers/
├── mcp-runtime/         # shared StdioMcpServer + Logging (~300 LOC)
├── database-mcp/        # 8 tools + 1 resource + 1 prompt
├── knowledge-mcp/       # 9 tools + 1 resource + 1 prompt
├── filesystem-mcp/      # 10 tools + 2 resources + 1 prompt + PathGuard
├── vector-mcp/          # 9 tools + 2 resources + 2 prompts (math reused from xarch-mcp-vector)
├── scripts/             # build-all.sh, test-server.sh
└── claude-desktop-config.json
```

## Architecture

```
Claude Desktop / Cursor / any MCP client
        │
        │ stdio (JSON-RPC 2.0)
        ▼
┌──────────────────────────────┐
│  java-mcp-servers           │
│                              │
│  StdioMcpServer (runtime)    │
│        ↓                     │
│  Tool / Resource / Prompt    │
│        ↓                     │
│  In-memory store / FS / etc. │
└──────────────────────────────┘
```

- **stdio transport** — server reads from stdin, writes to stdout, logs to stderr
- **No HTTP** — zero config, no port, no auth
- **Single-process** — pure JDK 25 + Jackson + SLF4J
- **Thread-safe** — multiple tools can be invoked concurrently

## vs HTTP MCP servers

| | stdio (this) | HTTP (`xarch-mcp-*`) |
|--|--------------|---------------------|
| Protocol | JSON-RPC 2.0 | REST |
| Deployment | local / AI client | K8s / production |
| Auth | none (local trust) | Sa-Token / OAuth |
| Discovery | none | Nacos |
| Use for | Claude Desktop, Cursor, dev | cross-service, cross-language |

The HTTP versions (in `backend/xarch-spring-boot-starter/xarch-mcp/`) have the **same tool names and parameters** — only the transport differs. You can switch from local stdio to deployed HTTP without changing LLM-side code.

## Development

```bash
# Run tests
./gradlew test

# Build all distributables
./gradlew installDist

# Smoke-test a server
./scripts/test-server.sh vector-mcp

# Live-reload a single server in dev
./gradlew :database-mcp:run
```

## See also

- [Node.js MCP servers](../node-mcp-servers/)
- [Python MCP servers](../py-mcp-servers/)
- [HTTP MCP servers (production Java + Spring)](../backend/xarch-spring-boot-starter/xarch-mcp/)
- [MCP guide](../docs/MCP_GUIDE.md)
- [MCP Java guide](../docs/MCP_JAVA_GUIDE.md)
- [MCP specification](https://spec.modelcontextprotocol.io/)

## License

MIT
