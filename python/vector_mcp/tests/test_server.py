"""Tests for the MCP server wrapper.

These tests exercise :class:`VectorMCPServer` directly (which is independent of
the transport layer) so they run even when the ``mcp`` package is not
installed. The in-memory transport pattern from ``mcp.shared.memory`` is
referenced in a separate, optional test that is skipped automatically when
the package is unavailable.
"""

from __future__ import annotations

import asyncio
import json
import shutil
import tempfile
from pathlib import Path
from typing import Any, Dict, Optional

import pytest

from vector_mcp.errors import (
    CollectionAlreadyExistsError,
    CollectionNotFoundError,
    DimensionMismatchError,
    InvalidDistanceMetricError,
    VectorNotFoundError,
)
from vector_mcp.persistence import PersistenceManager
from vector_mcp.server import (
    VectorMCPServer,
    _tool_definitions,
    map_error,
)
from vector_mcp.vector_db import VectorDB


def _call(server: VectorMCPServer, name: str, args: Optional[Dict[str, Any]] = None) -> Any:
    return asyncio.run(server.call_tool(name, args or {}))


@pytest.fixture()
def tmp_dir() -> Path:
    d = Path(tempfile.mkdtemp(prefix="vector-mcp-test-"))
    try:
        yield d
    finally:
        shutil.rmtree(d, ignore_errors=True)


@pytest.fixture()
def server(tmp_dir: Path) -> VectorMCPServer:
    return VectorMCPServer(
        db=VectorDB(),
        persistence=PersistenceManager(storage_dir=tmp_dir),
    )


class TestToolDefinitions:
    def test_defines_ten_tools(self) -> None:
        tools = _tool_definitions()
        assert len(tools) == 10
        names = {t["name"] for t in tools}
        assert names == {
            "vector_create_collection",
            "vector_list_collections",
            "vector_delete_collection",
            "vector_insert",
            "vector_insert_batch",
            "vector_search",
            "vector_get",
            "vector_delete",
            "vector_count",
            "vector_health",
        }

    def test_each_tool_has_schema(self) -> None:
        for t in _tool_definitions():
            assert "inputSchema" in t
            assert t["inputSchema"]["type"] == "object"


class TestCreateCollection:
    def test_create_ok(self, server: VectorMCPServer) -> None:
        out = _call(server, "vector_create_collection", {
            "name": "docs", "dimension": 3, "distance": "cosine"
        })
        assert out == {
            "name": "docs", "dimension": 3, "distance": "cosine", "status": "created"
        }

    def test_create_duplicate(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "docs", "dimension": 3, "distance": "cosine"
        })
        with pytest.raises(CollectionAlreadyExistsError):
            _call(server, "vector_create_collection", {
                "name": "docs", "dimension": 3, "distance": "cosine"
            })

    def test_create_invalid_metric(self, server: VectorMCPServer) -> None:
        with pytest.raises(InvalidDistanceMetricError):
            _call(server, "vector_create_collection", {
                "name": "x", "dimension": 3, "distance": "manhattan"
            })

    def test_create_missing_name(self, server: VectorMCPServer) -> None:
        with pytest.raises(ValueError):
            _call(server, "vector_create_collection", {
                "dimension": 3, "distance": "cosine"
            })


class TestListAndDeleteCollection:
    def test_list_empty(self, server: VectorMCPServer) -> None:
        out = _call(server, "vector_list_collections", {})
        assert out == {"collections": []}

    def test_list_after_create(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "a", "dimension": 2, "distance": "cosine"
        })
        _call(server, "vector_insert", {
            "collection": "a", "id": "x", "vector": [1.0, 0.0]
        })
        out = _call(server, "vector_list_collections", {})
        assert out["collections"][0]["name"] == "a"
        assert out["collections"][0]["count"] == 1

    def test_delete_existing(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "a", "dimension": 2, "distance": "cosine"
        })
        out = _call(server, "vector_delete_collection", {"name": "a"})
        assert out == {"name": "a", "deleted": True}

    def test_delete_missing(self, server: VectorMCPServer) -> None:
        out = _call(server, "vector_delete_collection", {"name": "ghost"})
        assert out == {"name": "ghost", "deleted": False}


class TestInsertGetDelete:
    def _setup(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "c", "dimension": 3, "distance": "cosine"
        })

    def test_insert_and_get(self, server: VectorMCPServer) -> None:
        self._setup(server)
        _call(server, "vector_insert", {
            "collection": "c", "id": "x",
            "vector": [1.0, 2.0, 3.0], "metadata": {"tag": "hi"}
        })
        out = _call(server, "vector_get", {"collection": "c", "id": "x"})
        assert out["id"] == "x"
        assert out["vector"] == [1.0, 2.0, 3.0]
        assert out["metadata"] == {"tag": "hi"}

    def test_insert_dimension_mismatch(self, server: VectorMCPServer) -> None:
        self._setup(server)
        with pytest.raises(DimensionMismatchError):
            _call(server, "vector_insert", {
                "collection": "c", "id": "x", "vector": [1.0, 2.0]
            })

    def test_get_missing(self, server: VectorMCPServer) -> None:
        self._setup(server)
        with pytest.raises(VectorNotFoundError):
            _call(server, "vector_get", {"collection": "c", "id": "ghost"})

    def test_insert_into_missing_collection(self, server: VectorMCPServer) -> None:
        with pytest.raises(CollectionNotFoundError):
            _call(server, "vector_insert", {
                "collection": "nope", "id": "x", "vector": [1.0, 0.0, 0.0]
            })

    def test_delete_vector(self, server: VectorMCPServer) -> None:
        self._setup(server)
        _call(server, "vector_insert", {
            "collection": "c", "id": "x", "vector": [1.0, 0.0, 0.0]
        })
        out = _call(server, "vector_delete", {"collection": "c", "id": "x"})
        assert out == {"id": "x", "deleted": True}
        out = _call(server, "vector_delete", {"collection": "c", "id": "x"})
        assert out == {"id": "x", "deleted": False}

    def test_count(self, server: VectorMCPServer) -> None:
        self._setup(server)
        _call(server, "vector_insert", {
            "collection": "c", "id": "x", "vector": [1.0, 0.0, 0.0]
        })
        _call(server, "vector_insert", {
            "collection": "c", "id": "y", "vector": [0.0, 1.0, 0.0]
        })
        assert _call(server, "vector_count", {"collection": "c"}) == {"count": 2}


class TestInsertBatch:
    def test_batch_insert(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "c", "dimension": 2, "distance": "cosine"
        })
        out = _call(server, "vector_insert_batch", {
            "collection": "c",
            "items": [
                {"id": "a", "vector": [1.0, 0.0], "metadata": {"k": 1}},
                {"id": "b", "vector": [0.0, 1.0]},
            ],
        })
        assert out == {"count": 2}
        assert _call(server, "vector_count", {"collection": "c"}) == {"count": 2}

    def test_batch_invalid_item_raises(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "c", "dimension": 2, "distance": "cosine"
        })
        with pytest.raises(ValueError):
            _call(server, "vector_insert_batch", {
                "collection": "c",
                "items": [{"vector": [1.0, 0.0]}],  # missing id
            })


class TestSearch:
    def _populate(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "c", "dimension": 3, "distance": "cosine"
        })
        _call(server, "vector_insert", {
            "collection": "c", "id": "a", "vector": [1.0, 0.0, 0.0],
            "metadata": {"type": "x"}
        })
        _call(server, "vector_insert", {
            "collection": "c", "id": "b", "vector": [0.9, 0.1, 0.0],
            "metadata": {"type": "x"}
        })
        _call(server, "vector_insert", {
            "collection": "c", "id": "c", "vector": [0.0, 1.0, 0.0],
            "metadata": {"type": "y"}
        })

    def test_search_basic(self, server: VectorMCPServer) -> None:
        self._populate(server)
        out = _call(server, "vector_search", {
            "collection": "c", "vector": [1.0, 0.0, 0.0], "top_k": 2
        })
        assert len(out["results"]) == 2
        assert out["results"][0]["id"] == "a"
        assert out["results"][0]["score"] == pytest.approx(1.0)

    def test_search_with_filter(self, server: VectorMCPServer) -> None:
        self._populate(server)
        out = _call(server, "vector_search", {
            "collection": "c", "vector": [1.0, 0.0, 0.0],
            "top_k": 5, "filter": {"type": "x"}
        })
        ids = {r["id"] for r in out["results"]}
        assert ids == {"a", "b"}

    def test_search_invalid_filter_type(self, server: VectorMCPServer) -> None:
        self._populate(server)
        with pytest.raises(ValueError):
            _call(server, "vector_search", {
                "collection": "c", "vector": [1.0, 0.0, 0.0],
                "top_k": 5, "filter": "not-a-dict"
            })


class TestHealth:
    def test_health_reports_status(self, server: VectorMCPServer) -> None:
        out = _call(server, "vector_health", {})
        assert out["status"] == "UP"
        assert out["collections"] == 0
        assert out["total_vectors"] == 0
        assert "version" in out

    def test_health_counts(self, server: VectorMCPServer) -> None:
        _call(server, "vector_create_collection", {
            "name": "c", "dimension": 2, "distance": "cosine"
        })
        _call(server, "vector_insert", {
            "collection": "c", "id": "x", "vector": [1.0, 0.0]
        })
        out = _call(server, "vector_health", {})
        assert out["collections"] == 1
        assert out["total_vectors"] == 1


class TestErrorMapping:
    def test_collection_not_found(self) -> None:
        err = map_error(CollectionNotFoundError("c"))
        assert err["code"] == -32004

    def test_collection_already_exists(self) -> None:
        err = map_error(CollectionAlreadyExistsError("c"))
        assert err["code"] == -32005

    def test_invalid_params(self) -> None:
        err = map_error(DimensionMismatchError(2, 3))
        assert err["code"] == -32602

    def test_value_error(self) -> None:
        err = map_error(ValueError("bad"))
        assert err["code"] == -32602

    def test_internal_error(self) -> None:
        err = map_error(RuntimeError("boom"))
        assert err["code"] == -32603


# ---------------------------------------------------------------------------
# Optional in-memory MCP transport test
# ---------------------------------------------------------------------------
mcp_memory = pytest.importorskip("mcp.shared.memory", reason="mcp SDK not installed")


class TestInMemoryTransport:
    def test_round_trip_over_memory_transport(self, tmp_dir: Path) -> None:
        """Spin up the real stdio-style server in-memory and call a tool.

        This test is skipped automatically when the official ``mcp`` SDK is
        not installed in the environment.
        """
        from mcp.server import Server
        from mcp.shared.memory import create_connected_server_and_client_session
        from mcp.types import Tool

        server_instance = VectorMCPServer(
            db=VectorDB(),
            persistence=PersistenceManager(storage_dir=tmp_dir),
        )

        app = Server("xarch-vector-mcp-test", "1.0.0")  # type: ignore[call-arg]

        @app.list_tools()
        async def _list_tools() -> list:
            return [Tool(**t) for t in _tool_definitions()]

        @app.call_tool()
        async def _call_tool(name: str, arguments: Dict[str, Any]) -> list:
            payload = await server_instance.call_tool(name, arguments or {})
            return [{"type": "text", "text": json.dumps(payload)}]

        async def scenario() -> Dict[str, Any]:
            async with create_connected_server_and_client_session(
                app, raise_exceptions=True
            ) as session:
                await session.initialize()
                result = await session.call_tool("vector_health", {})
                # The SDK returns a CallToolResult; payload is in .content[0].text
                return json.loads(result.content[0].text)  # type: ignore[union-attr]

        result = asyncio.run(scenario())
        assert result["status"] == "UP"
        assert result["collections"] == 0
        assert result["total_vectors"] == 0
