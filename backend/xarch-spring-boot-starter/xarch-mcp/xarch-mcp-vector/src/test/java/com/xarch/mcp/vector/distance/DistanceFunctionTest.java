package com.xarch.mcp.vector.distance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistanceFunctionTest {

    @Test
    void cosineOfIdenticalVectorsIsOne() {
        float[] a = {1f, 2f, 3f};
        assertEquals(1.0, DistanceFunction.cosine(a, a), 1e-6);
    }

    @Test
    void cosineOfOrthogonalVectorsIsZero() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};
        assertEquals(0.0, DistanceFunction.cosine(a, b), 1e-6);
    }

    @Test
    void cosineOfZeroVectorIsZero() {
        float[] a = {0f, 0f, 0f};
        float[] b = {1f, 2f, 3f};
        assertEquals(0.0, DistanceFunction.cosine(a, b), 1e-6);
    }

    @Test
    void euclideanOfIdenticalVectorsIsZero() {
        float[] a = {1f, 2f, 3f};
        assertEquals(0.0, DistanceFunction.euclidean(a, a), 1e-6);
    }

    @Test
    void euclideanOfUnitOffsetIsOne() {
        float[] a = {0f, 0f};
        float[] b = {1f, 0f};
        assertEquals(1.0, DistanceFunction.euclidean(a, b), 1e-6);
    }

    @Test
    void dotProductSumsProducts() {
        float[] a = {1f, 2f, 3f};
        float[] b = {4f, 5f, 6f};
        assertEquals(32.0, DistanceFunction.dot(a, b), 1e-6);
    }

    @Test
    void dimensionMismatchIsRejected() {
        float[] a = {1f, 2f};
        float[] b = {1f, 2f, 3f};
        assertThrows(IllegalArgumentException.class, () -> DistanceFunction.cosine(a, b));
        assertThrows(IllegalArgumentException.class, () -> DistanceFunction.euclidean(a, b));
        assertThrows(IllegalArgumentException.class, () -> DistanceFunction.dot(a, b));
    }

    @Test
    void nullVectorsAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> DistanceFunction.cosine(null, new float[]{1f}));
    }

    @Test
    void distanceMetricParseIsCaseInsensitive() {
        assertEquals(DistanceMetric.COSINE, DistanceMetric.fromString("cosine"));
        assertEquals(DistanceMetric.COSINE, DistanceMetric.fromString("COSINE"));
        assertEquals(DistanceMetric.EUCLIDEAN, DistanceMetric.fromString("l2"));
        assertEquals(DistanceMetric.DOT, DistanceMetric.fromString("dot_product"));
        assertEquals(DistanceMetric.COSINE, DistanceMetric.fromString(null));
    }

    @Test
    void unknownDistanceMetricIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> DistanceMetric.fromString("manhattan"));
    }

    @Test
    void toSimilarityNormalizesAllMetrics() {
        assertEquals(1.0, DistanceFunction.toSimilarity(DistanceMetric.COSINE, 1.0), 1e-6);
        assertEquals(0.5, DistanceFunction.toSimilarity(DistanceMetric.EUCLIDEAN, 1.0), 1e-6);
        assertEquals(1.0, DistanceFunction.toSimilarity(DistanceMetric.DOT, 1.0), 1e-6);
    }
}
