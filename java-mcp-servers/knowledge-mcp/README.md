# xarch knowledge-mcp (Java)

In-memory knowledge base / RAG server over the MCP stdio protocol.
Mirror of the [Node.js sibling](../node-mcp-servers/knowledge-mcp).

## Quick start

```bash
./gradlew :knowledge-mcp:installDist
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"t","version":"1"}}}' \
  | ./knowledge-mcp/build/install/knowledge-mcp/bin/knowledge-mcp
```

## Tools (9)

| Tool | Description |
|------|-------------|
| `kb_index_document` | Index a document (chunked) |
| `kb_index_file` | Index a file from disk |
| `kb_search` | Keyword-based search (TF-IDF-like) |
| `kb_get_document` | Retrieve document by ID |
| `kb_delete` | Delete document |
| `kb_list` | List indexed documents |
| `kb_update` | Update document |
| `kb_stats` | KB statistics |
| `health` | Health check |

Plus 1 resource (`kb://stats`) and 1 prompt (`rag-search`).

## Chunking & search

- **Chunking**: split by paragraph; if a paragraph exceeds `chunkSize`,
  break into overlapping windows.
- **Search**: case-insensitive token frequency match with normalized
  scores. Deterministic tie-breaker (ordinal then id) for reproducibility.

## License

MIT