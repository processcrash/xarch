"""Tests for :mod:`vector_mcp.persistence`."""

from __future__ import annotations

from pathlib import Path

import numpy as np
import pytest

from vector_mcp.persistence import PersistenceManager, default_storage_dir
from vector_mcp.vector_db import VectorDB


@pytest.fixture()
def tmp_storage(tmp_path: Path) -> PersistenceManager:
    return PersistenceManager(storage_dir=tmp_path)


class TestPersistenceManager:
    def test_default_storage_dir_creates_directory(self) -> None:
        d = default_storage_dir()
        assert d.exists()
        assert d.is_dir()

    def test_save_creates_file(self, tmp_storage: PersistenceManager) -> None:
        db = VectorDB()
        db.create_collection("alpha", dimension=3, distance_metric="cosine")
        db.insert("alpha", "x", [1.0, 2.0, 3.0], metadata={"k": "v"})
        tmp_storage.save_db(db)
        files = list(tmp_storage.storage_dir.glob("*.json"))
        assert any(f.name == "alpha.json" for f in files)

    def test_load_restores_collections(self, tmp_storage: PersistenceManager) -> None:
        db = VectorDB()
        db.create_collection("alpha", dimension=3, distance_metric="cosine")
        db.insert("alpha", "x", [1.0, 2.0, 3.0], metadata={"k": "v"})
        db.insert("alpha", "y", [4.0, 5.0, 6.0], metadata={"k": "w"})
        tmp_storage.save_db(db)

        # New DB reads from the same directory.
        db2 = VectorDB()
        loaded = tmp_storage.load_into(db2)
        assert loaded == 1
        assert db2.has_collection("alpha")
        coll = db2.get_collection("alpha")
        assert coll.count() == 2
        vec, meta = coll.get("x")
        assert np.allclose(vec, [1.0, 2.0, 3.0])
        assert meta == {"k": "v"}

    def test_roundtrip_preserves_metadata(self, tmp_storage: PersistenceManager) -> None:
        db = VectorDB()
        db.create_collection("beta", dimension=2, distance_metric="euclidean")
        db.insert("beta", "a", [0.0, 0.0], metadata={"tag": "first", "n": 1})
        db.insert("beta", "b", [1.0, 1.0], metadata={"tag": "second", "n": 2})
        tmp_storage.save_db(db)

        db2 = VectorDB()
        tmp_storage.load_into(db2)
        a_vec, a_meta = db2.get("beta", "a")
        b_vec, b_meta = db2.get("beta", "b")
        assert np.allclose(a_vec, [0.0, 0.0])
        assert np.allclose(b_vec, [1.0, 1.0])
        assert a_meta == {"tag": "first", "n": 1}
        assert b_meta == {"tag": "second", "n": 2}

    def test_delete_collection_file(self, tmp_storage: PersistenceManager) -> None:
        db = VectorDB()
        db.create_collection("c", dimension=2, distance_metric="cosine")
        db.insert("c", "a", [1.0, 0.0])
        tmp_storage.save_db(db)
        assert tmp_storage.delete_collection_file("c") is True
        assert tmp_storage.delete_collection_file("c") is False
        # And the in-memory state is untouched.
        assert db.has_collection("c")

    def test_load_all_empty_dir(self, tmp_storage: PersistenceManager) -> None:
        assert tmp_storage.load_all() == {}

    def test_load_into_empty_returns_zero(self, tmp_storage: PersistenceManager) -> None:
        assert tmp_storage.load_into(VectorDB()) == 0
