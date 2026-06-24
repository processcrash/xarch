"""
In-memory vector database used by the vector_mcp server.

The database stores named ``VectorCollection`` instances. Each collection
holds vectors (as ``numpy.ndarray``), an arbitrary metadata dictionary per
vector id and a configured distance metric (``cosine`` / ``euclidean`` /
``dot``). All public state changes are guarded by a re-entrant lock so the
database can be shared across threads safely.

The actual similarity / distance math lives in :mod:`vector_mcp.distance`; this
module is responsible for storage, lookup, mutation, filtering and KNN.
"""

from __future__ import annotations

import threading
from dataclasses import dataclass, field
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple

import numpy as np

from . import distance as dist_mod
from .errors import (
    CollectionAlreadyExistsError,
    CollectionNotFoundError,
    DimensionMismatchError,
    InvalidDistanceMetricError,
    VectorNotFoundError,
)


# Type aliases kept short to keep signatures readable.
Metadata = Dict[str, Any]
SearchResult = Dict[str, Any]


@dataclass
class VectorCollection:
    """A single named collection of vectors and their metadata.

    Attributes
    ----------
    name:
        Unique collection name.
    dimension:
        Required dimensionality for every vector stored in the collection.
    distance_metric:
        One of ``"cosine"``, ``"euclidean"``, ``"dot"``.
    entries:
        Mapping of vector id -> ``numpy.ndarray`` (1-D, ``float64``).
    metadata:
        Mapping of vector id -> arbitrary JSON-serialisable metadata.
    """

    name: str
    dimension: int
    distance_metric: str
    entries: Dict[str, np.ndarray] = field(default_factory=dict)
    metadata: Dict[str, Metadata] = field(default_factory=dict)
    _lock: threading.RLock = field(default_factory=threading.RLock, repr=False)

    def __post_init__(self) -> None:
        if self.dimension <= 0:
            raise ValueError(
                f"Collection dimension must be positive, got {self.dimension}"
            )
        if not dist_mod.is_valid_metric(self.distance_metric):
            raise InvalidDistanceMetricError(self.distance_metric)

    # ------------------------------------------------------------------
    # Validation helpers
    # ------------------------------------------------------------------
    def _validate_vector(self, vector: Sequence[float]) -> np.ndarray:
        """Convert and validate an incoming vector against the collection dim."""
        try:
            arr = np.asarray(vector, dtype=np.float64)
        except (TypeError, ValueError) as exc:
            raise ValueError(f"Invalid vector input: {exc}") from exc
        if arr.ndim != 1:
            arr = arr.reshape(-1)
        if arr.shape[0] != self.dimension:
            raise DimensionMismatchError(self.dimension, int(arr.shape[0]))
        return arr

    # ------------------------------------------------------------------
    # Mutation
    # ------------------------------------------------------------------
    def insert(
        self,
        vector_id: str,
        vector: Sequence[float],
        metadata: Optional[Metadata] = None,
    ) -> None:
        """Insert or replace a vector in the collection."""
        if not isinstance(vector_id, str) or not vector_id:
            raise ValueError("vector id must be a non-empty string")
        arr = self._validate_vector(vector)
        with self._lock:
            self.entries[vector_id] = arr
            self.metadata[vector_id] = dict(metadata) if metadata else {}

    def delete(self, vector_id: str) -> bool:
        """Delete a vector from the collection. Returns ``True`` if removed."""
        with self._lock:
            if vector_id in self.entries:
                del self.entries[vector_id]
                self.metadata.pop(vector_id, None)
                return True
            return False

    def get(self, vector_id: str) -> Tuple[np.ndarray, Metadata]:
        """Return ``(vector, metadata)`` for an id. Raises if missing."""
        with self._lock:
            if vector_id not in self.entries:
                raise VectorNotFoundError(self.name, vector_id)
            return self.entries[vector_id], self.metadata.get(vector_id, {})

    def contains(self, vector_id: str) -> bool:
        """Return ``True`` if the id is in the collection (no lock)."""
        return vector_id in self.entries

    def count(self) -> int:
        """Return number of stored vectors."""
        with self._lock:
            return len(self.entries)

    # ------------------------------------------------------------------
    # Query
    # ------------------------------------------------------------------
    def search(
        self,
        vector: Sequence[float],
        top_k: int = 10,
        metadata_filter: Optional[Metadata] = None,
    ) -> List[SearchResult]:
        """KNN search returning top-k results sorted by best score first.

        The returned score is normalised to ``[0, 1]`` where ``1`` is the
        best possible match for the chosen metric.
        """
        if top_k <= 0:
            raise ValueError("top_k must be a positive integer")
        query = self._validate_vector(vector)

        with self._lock:
            ids = list(self.entries.keys())
            if not ids:
                return []
            ids = [i for i in ids if self._matches_filter(i, metadata_filter)]
            if not ids:
                return []
            # Vectorised scoring.
            matrix = np.stack([self.entries[i] for i in ids], axis=0)
            raw = dist_mod.compute(self.distance_metric, query, matrix.T)
            # ``dist_mod.compute`` returns a scalar when both args are 1-D.
            # Build a per-row score array.
            if np.isscalar(raw):
                scores = np.full(len(ids), float(raw), dtype=np.float64)
            else:
                scores = np.asarray(raw, dtype=np.float64)

            # For "cosine" ``compute`` returns similarity in [0, 1] already.
            # For "euclidean" we want score = 1 / (1 + d) so that lower d -> higher score.
            # For "dot" we just use the raw dot product (clipped to [0, 1]).
            if self.distance_metric == "cosine":
                normalised = scores
            elif self.distance_metric == "euclidean":
                normalised = 1.0 / (1.0 + scores)
            else:  # "dot"
                normalised = np.clip(scores, 0.0, 1.0)

            order = np.argsort(-normalised)  # descending
            top = order[: min(top_k, len(order))]

            results: List[SearchResult] = []
            for idx in top:
                vid = ids[int(idx)]
                results.append(
                    {
                        "id": vid,
                        "score": float(normalised[int(idx)]),
                        "metadata": self.metadata.get(vid, {}),
                    }
                )
            return results

    def _matches_filter(self, vector_id: str, filt: Optional[Metadata]) -> bool:
        """Return ``True`` if the vector's metadata satisfies the filter."""
        if not filt:
            return True
        meta = self.metadata.get(vector_id, {})
        for key, expected in filt.items():
            if key not in meta or meta[key] != expected:
                return False
        return True

    # ------------------------------------------------------------------
    # Serialisation helpers (used by persistence)
    # ------------------------------------------------------------------
    def to_dict(self) -> Dict[str, Any]:
        """Serialise the collection to a JSON-friendly dict."""
        with self._lock:
            return {
                "name": self.name,
                "dimension": self.dimension,
                "distance_metric": self.distance_metric,
                "entries": {
                    vid: vec.tolist() for vid, vec in self.entries.items()
                },
                "metadata": {
                    vid: dict(meta) for vid, meta in self.metadata.items()
                },
            }

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "VectorCollection":
        """Build a collection from its serialised representation."""
        coll = cls(
            name=data["name"],
            dimension=int(data["dimension"]),
            distance_metric=data["distance_metric"],
        )
        entries = data.get("entries", {}) or {}
        meta = data.get("metadata", {}) or {}
        for vid, vec in entries.items():
            coll.entries[vid] = np.asarray(vec, dtype=np.float64)
            if vid in meta:
                coll.metadata[vid] = dict(meta[vid])
        return coll


class VectorDB:
    """Thread-safe in-memory vector database holding many collections."""

    def __init__(self) -> None:
        self._collections: Dict[str, VectorCollection] = {}
        self._lock = threading.RLock()

    # ------------------------------------------------------------------
    # Collection management
    # ------------------------------------------------------------------
    def create_collection(
        self, name: str, dimension: int, distance_metric: str = "cosine"
    ) -> VectorCollection:
        """Create a new collection. Raises if it already exists."""
        if not isinstance(name, str) or not name:
            raise ValueError("collection name must be a non-empty string")
        with self._lock:
            if name in self._collections:
                raise CollectionAlreadyExistsError(name)
            coll = VectorCollection(
                name=name, dimension=dimension, distance_metric=distance_metric
            )
            self._collections[name] = coll
            return coll

    def get_collection(self, name: str) -> VectorCollection:
        """Return a collection by name. Raises if missing."""
        with self._lock:
            coll = self._collections.get(name)
            if coll is None:
                raise CollectionNotFoundError(name)
            return coll

    def list_collections(self) -> List[Dict[str, Any]]:
        """Return a summary list of all collections."""
        with self._lock:
            return [
                {
                    "name": coll.name,
                    "dimension": coll.dimension,
                    "distance": coll.distance_metric,
                    "count": coll.count(),
                }
                for coll in self._collections.values()
            ]

    def delete_collection(self, name: str) -> bool:
        """Delete a collection. Returns ``True`` if it was present."""
        with self._lock:
            return self._collections.pop(name, None) is not None

    def has_collection(self, name: str) -> bool:
        """Return ``True`` if a collection exists."""
        with self._lock:
            return name in self._collections

    # ------------------------------------------------------------------
    # Vector operations (convenience wrappers)
    # ------------------------------------------------------------------
    def insert(
        self,
        collection: str,
        vector_id: str,
        vector: Sequence[float],
        metadata: Optional[Metadata] = None,
    ) -> None:
        """Insert a vector into a named collection."""
        self.get_collection(collection).insert(vector_id, vector, metadata)

    def insert_batch(
        self,
        collection: str,
        items: Iterable[Dict[str, Any]],
    ) -> int:
        """Insert a batch of vectors. Returns the number inserted."""
        coll = self.get_collection(collection)
        count = 0
        for item in items:
            coll.insert(
                vector_id=item["id"],
                vector=item["vector"],
                metadata=item.get("metadata") or {},
            )
            count += 1
        return count

    def delete(self, collection: str, vector_id: str) -> bool:
        """Delete a vector from a collection."""
        return self.get_collection(collection).delete(vector_id)

    def get(
        self, collection: str, vector_id: str
    ) -> Tuple[np.ndarray, Metadata]:
        """Get a vector + metadata by id."""
        return self.get_collection(collection).get(vector_id)

    def count(self, collection: str) -> int:
        """Return number of vectors in a collection."""
        return self.get_collection(collection).count()

    def search(
        self,
        collection: str,
        vector: Sequence[float],
        top_k: int = 10,
        metadata_filter: Optional[Metadata] = None,
    ) -> List[SearchResult]:
        """KNN search within a collection."""
        return self.get_collection(collection).search(
            vector, top_k=top_k, metadata_filter=metadata_filter
        )

    # ------------------------------------------------------------------
    # Stats
    # ------------------------------------------------------------------
    def total_vectors(self) -> int:
        """Return the total number of vectors across all collections."""
        with self._lock:
            return sum(c.count() for c in self._collections.values())

    def collection_names(self) -> List[str]:
        """Return a snapshot of collection names."""
        with self._lock:
            return list(self._collections.keys())

    def to_dict(self) -> Dict[str, Any]:
        """Serialise the database (used by persistence layer)."""
        with self._lock:
            return {
                "version": 1,
                "collections": {
                    name: coll.to_dict()
                    for name, coll in self._collections.items()
                },
            }

    def load_from_dict(self, data: Dict[str, Any]) -> None:
        """Replace the in-memory state from a serialised dict."""
        collections = data.get("collections", {}) or {}
        with self._lock:
            self._collections = {
                name: VectorCollection.from_dict(payload)
                for name, payload in collections.items()
            }
