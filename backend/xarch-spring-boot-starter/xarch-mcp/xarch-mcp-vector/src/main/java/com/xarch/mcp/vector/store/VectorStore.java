package com.xarch.mcp.vector.store;

import com.xarch.mcp.vector.distance.DistanceMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory vector store. Backed by per-collection {@link VectorCollection}
 * instances held in a thread-safe map. Persistence is provided by
 * {@code VectorStorePersistence} which can snapshot and restore state to disk.
 */
public class VectorStore {

    private final Map<String, VectorCollection> collections = new ConcurrentHashMap<>();

    /**
     * Create a new collection. Returns the created collection, or the existing
     * one if a collection with the same name already exists with the same config.
     */
    public VectorCollection createCollection(String name, int dimension, DistanceMetric metric) {
        return collections.compute(name, (k, existing) -> {
            if (existing != null) {
                if (existing.config().dimension() != dimension
                    || existing.config().metric() != metric) {
                    throw new IllegalStateException(
                        "Collection " + name + " already exists with a different config");
                }
                return existing;
            }
            return new VectorCollection(new CollectionConfig(name, dimension, metric));
        });
    }

    public boolean deleteCollection(String name) {
        return collections.remove(name) != null;
    }

    public Optional<VectorCollection> getCollection(String name) {
        return Optional.ofNullable(collections.get(name));
    }

    public List<CollectionConfig> listCollections() {
        List<CollectionConfig> configs = new ArrayList<>(collections.size());
        for (VectorCollection c : collections.values()) {
            configs.add(c.config());
        }
        return configs;
    }

    public int totalVectors() {
        int n = 0;
        for (VectorCollection c : collections.values()) {
            n += c.size();
        }
        return n;
    }

    /**
     * Internal: register a collection programmatically (used by persistence restore).
     */
    void putCollection(VectorCollection collection) {
        Objects.requireNonNull(collection, "collection");
        collections.put(collection.config().name(), collection);
    }
}
