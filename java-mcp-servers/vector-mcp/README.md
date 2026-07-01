# xarch vector-mcp (Java)

Vector database over the MCP stdio protocol. Mirror of the
[Node.js sibling](../node-mcp-servers/vector-mcp). Reuses the
math from the REST sibling at
`backend/xarch-spring-boot-starter/xarch-mcp/xarch-mcp-vector/`
(DistanceFunction, DistanceMetric, VectorCollection, VectorStore).

## Quick start

```bash
./gradlew :vector-mcp:installDist
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"t","version":"1"}}}' \
  | ./vector-mcp/build/install/vector-mcp/bin/vector-mcp
```

## Tools (9)

| Tool | Description |
|------|-------------|
| `configure` | Configure store (type, default dimension) |
| `create_collection` | Create a new collection |
| `upsert` | Insert/update vectors |
| `search` | KNN search |
| `text_search` | Placeholder (requires LLM embedding) |
| `delete` | Delete by ID |
| `list_collections` | List collections |
| `get_stats` | Collection stats |
| `health` | Health check |

Plus 2 resources (`vector://config`, `vector://collections`) and 2
prompts (`semantic-search`, `similarity-search`).

## Distance metrics

Cosine (default), Euclidean, Dot — selectable per collection via
`create_collection(name, dimension, distance)`.

## Production backends

The default `VectorStore` is in-memory. To swap for Qdrant, Milvus,
pgvector, or any HTTP-backed vector DB, replace `VectorStore` with a
backend that implements the same shape; tools stay the same.

## License

MIT