"""
MCP server implementation for the vector_mcp package.

The server wires up the in-memory :class:`VectorDB` and the optional
:class:`PersistenceManager` and exposes 10 tools over the Model Context
Protocol's stdio JSON-RPC transport.

The implementation tries to use the official ``mcp`` package (``mcp.server``
and ``mcp.server.stdio``) when it is installed. When the package is not
available, the module still imports so that the database / persistence
layers can be exercised in tests.
"""

from __future__ import annotations

import asyncio
import json
import os
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional

from . import __version__
from .errors import (
    CollectionAlreadyExistsError,
    CollectionNotFoundError,
    DimensionMismatchError,
    InvalidDistanceMetricError,
    PersistenceError,
    VectorMCPError,
    VectorNotFoundError,
)
from .persistence import (
    STORAGE_DIR_ENV,
    PersistenceManager,
    default_storage_dir,
)
from .vector_db import VectorDB


# ---------------------------------------------------------------------------
# MCP SDK imports
# ---------------------------------------------------------------------------
try:  # pragma: no cover - import is environment dependent
    from mcp.server import Server
    from mcp.server.stdio import stdio_server
    from mcp.types import Tool
    _MCP_AVAILABLE = True
except Exception:  # pragma: no cover - degrade gracefully for tests
    Server = None  # type: ignore[assignment]
    stdio_server = None  # type: ignore[assignment]
    Tool = None  # type: ignore[assignment]
    _MCP_AVAILABLE = False


# JSON-RPC error codes used by the server layer.
ERR_INVALID_PARAMS = -32602
ERR_INTERNAL = -32603
ERR_NOT_FOUND = -32004  # application-defined "not found"
ERR_ALREADY_EXISTS = -32005  # application-defined "conflict"


# ---------------------------------------------------------------------------
# Tool definitions
# ---------------------------------------------------------------------------
def _tool_definitions() -> List[Dict[str, Any]]:
    """Return the tool schema list (in MCP wire format)."""
    return [
        {
            "name": "vector_create_collection",
            "description": (
                "Create a new vector collection with the given dimension and "
                "distance metric. Supported metrics: cosine, euclidean, dot."
            ),
            "inputSchema": {
                "type": "object",
                "properties": {
                    "name": {"type": "string", "description": "Collection name"},
                    "dimension": {
                        "type": "integer",
                        "minimum": 1,
                        "description": "Vector dimension",
                    },
                    "distance": {
                        "type": "string",
                        "enum": ["cosine", "euclidean", "dot"],
                        "description": "Distance metric",
                    },
                },
                "required": ["name", "dimension", "distance"],
            },
        },
        {
            "name": "vector_list_collections",
            "description": "List all collections with their stats.",
            "inputSchema": {"type": "object", "properties": {}},
        },
        {
            "name": "vector_delete_collection",
            "description": "Delete an entire collection and its persisted file.",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "name": {"type": "string", "description": "Collection name"},
                },
                "required": ["name"],
            },
        },
        {
            "name": "vector_insert",
            "description": "Insert a single vector with optional metadata.",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "collection": {"type": "string"},
                    "id": {"type": "string"},
                    "vector": {
                        "type": "array",
                        "items": {"type": "number"},
                    },
                    "metadata": {"type": "object"},
                },
                "required": ["collection", "id", "vector"],
            },
        },
        {
            "name": "vector_insert_batch",
            "description": "Insert a batch of vectors into a collection.",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "collection": {"type": "string"},
                    "items": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "id": {"type": "string"},
                                "vector": {
                                    "type": "array",
                                    "items": {"type": "number"},
                                },
                                "metadata": {"type": "object"},
                            },
                            "required": ["id", "vector"],
                        },
                    },
                },
                "required": ["collection", "items"],
            },
        },
        {
            "name": "vector_search",
            "description": (
                "KNN search. Returns up to top_k results sorted by score "
                "(normalised to [0, 1] where 1 is the best match)."
            ),
            "inputSchema": {
                "type": "object",
                "properties": {
                    "collection": {"type": "string"},
                    "vector": {"type": "array", "items": {"type": "number"}},
                    "top_k": {"type": "integer", "minimum": 1, "default": 10},
                    "filter": {
                        "type": "object",
                        "description": "Optional metadata equality filter",
                    },
                },
                "required": ["collection", "vector"],
            },
        },
        {
            "name": "vector_get",
            "description": "Retrieve a single vector and its metadata by id.",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "collection": {"type": "string"},
                    "id": {"type": "string"},
                },
                "required": ["collection", "id"],
            },
        },
        {
            "name": "vector_delete",
            "description": "Delete a single vector by id from a collection.",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "collection": {"type": "string"},
                    "id": {"type": "string"},
                },
                "required": ["collection", "id"],
            },
        },
        {
            "name": "vector_count",
            "description": "Return the number of vectors in a collection.",
            "inputSchema": {
                "type": "object",
                "properties": {"collection": {"type": "string"}},
                "required": ["collection"],
            },
        },
        {
            "name": "vector_health",
            "description": "Health check / status of the MCP server.",
            "inputSchema": {"type": "object", "properties": {}},
        },
    ]


# ---------------------------------------------------------------------------
# Server class
# ---------------------------------------------------------------------------
class VectorMCPServer:
    """MCP server wrapping a :class:`VectorDB` and a :class:`PersistenceManager`."""

    def __init__(
        self,
        db: Optional[VectorDB] = None,
        persistence: Optional[PersistenceManager] = None,
    ) -> None:
        self.db = db or VectorDB()
        env_dir = os.environ.get(STORAGE_DIR_ENV)
        self.persistence = persistence or PersistenceManager(
            storage_dir=Path(env_dir) if env_dir else None
        )
        # Load any persisted collections on startup.
        try:
            self.persistence.load_into(self.db)
        except PersistenceError:
            # Don't crash startup on a corrupted file - the operator can fix.
            pass

    # ------------------------------------------------------------------
    # Tool dispatch
    # ------------------------------------------------------------------
    async def call_tool(self, name: str, arguments: Dict[str, Any]) -> Any:
        """Dispatch a tool call to the right handler and persist as needed."""
        handler = _HANDLERS.get(name)
        if handler is None:
            raise ValueError(f"Unknown tool: {name}")
        result = await handler(self, arguments or {})
        self._maybe_persist(name)
        return result

    def _maybe_persist(self, tool_name: str) -> None:
        """Persist a single collection after a mutation tool call."""
        # We don't have the collection name here cheaply for all tools, so
        # we just snapshot the whole DB on every mutation. This is simple
        # and safe; for very large datasets callers can override this.
        if tool_name in _MUTATING_TOOLS:
            try:
                self.persistence.save_db(self.db)
            except PersistenceError:
                # Swallow persistence errors at runtime - they shouldn't break
                # the in-memory tool call.
                pass

    # ------------------------------------------------------------------
    # Tool implementations
    # ------------------------------------------------------------------
    async def _create_collection(self, args: Dict[str, Any]) -> Dict[str, Any]:
        name = self._require_str(args, "name")
        dim = self._require_int(args, "dimension", min_value=1)
        dist = self._require_str(args, "distance")
        if dist not in ("cosine", "euclidean", "dot"):
            raise InvalidDistanceMetricError(dist)
        self.db.create_collection(name, dim, dist)
        return {"name": name, "dimension": dim, "distance": dist, "status": "created"}

    async def _list_collections(self, args: Dict[str, Any]) -> Dict[str, Any]:
        return {"collections": self.db.list_collections()}

    async def _delete_collection(self, args: Dict[str, Any]) -> Dict[str, Any]:
        name = self._require_str(args, "name")
        deleted = self.db.delete_collection(name)
        if deleted:
            self.persistence.delete_collection_file(name)
        return {"name": name, "deleted": deleted}

    async def _insert(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        vid = self._require_str(args, "id")
        vec = self._require_list(args, "vector")
        meta = args.get("metadata") or {}
        if not isinstance(meta, dict):
            raise ValueError("metadata must be an object/dict")
        self.db.insert(coll, vid, vec, meta)
        return {"id": vid, "status": "inserted"}

    async def _insert_batch(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        items = self._require_list(args, "items")
        normalised: List[Dict[str, Any]] = []
        for raw in items:
            if not isinstance(raw, dict):
                raise ValueError("each item must be an object")
            vid = raw.get("id")
            vec = raw.get("vector")
            if not isinstance(vid, str) or not vid:
                raise ValueError("item.id must be a non-empty string")
            if not isinstance(vec, list):
                raise ValueError("item.vector must be a list of numbers")
            meta = raw.get("metadata") or {}
            if not isinstance(meta, dict):
                raise ValueError("item.metadata must be an object/dict")
            normalised.append({"id": vid, "vector": vec, "metadata": meta})
        count = self.db.insert_batch(coll, normalised)
        return {"count": count}

    async def _search(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        vec = self._require_list(args, "vector")
        top_k = int(args.get("top_k") or 10)
        if top_k <= 0:
            raise ValueError("top_k must be positive")
        filt = args.get("filter")
        if filt is not None and not isinstance(filt, dict):
            raise ValueError("filter must be an object/dict")
        results = self.db.search(coll, vec, top_k=top_k, metadata_filter=filt)
        return {"results": results}

    async def _get(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        vid = self._require_str(args, "id")
        arr, meta = self.db.get(coll, vid)
        return {"id": vid, "vector": arr.tolist(), "metadata": meta}

    async def _delete(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        vid = self._require_str(args, "id")
        deleted = self.db.delete(coll, vid)
        return {"id": vid, "deleted": deleted}

    async def _count(self, args: Dict[str, Any]) -> Dict[str, Any]:
        coll = self._require_str(args, "collection")
        return {"count": self.db.count(coll)}

    async def _health(self, args: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "status": "UP",
            "version": __version__,
            "collections": len(self.db.collection_names()),
            "total_vectors": self.db.total_vectors(),
        }

    # ------------------------------------------------------------------
    # Validation helpers
    # ------------------------------------------------------------------
    @staticmethod
    def _require_str(args: Dict[str, Any], key: str) -> str:
        val = args.get(key)
        if not isinstance(val, str) or not val:
            raise ValueError(f"{key!r} must be a non-empty string")
        return val

    @staticmethod
    def _require_int(args: Dict[str, Any], key: str, min_value: int = 0) -> int:
        val = args.get(key)
        if isinstance(val, bool) or not isinstance(val, int):
            raise ValueError(f"{key!r} must be an integer")
        if val < min_value:
            raise ValueError(f"{key!r} must be >= {min_value}")
        return val

    @staticmethod
    def _require_list(args: Dict[str, Any], key: str) -> List[Any]:
        val = args.get(key)
        if not isinstance(val, list):
            raise ValueError(f"{key!r} must be a list")
        return val


# Tools that mutate the database and therefore trigger a save.
_MUTATING_TOOLS = {
    "vector_create_collection",
    "vector_delete_collection",
    "vector_insert",
    "vector_insert_batch",
    "vector_delete",
}

_HANDLERS = {
    "vector_create_collection": VectorMCPServer._create_collection,
    "vector_list_collections": VectorMCPServer._list_collections,
    "vector_delete_collection": VectorMCPServer._delete_collection,
    "vector_insert": VectorMCPServer._insert,
    "vector_insert_batch": VectorMCPServer._insert_batch,
    "vector_search": VectorMCPServer._search,
    "vector_get": VectorMCPServer._get,
    "vector_delete": VectorMCPServer._delete,
    "vector_count": VectorMCPServer._count,
    "vector_health": VectorMCPServer._health,
}


# ---------------------------------------------------------------------------
# Error mapping
# ---------------------------------------------------------------------------
def map_error(exc: BaseException) -> Dict[str, Any]:
    """Convert a :class:`BaseException` into a JSON-RPC error payload."""
    if isinstance(exc, (CollectionNotFoundError, VectorNotFoundError)):
        return {"code": ERR_NOT_FOUND, "message": str(exc)}
    if isinstance(exc, CollectionAlreadyExistsError):
        return {"code": ERR_ALREADY_EXISTS, "message": str(exc)}
    if isinstance(exc, (DimensionMismatchError, InvalidDistanceMetricError, ValueError)):
        return {"code": ERR_INVALID_PARAMS, "message": str(exc)}
    if isinstance(exc, PersistenceError):
        return {"code": ERR_INTERNAL, "message": str(exc)}
    if isinstance(exc, VectorMCPError):
        return {"code": ERR_INTERNAL, "message": str(exc)}
    return {"code": ERR_INTERNAL, "message": f"Internal error: {exc}"}


# ---------------------------------------------------------------------------
# stdio transport
# ---------------------------------------------------------------------------
async def main() -> None:  # pragma: no cover - runtime entry point
    """Start the MCP server on stdio using the official SDK."""
    if not _MCP_AVAILABLE or Server is None or stdio_server is None:
        sys.stderr.write(
            "[vector_mcp] the 'mcp' package is not installed. "
            "Install with: pip install 'mcp[cli]>=1.0.0'\n"
        )
        sys.exit(1)

    server_instance = Server(  # type: ignore[call-arg]
        name="xarch-vector-mcp",
        version=__version__,
    )
    app = VectorMCPServer()

    @server_instance.list_tools()  # type: ignore[misc]
    async def _list_tools() -> List[Any]:
        if Tool is not None:
            return [Tool(**t) for t in _tool_definitions()]
        return _tool_definitions()

    @server_instance.call_tool()  # type: ignore[misc]
    async def _call_tool(name: str, arguments: Dict[str, Any]) -> List[Any]:
        try:
            payload = await app.call_tool(name, arguments or {})
        except BaseException as exc:  # noqa: BLE001
            err = map_error(exc)
            return [{"type": "text", "text": json.dumps({"error": err})}]
        return [{"type": "text", "text": json.dumps(payload, default=_json_default)}]

    async with stdio_server() as (read_stream, write_stream):
        await server_instance.run(
            read_stream,
            write_stream,
            server_instance.create_initialization_options(),  # type: ignore[attr-defined]
        )


# ---------------------------------------------------------------------------
# Lightweight in-memory transport for tests
# ---------------------------------------------------------------------------
def _json_default(obj: Any) -> Any:
    """Fallback encoder for numpy types in JSON responses."""
    if hasattr(obj, "tolist"):
        return obj.tolist()
    if isinstance(obj, set):
        return list(obj)
    raise TypeError(f"Object of type {type(obj).__name__} is not JSON serialisable")
