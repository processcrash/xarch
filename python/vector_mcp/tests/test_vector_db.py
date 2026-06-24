"""Tests for :mod:`vector_mcp.vector_db`."""

from __future__ import annotations

import threading
import time

import pytest

from vector_mcp.errors import (
    CollectionAlreadyExistsError,
    CollectionNotFoundError,
    DimensionMismatchError,
    InvalidDistanceMetricError,
    VectorNotFoundError,
)
from vector_mcp.vector_db import VectorCollection, VectorDB


class TestVectorCollection:
    def test_create_with_invalid_metric_raises(self) -> None:
        with pytest.raises(InvalidDistanceMetricError):
            VectorCollection(name="bad", dimension=4, distance_metric="manhattan")

    def test_create_with_zero_dimension_raises(self) -> None:
        with pytest.raises(ValueError):
            VectorCollection(name="bad", dimension=0, distance_metric="cosine")

    def test_insert_and_get(self) -> None:
        coll = VectorCollection(name="c", dimension=3, distance_metric="cosine")
        coll.insert("a", [1.0, 2.0, 3.0], metadata={"tag": "x"})
        vec, meta = coll.get("a")
        assert vec.shape == (3,)
        assert meta == {"tag": "x"}

    def test_insert_dimension_mismatch(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        with pytest.raises(DimensionMismatchError):
            coll.insert("a", [1.0, 2.0, 3.0])

    def test_get_missing_raises(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        with pytest.raises(VectorNotFoundError):
            coll.get("missing")

    def test_delete_returns_bool(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        coll.insert("a", [1.0, 0.0])
        assert coll.delete("a") is True
        assert coll.delete("a") is False

    def test_knn_returns_nearest_neighbour(self) -> None:
        coll = VectorCollection(name="c", dimension=3, distance_metric="cosine")
        coll.insert("a", [1.0, 0.0, 0.0])
        coll.insert("b", [0.9, 0.1, 0.0])  # very close to query
        coll.insert("c", [0.0, 1.0, 0.0])  # orthogonal
        results = coll.search([1.0, 0.0, 0.0], top_k=2)
        assert results[0]["id"] == "a"
        assert results[1]["id"] == "b"
        # Best score should be 1.0 (perfect match).
        assert results[0]["score"] == pytest.approx(1.0)

    def test_metadata_filter(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        coll.insert("a", [1.0, 0.0], metadata={"type": "x"})
        coll.insert("b", [0.9, 0.1], metadata={"type": "y"})
        results = coll.search([1.0, 0.0], top_k=5, metadata_filter={"type": "y"})
        assert len(results) == 1
        assert results[0]["id"] == "b"

    def test_search_empty_collection(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        assert coll.search([1.0, 0.0]) == []

    def test_search_top_k_must_be_positive(self) -> None:
        coll = VectorCollection(name="c", dimension=2, distance_metric="cosine")
        with pytest.raises(ValueError):
            coll.search([1.0, 0.0], top_k=0)

    def test_to_from_dict_roundtrip(self) -> None:
        coll = VectorCollection(name="c", dimension=3, distance_metric="euclidean")
        coll.insert("a", [1.0, 2.0, 3.0], metadata={"k": "v"})
        coll.insert("b", [4.0, 5.0, 6.0], metadata={})
        payload = coll.to_dict()
        rebuilt = VectorCollection.from_dict(payload)
        assert rebuilt.name == "c"
        assert rebuilt.dimension == 3
        assert rebuilt.distance_metric == "euclidean"
        assert set(rebuilt.entries.keys()) == {"a", "b"}
        vec, meta = rebuilt.get("a")
        assert vec.tolist() == [1.0, 2.0, 3.0]
        assert meta == {"k": "v"}


class TestVectorDB:
    def test_create_get_delete_collection(self) -> None:
        db = VectorDB()
        coll = db.create_collection("docs", dimension=4, distance_metric="cosine")
        assert coll.name == "docs"
        assert db.has_collection("docs")
        assert db.get_collection("docs") is coll
        assert db.delete_collection("docs") is True
        assert not db.has_collection("docs")

    def test_create_duplicate_raises(self) -> None:
        db = VectorDB()
        db.create_collection("a", dimension=2, distance_metric="cosine")
        with pytest.raises(CollectionAlreadyExistsError):
            db.create_collection("a", dimension=2, distance_metric="cosine")

    def test_get_missing_collection_raises(self) -> None:
        db = VectorDB()
        with pytest.raises(CollectionNotFoundError):
            db.get_collection("missing")

    def test_list_collections(self) -> None:
        db = VectorDB()
        db.create_collection("a", dimension=2, distance_metric="cosine")
        db.create_collection("b", dimension=3, distance_metric="euclidean")
        db.insert("a", "x", [1.0, 0.0])
        info = db.list_collections()
        names = {c["name"] for c in info}
        assert names == {"a", "b"}
        a_info = next(c for c in info if c["name"] == "a")
        assert a_info["count"] == 1

    def test_total_vectors(self) -> None:
        db = VectorDB()
        db.create_collection("a", dimension=2, distance_metric="cosine")
        db.create_collection("b", dimension=2, distance_metric="cosine")
        db.insert("a", "x", [1.0, 0.0])
        db.insert("a", "y", [0.0, 1.0])
        db.insert("b", "z", [1.0, 1.0])
        assert db.total_vectors() == 3
        assert db.count("a") == 2
        assert db.count("b") == 1

    def test_search_returns_score_in_range(self) -> None:
        db = VectorDB()
        db.create_collection("c", dimension=3, distance_metric="cosine")
        db.insert("c", "a", [1.0, 0.0, 0.0])
        db.insert("c", "b", [0.0, 1.0, 0.0])
        results = db.search("c", [1.0, 0.0, 0.0], top_k=2)
        for r in results:
            assert 0.0 <= r["score"] <= 1.0

    def test_thread_safety_smoke(self) -> None:
        db = VectorDB()
        db.create_collection("c", dimension=4, distance_metric="cosine")

        errors: list[BaseException] = []

        def writer(start: int) -> None:
            try:
                for i in range(100):
                    db.insert("c", f"id-{start}-{i}", [float(i), 0.0, 0.0, 0.0])
            except BaseException as exc:  # noqa: BLE001
                errors.append(exc)

        def reader() -> None:
            try:
                for _ in range(50):
                    _ = db.search("c", [1.0, 0.0, 0.0, 0.0], top_k=5)
                    time.sleep(0.001)
            except BaseException as exc:  # noqa: BLE001
                errors.append(exc)

        threads = [
            threading.Thread(target=writer, args=(0,)),
            threading.Thread(target=writer, args=(1,)),
            threading.Thread(target=reader),
        ]
        for t in threads:
            t.start()
        for t in threads:
            t.join()

        assert not errors
        assert db.count("c") == 200
