# vector_mcp

An in-memory vector database exposed via the **Model Context Protocol** (MCP)
over stdio. It lets MCP-capable agents create typed vector collections, insert
and search vectors, and persist everything to disk between sessions.

* **Pure Python** &mdash; only `numpy` plus the official `mcp` SDK are required.
* **Three distance metrics** &mdash; `cosine`, `euclidean`, `dot`.
* **JSON persistence** &mdash; every collection is auto-saved to
  `~/.xarch/vector-mcp/<collection>.json`.
* **Thread-safe** &mdash; the in-memory store is guarded by re-entrant locks.
* **10 MCP tools** covering collection lifecycle, vector CRUD, KNN search and
  health checks.

---

## Features

- Create / list / delete named collections with a fixed dimension and metric.
- Insert single vectors or a batch; each entry has a string id and an
  arbitrary JSON-serialisable metadata dict.
- KNN search returns top-k results with a normalised score in `[0, 1]`
  (`1` is the best possible match).
- Optional metadata equality filter for search (`{"type": "x"}`).
- `vector_get`, `vector_delete`, `vector_count`, `vector_health` for the
  usual read-side ergonomics.
- Automatic load-on-startup and save-on-mutate via the persistence layer.
- Graceful degradation: the database / persistence layers are usable on
  their own even when the `mcp` SDK is not installed (so the test suite can
  run in a minimal environment).

---

## Installation

The package can be installed in editable mode (recommended for development)
or used directly with `python -m vector_mcp` from a checkout.

```bash
# Clone &amp; install (editable)
git clone https://github.com/processcrash/xarch.git
cd xarch/python
pip install -e ./vector_mcp

# Or install dependencies for an existing checkout
cd xarch/python/vector_mcp
pip install -r requirements.txt
```

Python **3.10+** is required. The runtime dependencies are:

- `mcp[cli]>=1.0.0`
- `numpy>=1.24.0`

---

## Running

The server speaks JSON-RPC over **stdio**, so it is normally launched by an
MCP host (e.g. Claude Code, an IDE plugin or any MCP client). To run it
standalone for debugging:

```bash
python -m vector_mcp
```

The server prints nothing to stdout (the wire protocol requires stdout to
remain clean). Diagnostic messages, if any, go to stderr.

---

## Configuration

| Setting         | Environment variable      | Default                          |
| --------------- | ------------------------- | -------------------------------- |
| Storage dir     | `VECTOR_MCP_STORAGE_DIR`  | `~/.xarch/vector-mcp/`           |
| Default metric  | (none &mdash; required)   | `cosine`                         |
| Persistence     | always on                 | atomic JSON write per mutation   |

Override the storage directory when running multiple isolated instances
(e.g. for tests or per-project state):

```bash
export VECTOR_MCP_STORAGE_DIR=/tmp/my-vectors
python -m vector_mcp
```

The directory is created automatically on startup.

---

## Tools

All tools return their result as a JSON object inside the MCP `content`
payload. Domain errors are mapped to JSON-RPC error codes:

| Code    | Meaning                                                    |
| ------- | ---------------------------------------------------------- |
| `-32602`| Invalid params (validation failure, bad metric, ...)       |
| `-32603`| Internal error (persistence, unexpected)                   |
| `-32004`| Collection or vector not found                             |
| `-32005`| Collection already exists                                  |

### 1. `vector_create_collection`
```json
{
  "name": "docs",
  "dimension": 384,
  "distance": "cosine"
}
```
Returns `{ "name", "dimension", "distance", "status": "created" }`.

### 2. `vector_list_collections`
```json
{}
```
Returns `{ "collections": [ { "name", "dimension", "distance", "count" } ] }`.

### 3. `vector_delete_collection`
```json
{ "name": "docs" }
```
Returns `{ "name", "deleted": bool }`.

### 4. `vector_insert`
```json
{
  "collection": "docs",
  "id": "doc-1",
  "vector": [0.1, 0.2, 0.3, ...],
  "metadata": { "source": "web" }
}
```
Returns `{ "id", "status": "inserted" }`.

### 5. `vector_insert_batch`
```json
{
  "collection": "docs",
  "items": [
    { "id": "a", "vector": [0.1, 0.2], "metadata": { "k": 1 } },
    { "id": "b", "vector": [0.3, 0.4] }
  ]
}
```
Returns `{ "count": 2 }`.

### 6. `vector_search`
```json
{
  "collection": "docs",
  "vector": [0.1, 0.2, 0.3, ...],
  "top_k": 5,
  "filter": { "source": "web" }
}
```
Returns `{ "results": [ { "id", "score", "metadata" } ] }`. `filter` is
optional and supports equality match on any metadata key.

### 7. `vector_get`
```json
{ "collection": "docs", "id": "doc-1" }
```
Returns `{ "id", "vector": [...], "metadata": {...} }`.

### 8. `vector_delete`
```json
{ "collection": "docs", "id": "doc-1" }
```
Returns `{ "id", "deleted": bool }`.

### 9. `vector_count`
```json
{ "collection": "docs" }
```
Returns `{ "count": 42 }`.

### 10. `vector_health`
```json
{}
```
Returns `{ "status": "UP", "version", "collections": <int>, "total_vectors": <int> }`.

---

## Architecture

```
+--------------+        stdio JSON-RPC        +-------------------+
|  MCP client  |  <----------------------->  |   vector_mcp      |
|  (host app)  |                              |   server.py       |
+--------------+                              |   - tool router   |
                                             |   - error mapper  |
                                             +---------+---------+
                                                       |
                                  in-process Python    |
                                                       v
                                             +-------------------+
                                             |    VectorDB       |
                                             |    vector_db.py   |
                                             |   - collections   |
                                             |   - KNN search    |
                                             |   - threading.RLock|
                                             +---------+---------+
                                                       |
                                       on every mutation
                                                       v
                                             +-------------------+
                                             |  PersistenceMgr   |
                                             |  persistence.py   |
                                             |  - atomic write   |
                                             |  - load_all       |
                                             +---------+---------+
                                                       |
                                                JSON files
                                                       v
                                       ~/.xarch/vector-mcp/*.json
```

The `vector_db` layer is pure in-memory and uses `numpy` for vectorised
distance computation. The persistence layer is intentionally tiny: one JSON
file per collection, written atomically (`.tmp` + `os.replace`).

---

## Development

Run the test suite from the package directory:

```bash
cd python/vector_mcp
python -m pytest tests/ -v
```

To quickly verify that all source files are syntactically valid:

```bash
python -m compileall -q vector_mcp/
```

The test suite uses only `pytest` and the standard library for its core
tests; one optional test (`TestInMemoryTransport`) requires the official
`mcp` package and is automatically skipped if it is not installed.

Layout:

```
python/vector_mcp/
├── __init__.py
├── __main__.py
├── server.py
├── vector_db.py
├── persistence.py
├── requirements.txt
├── README.md
└── tests/
    ├── __init__.py
    ├── test_vector_db.py
    ├── test_distance.py
    ├── test_persistence.py
    └── test_server.py
```

---

## License

MIT &mdash; see the project root for the full license text.
