package com.xarch.mcp.vector.store;

import java.util.Collections;
import java.util.Map;

/**
 * A single vector record stored in a collection.
 *
 * @param id        caller-supplied identifier; must be unique within the collection
 * @param vector    the embedding values
 * @param metadata  arbitrary JSON-serializable payload
 */
public record VectorEntry(String id, float[] vector, Map<String, Object> metadata) {

    public VectorEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vector id must not be blank");
        }
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty");
        }
        metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(metadata);
        // Defensive copy so callers cannot mutate the stored vector.
        vector = vector.clone();
    }

    public float[] vectorCopy() {
        return vector.clone();
    }
}
