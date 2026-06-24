"""
Persistence layer for the in-memory vector database.

Collections are stored as individual JSON files inside a configurable directory
(default ``~/.xarch/vector-mcp/``). Each file is named after the collection
and contains the collection name, dimension, distance metric, vectors (as
lists of floats) and metadata.

The :class:`PersistenceManager` is intentionally small: it is told when a
mutation happens and it writes the affected collection to disk. It also
offers a :meth:`load_all` helper used at server startup to repopulate the
in-memory state.
"""

from __future__ import annotations

import base64
import json
import os
import threading
from pathlib import Path
from typing import Any, Dict, Optional

from .errors import PersistenceError
from .vector_db import VectorDB

# Environment variable used to override the default storage directory.
STORAGE_DIR_ENV = "VECTOR_MCP_STORAGE_DIR"

# Filename for the manifest (used to track which collections exist on disk).
MANIFEST_FILENAME = "manifest.json"


def default_storage_dir() -> Path:
    """Return the default storage directory, creating it if necessary."""
    base = Path(os.path.expanduser("~")) / ".xarch" / "vector-mcp"
    base.mkdir(parents=True, exist_ok=True)
    return base


def _collection_filename(name: str) -> str:
    """Sanitise a collection name into a safe filename."""
    safe = "".join(c if c.isalnum() or c in ("-", "_", ".") else "_" for c in name)
    return f"{safe}.json"


class PersistenceManager:
    """Saves and loads vector collections as JSON files on disk."""

    def __init__(self, storage_dir: Optional[Path] = None) -> None:
        self.storage_dir: Path = (
            Path(storage_dir).expanduser().resolve()
            if storage_dir is not None
            else default_storage_dir()
        )
        self.storage_dir.mkdir(parents=True, exist_ok=True)
        self._lock = threading.RLock()

    # ------------------------------------------------------------------
    # Filesystem helpers
    # ------------------------------------------------------------------
    def _path_for(self, name: str) -> Path:
        return self.storage_dir / _collection_filename(name)

    def _atomic_write(self, path: Path, payload: Dict[str, Any]) -> None:
        """Write *payload* as JSON to *path* atomically."""
        tmp = path.with_suffix(path.suffix + ".tmp")
        try:
            data = json.dumps(payload, ensure_ascii=False, indent=2)
            with open(tmp, "w", encoding="utf-8") as fh:
                fh.write(data)
            os.replace(tmp, path)
        except OSError as exc:
            raise PersistenceError(
                f"Failed to write collection file {path}: {exc}"
            ) from exc

    def _read_json(self, path: Path) -> Dict[str, Any]:
        try:
            with open(path, "r", encoding="utf-8") as fh:
                return json.load(fh)
        except (OSError, json.JSONDecodeError) as exc:
            raise PersistenceError(
                f"Failed to read collection file {path}: {exc}"
            ) from exc

    # ------------------------------------------------------------------
    # Collection level save / load / delete
    # ------------------------------------------------------------------
    def save_collection(self, collection_payload: Dict[str, Any]) -> None:
        """Persist a single collection. ``payload`` is from ``to_dict``."""
        if not collection_payload or "name" not in collection_payload:
            raise PersistenceError("Cannot save collection without a name")
        name = collection_payload["name"]
        enriched = dict(collection_payload)
        # Vectors are stored as plain lists for human-readability. We also
        # include a base64 blob for fast binary round-trip in the future.
        encoded_vectors = {
            vid: base64.b64encode(
                json.dumps(vec).encode("utf-8")
            ).decode("ascii")
            for vid, vec in enriched.get("entries", {}).items()
        }
        enriched["vectors_b64"] = encoded_vectors
        with self._lock:
            self._atomic_write(self._path_for(name), enriched)

    def delete_collection_file(self, name: str) -> bool:
        """Remove the persisted file for *name*. Returns ``True`` if removed."""
        path = self._path_for(name)
        with self._lock:
            if path.exists():
                try:
                    path.unlink()
                except OSError as exc:
                    raise PersistenceError(
                        f"Failed to delete collection file {path}: {exc}"
                    ) from exc
                return True
            return False

    def load_all(self) -> Dict[str, Dict[str, Any]]:
        """Load every collection file from the storage directory.

        Returns a dict ``{collection_name: serialised_collection}``.
        """
        results: Dict[str, Dict[str, Any]] = {}
        with self._lock:
            for path in sorted(self.storage_dir.glob("*.json")):
                if path.name == MANIFEST_FILENAME:
                    continue
                data = self._read_json(path)
                if "name" not in data:
                    continue
                results[data["name"]] = data
        return results

    # ------------------------------------------------------------------
    # DB-level helpers
    # ------------------------------------------------------------------
    def save_db(self, db: VectorDB) -> None:
        """Persist every collection in *db*."""
        payload = db.to_dict()
        for coll_payload in payload.get("collections", {}).values():
            self.save_collection(coll_payload)

    def load_into(self, db: VectorDB) -> int:
        """Populate *db* from disk. Returns the number of collections loaded."""
        all_data = self.load_all()
        if not all_data:
            return 0
        db.load_from_dict({"collections": all_data})
        return len(all_data)
