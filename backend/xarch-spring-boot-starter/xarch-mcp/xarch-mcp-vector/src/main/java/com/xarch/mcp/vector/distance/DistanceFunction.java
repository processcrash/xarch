package com.xarch.mcp.vector.distance;

/**
 * Pure-Java vector math utilities.
 *
 * All inputs are validated to have the same dimension before any math is performed
 * to provide clear error messages.
 */
public final class DistanceFunction {

    private DistanceFunction() {}

    /**
     * Cosine similarity in the range [-1, 1]. Identical direction vectors return 1.
     * Zero vectors return 0 to avoid NaN.
     */
    public static double cosine(float[] a, float[] b) {
        requireSameDimension(a, b);
        double dot = 0d, na = 0d, nb = 0d;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na == 0d || nb == 0d) {
            return 0d;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    /**
     * Euclidean (L2) distance. Lower is closer. Returns 0 for identical vectors.
     */
    public static double euclidean(float[] a, float[] b) {
        requireSameDimension(a, b);
        double sum = 0d;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    /**
     * Dot product. Higher is closer (for non-negative vectors).
     */
    public static double dot(float[] a, float[] b) {
        requireSameDimension(a, b);
        double sum = 0d;
        for (int i = 0; i < a.length; i++) {
            sum += (double) a[i] * b[i];
        }
        return sum;
    }

    /**
     * Convert raw metric value to a normalized similarity in [0, 1] where 1 = best.
     * Used so KNN search can return a consistent score across distance metrics.
     */
    public static double toSimilarity(DistanceMetric metric, double raw) {
        return switch (metric) {
            case COSINE -> (raw + 1d) / 2d;
            case EUCLIDEAN -> 1d / (1d + raw);
            case DOT -> raw;
        };
    }

    private static void requireSameDimension(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vector must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                "Dimension mismatch: " + a.length + " vs " + b.length);
        }
    }
}
