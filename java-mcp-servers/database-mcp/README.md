# xarch database-mcp (Java)

Java stdio MCP server for database operations. Drop-in alternative to
the [Node.js sibling](../node-mcp-servers/database-mcp) for teams
running Java/JVM toolchains.

## Quick start

```bash
# from java-mcp-servers/
./gradlew :database-mcp:installDist
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"t","version":"1"}}}' \
  | ./database-mcp/build/install/database-mcp/bin/database-mcp
```

## Tools (8)

| Tool | Description |
|------|-------------|
| `configure` | Configure database connection |
| `query_execute` | Execute a read-only SQL query |
| `execute_update` | Execute INSERT/UPDATE/DELETE |
| `schema_get` | Get full database schema |
| `table_list` | List all tables |
| `table_describe` | Describe a specific table |
| `index_list` | List indexes for a table |
| `health` | Health check |

Plus 1 resource (`config://current`) and 1 prompt (`sql-query`).

## Notes on the database client

`DatabaseClient` is a deliberately driver-free stub that returns
consistent mock data. To switch to a real DB, add the JDBC driver to
`build.gradle.kts` and replace the body of each method (the contract
stays the same). See the class javadoc for the swap-in recipe.

## Claude Desktop config

```json
{
  "mcpServers": {
    "xarch-database": {
      "command": "java",
      "args": ["-jar", "/path/to/database-mcp/build/install/database-mcp/lib/database-mcp-1.0.0.jar"]
    }
  }
}
```

## License

MIT