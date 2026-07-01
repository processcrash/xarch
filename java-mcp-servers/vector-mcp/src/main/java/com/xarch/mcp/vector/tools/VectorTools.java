package com.xarch.mcp.vector.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.runtime.stdio.StdioMcpServer.ContentBlock;
import com.xarch.mcp.vector.VectorConfig;
import com.xarch.mcp.vector.store.CollectionConfig;
import com.xarch.mcp.vector.store.DistanceMetric;
import com.xarch.mcp.vector.store.SearchHit;
import com.xarch.mcp.vector.store.VectorCollection;
import com.xarch.mcp.vector.store.VectorEntry;
import com.xarch.mcp.vector.store.VectorStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tool handlers for the vector MCP stdio server. */
public final class VectorTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private VectorTools() {}

    public static List<ContentBlock> configure(VectorConfig config, JsonNode args) {
        if (args.has("type")) config.setType(args.path("type").asText());
        if (args.has("defaultDimension")) config.setDefaultDimension(args.path("defaultDimension").asInt());
        if (args.has("host")) config.setHost(args.path("host").asText());
        if (args.has("port")) config.setPort(args.path("port").asInt());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "configured");
        result.putAll(config.snapshot());
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> createCollection(VectorStore store, JsonNode args) {
        if (!args.has("name")) throw new IllegalArgumentException("name is required");
        if (!args.has("dimension")) throw new IllegalArgumentException("dimension is required");
        String name = args.path("name").asText();
        int dimension = args.path("dimension").asInt();
        DistanceMetric metric = DistanceMetric.fromString(args.path("distance").asText("cosine"));
        VectorCollection col = store.createCollection(name, dimension, metric);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", col.config().name());
        result.put("dimension", col.config().dimension());
        result.put("distance", col.config().metric().name().toLowerCase());
        result.put("status", "created");
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> upsert(VectorStore store, JsonNode args) {
        if (!args.has("collection")) throw new IllegalArgumentException("collection is required");
        if (!args.has("vectors") || !args.path("vectors").isArray()) {
            throw new IllegalArgumentException("vectors must be an array");
        }
        String collectionName = args.path("collection").asText();
        VectorCollection col = store.getCollection(collectionName)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + collectionName));
        int count = 0;
        for (JsonNode v : args.path("vectors")) {
            String id = v.path("id").asText(UUID.randomUUID().toString());
            JsonNode vecNode = v.path("vector");
            if (!vecNode.isArray() || vecNode.size() == 0) {
                throw new IllegalArgumentException("vector must be a non-empty array");
            }
            float[] vec = new float[vecNode.size()];
            for (int i = 0; i < vecNode.size(); i++) {
                vec[i] = (float) vecNode.get(i).asDouble();
            }
            Map<String, Object> metadata = v.has("metadata") && v.path("metadata").isObject()
                    ? MAPPER.convertValue(v.path("metadata"), Map.class)
                    : Map.of();
            if (v.has("content") && !v.path("content").isNull()) {
                metadata = new LinkedHashMap<>(metadata);
                metadata.put("content", v.path("content").asText());
            }
            col.upsert(new VectorEntry(id, vec, metadata));
            count++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("collection", collectionName);
        result.put("insertedCount", count);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> search(VectorStore store, JsonNode args) {
        String collectionName = requireString(args, "collection");
        JsonNode vecNode = requireArray(args, "vector");
        int limit = args.has("limit") ? args.path("limit").asInt() : 10;
        Map<String, Object> filter = args.has("filter") && args.path("filter").isObject()
                ? MAPPER.convertValue(args.path("filter"), Map.class) : null;

        VectorCollection col = store.getCollection(collectionName)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + collectionName));
        float[] query = toFloats(vecNode);
        List<SearchHit> hits = col.search(query, limit, filter);

        List<Map<String, Object>> results = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", hit.id());
            m.put("score", hit.score());
            m.put("metadata", hit.metadata());
            results.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", results.size());
        result.put("results", results);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> textSearch(VectorStore store, JsonNode args) {
        // No real embedding here — placeholder for an LLM-embedded query.
        // Return an empty result with a note explaining the wiring needed.
        String collectionName = args.path("collection").asText();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("collection", collectionName);
        result.put("note", "text_search requires an embedding model; wire LLM to embed the query then call search");
        result.put("results", List.of());
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> delete(VectorStore store, JsonNode args) {
        String collectionName = requireString(args, "collection");
        String id = requireString(args, "id");
        VectorCollection col = store.getCollection(collectionName)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + collectionName));
        boolean deleted = col.delete(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("collection", collectionName);
        result.put("id", id);
        result.put("deleted", deleted);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> listCollections(VectorStore store) {
        List<Map<String, Object>> cols = new ArrayList<>();
        for (CollectionConfig cfg : store.listCollections()) {
            VectorCollection col = store.getCollection(cfg.name()).orElseThrow();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", cfg.name());
            m.put("dimension", cfg.dimension());
            m.put("distance", cfg.metric().name().toLowerCase());
            m.put("count", col.size());
            cols.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", cols.size());
        result.put("collections", cols);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> getStats(VectorStore store, JsonNode args) {
        String collectionName = requireString(args, "collection");
        VectorCollection col = store.getCollection(collectionName)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found: " + collectionName));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("collection", collectionName);
        result.put("exists", true);
        result.put("dimension", col.config().dimension());
        result.put("distance", col.config().metric().name().toLowerCase());
        result.put("size", col.size());
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> health(VectorConfig config, VectorStore store) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("service", "vector-mcp");
        result.put("version", "1.0.0");
        result.put("type", config.getType());
        result.put("collections", store.listCollections().size());
        result.put("totalVectors", store.totalVectors());
        return List.of(ContentBlock.text(toJson(result)));
    }

    private static String requireString(JsonNode args, String field) {
        if (!args.has(field) || args.path(field).isNull()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return args.path(field).asText();
    }

    private static JsonNode requireArray(JsonNode args, String field) {
        JsonNode n = args.path(field);
        if (!n.isArray() || n.size() == 0) {
            throw new IllegalArgumentException(field + " must be a non-empty array");
        }
        return n;
    }

    private static float[] toFloats(JsonNode arr) {
        float[] out = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            out[i] = (float) arr.get(i).asDouble();
        }
        return out;
    }

    private static String toJson(Object value) {
        try { return MAPPER.writeValueAsString(value); }
        catch (Exception e) { return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }
}