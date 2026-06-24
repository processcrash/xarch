package com.xarch.mcp.vector.store;

import com.xarch.mcp.vector.distance.DistanceFunction;
import com.xarch.mcp.vector.distance.DistanceMetric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A single vector collection: holds a fixed-dimension embedding space indexed by
 * caller-supplied IDs. Thread-safe for concurrent reads/writes.
 */
public class VectorCollection {

    private final CollectionConfig config;
    private final Map<String, VectorEntry> entries = new ConcurrentHashMap<>();

    public VectorCollection(CollectionConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public CollectionConfig config() {
        return config;
    }

    public int size() {
        return entries.size();
    }

    /**
     * Insert or replace a vector. Throws if the vector dimension does not match
     * the collection's configured dimension.
     */
    public void upsert(VectorEntry entry) {
        if (entry.vector().length != config.dimension()) {
            throw new IllegalArgumentException(
                "Vector dimension " + entry.vector().length
                    + " does not match collection dimension " + config.dimension());
        }
        entries.put(entry.id(), entry);
    }

    public boolean delete(String id) {
        return entries.remove(id) != null;
    }

    public VectorEntry get(String id) {
        return entries.get(id);
    }

    /**
     * K-nearest-neighbor search. Returns at most {@code topK} hits ordered by
     * descending similarity.
     *
     * <p>If {@code metadataFilter} is non-null, only entries whose metadata contains
     * the given key/value pairs are considered.
     */
    public List<SearchHit> search(float[] query, int topK, Map<String, Object> metadataFilter) {
        if (query == null || query.length != config.dimension()) {
            throw new IllegalArgumentException(
                "Query vector dimension must equal " + config.dimension());
        }
        if (topK <= 0) {
            return List.of();
        }

        DistanceMetric metric = config.metric();
        List<SearchHit> scored = new ArrayList<>();
        for (VectorEntry e : entries.values()) {
            if (metadataFilter != null && !matchesFilter(e.metadata(), metadataFilter)) {
                continue;
            }
            double raw = rawScore(metric, query, e.vector());
            double similarity = DistanceFunction.toSimilarity(metric, raw);
            scored.add(new SearchHit(e.id(), similarity, e.metadata()));
        }

        // Higher similarity is better; we want top-K closest.
        scored.sort(Comparator.comparingDouble(SearchHit::score).reversed());
        if (scored.size() > topK) {
            return scored.subList(0, topK);
        }
        return scored;
    }

    private double rawScore(DistanceMetric metric, float[] q, float[] v) {
        return switch (metric) {
            case COSINE -> DistanceFunction.cosine(q, v);
            case EUCLIDEAN -> DistanceFunction.euclidean(q, v);
            case DOT -> DistanceFunction.dot(q, v);
        };
    }

    private static boolean matchesFilter(Map<String, Object> metadata,
                                         Map<String, Object> filter) {
        if (metadata == null) {
            return false;
        }
        for (Map.Entry<String, Object> e : filter.entrySet()) {
            if (!Objects.equals(metadata.get(e.getKey()), e.getValue())) {
                return false;
            }
        }
        return true;
    }
}
