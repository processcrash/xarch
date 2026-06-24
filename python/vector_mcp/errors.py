"""
Custom exception hierarchy for the vector_mcp package.

All errors raised by the vector database layer derive from ``VectorMCPError``
so that callers (most notably the MCP server) can map them to a single base
class while still being able to distinguish the specific failure mode.
"""

from __future__ import annotations


class VectorMCPError(Exception):
    """Base class for all vector_mcp exceptions."""


class CollectionNotFoundError(VectorMCPError):
    """Raised when a collection name is referenced that does not exist."""

    def __init__(self, name: str) -> None:
        super().__init__(f"Collection not found: {name!r}")
        self.name = name


class CollectionAlreadyExistsError(VectorMCPError):
    """Raised when attempting to create a collection that already exists."""

    def __init__(self, name: str) -> None:
        super().__init__(f"Collection already exists: {name!r}")
        self.name = name


class DimensionMismatchError(VectorMCPError):
    """Raised when a vector's dimension does not match the collection dimension."""

    def __init__(self, expected: int, actual: int) -> None:
        super().__init__(
            f"Vector dimension mismatch: expected {expected}, got {actual}"
        )
        self.expected = expected
        self.actual = actual


class VectorNotFoundError(VectorMCPError):
    """Raised when a vector id is referenced that does not exist in a collection."""

    def __init__(self, collection: str, vector_id: str) -> None:
        super().__init__(
            f"Vector {vector_id!r} not found in collection {collection!r}"
        )
        self.collection = collection
        self.vector_id = vector_id


class InvalidDistanceMetricError(VectorMCPError):
    """Raised when an unsupported distance metric is supplied."""

    def __init__(self, metric: str) -> None:
        super().__init__(
            f"Unsupported distance metric: {metric!r}. "
            "Supported metrics: 'cosine', 'euclidean', 'dot'."
        )
        self.metric = metric


class PersistenceError(VectorMCPError):
    """Raised when reading or writing a persistent collection fails."""
