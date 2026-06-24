package com.xarch.mcp.vector.distance;

/**
 * Distance metric supported by the vector store.
 */
public enum DistanceMetric {
    /** Cosine similarity, normalized to [0, 1] where 1 = identical. */
    COSINE,
    /** Euclidean (L2) distance, lower is closer. Converted to similarity 1/(1+d). */
    EUCLIDEAN,
    /** Dot product. Higher is closer. Normalized to [0, 1] when non-negative. */
    DOT;

    public static DistanceMetric fromString(String value) {
        if (value == null || value.isBlank()) {
            return COSINE;
        }
        return switch (value.toLowerCase()) {
            case "cosine" -> COSINE;
            case "euclidean", "l2" -> EUCLIDEAN;
            case "dot", "dotproduct", "dot_product" -> DOT;
            default -> throw new IllegalArgumentException("Unsupported distance metric: " + value);
        };
    }
}
