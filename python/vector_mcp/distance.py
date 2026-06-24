"""
Distance / similarity functions used by the in-memory vector database.

All functions accept either Python sequences (``list``, ``tuple``) or
``numpy.ndarray`` instances and return a single ``float`` score.

Conventions
-----------
* ``cosine_similarity`` returns a value in ``[0.0, 1.0]`` where ``1.0`` means
  identical direction and ``0.0`` means orthogonal / zero-norm.
* ``euclidean_distance`` returns a non-negative float where ``0.0`` means
  identical vectors.
* ``dot_product`` returns the raw dot product (no normalisation).
"""

from __future__ import annotations

import math
from typing import Sequence, Union

import numpy as np

VectorLike = Union[Sequence[float], np.ndarray]

# Public distance metrics supported by the vector database.
DISTANCE_METRICS: tuple[str, ...] = ("cosine", "euclidean", "dot")


def _to_array(vec: VectorLike) -> np.ndarray:
    """Convert a vector-like input to a 1D ``float64`` numpy array."""
    arr = np.asarray(vec, dtype=np.float64)
    if arr.ndim != 1:
        arr = arr.reshape(-1)
    return arr


def cosine_similarity(a: VectorLike, b: VectorLike) -> float:
    """Compute cosine similarity between two vectors.

    The result is the dot product divided by the product of L2 norms,
    clamped to ``[0.0, 1.0]``. Identical direction -> 1.0; orthogonal or
    zero-norm vectors -> 0.0. The function is symmetric in its arguments.
    """
    va = _to_array(a)
    vb = _to_array(b)
    if va.shape != vb.shape:
        raise ValueError(
            f"Vector shape mismatch for cosine_similarity: {va.shape} vs {vb.shape}"
        )
    norm_a = float(np.linalg.norm(va))
    norm_b = float(np.linalg.norm(vb))
    if norm_a == 0.0 or norm_b == 0.0:
        return 0.0
    sim = float(np.dot(va, vb) / (norm_a * norm_b))
    # Clamp to handle tiny floating point overshoot (e.g. 1.0000000002).
    if sim > 1.0:
        return 1.0
    if sim < 0.0:
        return 0.0
    return sim


def euclidean_distance(a: VectorLike, b: VectorLike) -> float:
    """Compute the L2 (Euclidean) distance between two vectors.

    Returns a non-negative ``float``. Identical vectors -> 0.0.
    """
    va = _to_array(a)
    vb = _to_array(b)
    if va.shape != vb.shape:
        raise ValueError(
            f"Vector shape mismatch for euclidean_distance: {va.shape} vs {vb.shape}"
        )
    return float(np.linalg.norm(va - vb))


def dot_product(a: VectorLike, b: VectorLike) -> float:
    """Compute the plain dot product of two vectors."""
    va = _to_array(a)
    vb = _to_array(b)
    if va.shape != vb.shape:
        raise ValueError(
            f"Vector shape mismatch for dot_product: {va.shape} vs {vb.shape}"
        )
    return float(np.dot(va, vb))


def score_to_distance(metric: str, score: float) -> float:
    """Convert a similarity score to a distance value for sorting.

    For "cosine" we invert by subtracting from 1.0 (since higher similarity
    means closer). For "euclidean" and "dot" the value is already a distance
    in the right direction (lower is closer), so we return it unchanged.
    """
    if metric == "cosine":
        return 1.0 - float(score)
    return float(score)


def distance_to_score(metric: str, distance: float) -> float:
    """Inverse of :func:`score_to_distance` -- convert a distance back to a score.

    "cosine" -> 1.0 - distance. "euclidean" and "dot" -> distance.
    """
    if metric == "cosine":
        return 1.0 - float(distance)
    return float(distance)


def compute(metric: str, a: VectorLike, b: VectorLike) -> float:
    """Dispatch a metric name to the appropriate function.

    Parameters
    ----------
    metric:
        One of ``"cosine"``, ``"euclidean"``, ``"dot"``.
    a, b:
        The two vectors to compare.

    Returns
    -------
    float
        The raw score (similarity for "cosine", distance for the others).
    """
    match metric:
        case "cosine":
            return cosine_similarity(a, b)
        case "euclidean":
            return euclidean_distance(a, b)
        case "dot":
            return dot_product(a, b)
        case _:  # pragma: no cover - guarded by callers
            raise ValueError(
                f"Unknown distance metric {metric!r}; expected one of {DISTANCE_METRICS}"
            )


def is_valid_metric(metric: str) -> bool:
    """Return ``True`` if ``metric`` is a supported distance metric."""
    return metric in DISTANCE_METRICS


def unit_vector(vec: VectorLike) -> np.ndarray:
    """Return the L2-normalised version of a vector.

    A zero-norm input is returned unchanged.
    """
    arr = _to_array(vec)
    norm = float(np.linalg.norm(arr))
    if norm == 0.0 or math.isclose(norm, 0.0):
        return arr
    return arr / norm
