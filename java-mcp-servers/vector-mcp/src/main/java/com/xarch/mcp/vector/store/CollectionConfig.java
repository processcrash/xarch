package com.xarch.mcp.vector.store;

/**
 * Static configuration of a vector collection.
 */
public record CollectionConfig(String name, int dimension, DistanceMetric metric) {

    public CollectionConfig {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Collection name must not be blank");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        if (metric == null) {
            throw new IllegalArgumentException("Distance metric must not be null");
        }
    }
}