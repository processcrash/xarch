"""Tests for :mod:`vector_mcp.distance`."""

from __future__ import annotations

import math

import pytest

from vector_mcp import distance as dist


class TestCosineSimilarity:
    def test_identical_vectors_score_one(self) -> None:
        assert math.isclose(dist.cosine_similarity([1, 2, 3], [1, 2, 3]), 1.0)

    def test_orthogonal_vectors_score_zero(self) -> None:
        assert math.isclose(dist.cosine_similarity([1, 0], [0, 1]), 0.0)

    def test_opposite_vectors_clamped_to_zero(self) -> None:
        # [-1, -1] vs [1, 1] -> similarity = -1, clamped to 0.
        assert math.isclose(dist.cosine_similarity([-1, -1], [1, 1]), 0.0)

    def test_zero_vector_returns_zero(self) -> None:
        assert dist.cosine_similarity([0, 0, 0], [1, 2, 3]) == 0.0
        assert dist.cosine_similarity([0, 0, 0], [0, 0, 0]) == 0.0

    def test_dimension_mismatch_raises(self) -> None:
        with pytest.raises(ValueError):
            dist.cosine_similarity([1, 2], [1, 2, 3])


class TestEuclideanDistance:
    def test_identical_vectors_zero(self) -> None:
        assert math.isclose(dist.euclidean_distance([1, 2, 3], [1, 2, 3]), 0.0)

    def test_known_distance(self) -> None:
        # (3, 4) -> 5
        assert math.isclose(dist.euclidean_distance([0, 0], [3, 4]), 5.0)

    def test_dimension_mismatch_raises(self) -> None:
        with pytest.raises(ValueError):
            dist.euclidean_distance([1.0, 2.0], [1.0, 2.0, 3.0])


class TestDotProduct:
    def test_orthogonal_dot(self) -> None:
        assert math.isclose(dist.dot_product([1, 0, 0], [0, 1, 0]), 0.0)

    def test_known_dot(self) -> None:
        # 1*4 + 2*5 + 3*6 = 32
        assert math.isclose(dist.dot_product([1, 2, 3], [4, 5, 6]), 32.0)

    def test_dimension_mismatch_raises(self) -> None:
        with pytest.raises(ValueError):
            dist.dot_product([1, 2], [1, 2, 3])


class TestDispatchers:
    def test_compute_dispatch(self) -> None:
        assert math.isclose(dist.compute("cosine", [1, 0], [1, 0]), 1.0)
        assert math.isclose(dist.compute("euclidean", [0, 0], [3, 4]), 5.0)
        assert math.isclose(dist.compute("dot", [1, 2], [3, 4]), 11.0)

    def test_compute_unknown_metric(self) -> None:
        with pytest.raises(ValueError):
            dist.compute("manhattan", [1, 2], [1, 2])

    def test_is_valid_metric(self) -> None:
        assert dist.is_valid_metric("cosine")
        assert dist.is_valid_metric("euclidean")
        assert dist.is_valid_metric("dot")
        assert not dist.is_valid_metric("manhattan")

    def test_score_to_distance_roundtrip(self) -> None:
        for metric in ("cosine", "euclidean", "dot"):
            assert math.isclose(
                dist.distance_to_score(metric, dist.score_to_distance(metric, 0.42)),
                0.42,
            )

    def test_unit_vector(self) -> None:
        u = dist.unit_vector([3.0, 4.0])
        assert math.isclose(float((u ** 2).sum()), 1.0)
        # Zero vector stays zero.
        z = dist.unit_vector([0.0, 0.0])
        assert math.isclose(float(z[0]), 0.0)
        assert math.isclose(float(z[1]), 0.0)
