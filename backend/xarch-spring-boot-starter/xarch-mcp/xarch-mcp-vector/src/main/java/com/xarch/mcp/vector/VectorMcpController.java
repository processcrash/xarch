package com.xarch.mcp.vector;

import com.xarch.mcp.vector.distance.DistanceFunction;
import com.xarch.mcp.vector.distance.DistanceMetric;
import com.xarch.mcp.vector.store.CollectionConfig;
import com.xarch.mcp.vector.store.SearchHit;
import com.xarch.mcp.vector.store.VectorCollection;
import com.xarch.mcp.vector.store.VectorEntry;
import com.xarch.mcp.vector.store.VectorStore;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vector Database MCP Server Controller.
 *
 * Exposes a small REST surface that mirrors the MCP tool set used by the Node
 * and Python vector-mcp servers. State lives in {@link VectorStore} (in-memory)
 * which is safe for the embedded MCP process; replace with a real vector DB
 * client when integrating with Qdrant, Milvus, pgvector, etc.
 */
@RestController
@RequestMapping("/mcp/vector")
public class VectorMcpController {

    private final VectorStore store = new VectorStore();

    // ---------------------------------------------------------------------
    // Meta
    // ---------------------------------------------------------------------

    @GetMapping("/health")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
            "status", "UP",
            "service", "vector-mcp",
            "version", "1.0.0",
            "collections", store.listCollections().size(),
            "totalVectors", store.totalVectors()
        ));
    }

    @GetMapping("/tools")
    public ApiResult<List<Map<String, String>>> tools() {
        return ApiResult.success(List.of(
            Map.of("name", "vector_create_collection", "description", "Create a new vector collection"),
            Map.of("name", "vector_list_collections", "description", "List all collections"),
            Map.of("name", "vector_delete_collection", "description", "Delete a collection"),
            Map.of("name", "vector_insert", "description", "Insert a single vector"),
            Map.of("name", "vector_insert_batch", "description", "Insert a batch of vectors"),
            Map.of("name", "vector_search", "description", "KNN search by vector"),
            Map.of("name", "vector_get", "description", "Get vector by id"),
            Map.of("name", "vector_delete", "description", "Delete a single vector"),
            Map.of("name", "vector_count", "description", "Count vectors in a collection")
        ));
    }

    // ---------------------------------------------------------------------
    // Collections
    // ---------------------------------------------------------------------

    @PostMapping("/tools/vector_create_collection")
    public ApiResult<Map<String, Object>> createCollection(@RequestBody CreateCollectionRequest req) {
        if (req.name == null || req.name.isBlank()) {
            return ApiResult.error("name is required");
        }
        if (req.dimension == null || req.dimension <= 0) {
            return ApiResult.error("dimension must be a positive integer");
        }
        DistanceMetric metric = DistanceMetric.fromString(req.distance);
        VectorCollection collection = store.createCollection(req.name, req.dimension, metric);
        return ApiResult.success(Map.of(
            "name", collection.config().name(),
            "dimension", collection.config().dimension(),
            "distance", collection.config().metric().name().toLowerCase(),
            "status", "created"
        ));
    }

    @GetMapping("/tools/vector_list_collections")
    public ApiResult<Map<String, Object>> listCollections() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CollectionConfig cfg : store.listCollections()) {
            VectorCollection col = store.getCollection(cfg.name()).orElseThrow();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", cfg.name());
            item.put("dimension", cfg.dimension());
            item.put("distance", cfg.metric().name().toLowerCase());
            item.put("count", col.size());
            list.add(item);
        }
        return ApiResult.success(Map.of(
            "collections", list,
            "count", list.size()
        ));
    }

    @PostMapping("/tools/vector_delete_collection")
    public ApiResult<Map<String, Object>> deleteCollection(@RequestBody Map<String, Object> body) {
        String name = asString(body.get("name"));
        if (name == null) {
            return ApiResult.error("name is required");
        }
        boolean deleted = store.deleteCollection(name);
        return ApiResult.success(Map.of("name", name, "deleted", deleted));
    }

    // ---------------------------------------------------------------------
    // Vectors
    // ---------------------------------------------------------------------

    @PostMapping("/tools/vector_insert")
    public ApiResult<Map<String, Object>> insert(@RequestBody VectorUpsertRequest req) {
        Optional<VectorCollection> opt = store.getCollection(req.collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + req.collection);
        }
        Map<String, Object> metadata = req.metadata == null ? Map.of() : req.metadata;
        opt.get().upsert(new VectorEntry(req.id, toFloats(req.vector), metadata));
        return ApiResult.success(Map.of("id", req.id, "status", "inserted"));
    }

    @PostMapping("/tools/vector_insert_batch")
    public ApiResult<Map<String, Object>> insertBatch(@RequestBody BatchUpsertRequest req) {
        Optional<VectorCollection> opt = store.getCollection(req.collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + req.collection);
        }
        VectorCollection collection = opt.get();
        int count = 0;
        for (VectorUpsertRequest item : req.items) {
            Map<String, Object> metadata = item.metadata == null ? Map.of() : item.metadata;
            collection.upsert(new VectorEntry(item.id, toFloats(item.vector), metadata));
            count++;
        }
        return ApiResult.success(Map.of("count", count));
    }

    @PostMapping("/tools/vector_search")
    public ApiResult<Map<String, Object>> search(@RequestBody SearchRequest req) {
        Optional<VectorCollection> opt = store.getCollection(req.collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + req.collection);
        }
        int topK = req.topK == null ? 10 : Math.max(1, req.topK);
        List<SearchHit> hits = opt.get().search(toFloats(req.vector), topK, req.filter);
        List<Map<String, Object>> results = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            results.add(Map.of(
                "id", hit.id(),
                "score", hit.score(),
                "metadata", hit.metadata()
            ));
        }
        return ApiResult.success(Map.of("results", results, "count", results.size()));
    }

    @PostMapping("/tools/vector_get")
    public ApiResult<Map<String, Object>> get(@RequestBody Map<String, Object> body) {
        String collection = asString(body.get("collection"));
        String id = asString(body.get("id"));
        if (collection == null || id == null) {
            return ApiResult.error("collection and id are required");
        }
        Optional<VectorCollection> opt = store.getCollection(collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + collection);
        }
        VectorEntry entry = opt.get().get(id);
        if (entry == null) {
            return ApiResult.error("Vector not found: " + id);
        }
        return ApiResult.success(Map.of(
            "id", entry.id(),
            "vector", toDoubleList(entry.vector()),
            "metadata", entry.metadata()
        ));
    }

    @PostMapping("/tools/vector_delete")
    public ApiResult<Map<String, Object>> delete(@RequestBody Map<String, Object> body) {
        String collection = asString(body.get("collection"));
        String id = asString(body.get("id"));
        if (collection == null || id == null) {
            return ApiResult.error("collection and id are required");
        }
        Optional<VectorCollection> opt = store.getCollection(collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + collection);
        }
        boolean deleted = opt.get().delete(id);
        return ApiResult.success(Map.of("id", id, "deleted", deleted));
    }

    @PostMapping("/tools/vector_count")
    public ApiResult<Map<String, Object>> count(@RequestBody Map<String, Object> body) {
        String collection = asString(body.get("collection"));
        if (collection == null) {
            return ApiResult.error("collection is required");
        }
        Optional<VectorCollection> opt = store.getCollection(collection);
        if (opt.isEmpty()) {
            return ApiResult.error("Collection not found: " + collection);
        }
        return ApiResult.success(Map.of("collection", collection, "count", opt.get().size()));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static float[] toFloats(List<Number> values) {
        if (values == null) {
            throw new IllegalArgumentException("vector must not be null");
        }
        float[] out = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Number n = values.get(i);
            out[i] = n == null ? 0f : n.floatValue();
        }
        return out;
    }

    private static List<Double> toDoubleList(float[] values) {
        List<Double> out = new ArrayList<>(values.length);
        for (float v : values) {
            out.add((double) v);
        }
        return out;
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString();
        return s.isBlank() ? null : s;
    }

    // ---------------------------------------------------------------------
    // DTOs
    // ---------------------------------------------------------------------

    public static class CreateCollectionRequest {
        public String name;
        public Integer dimension;
        public String distance;
    }

    public static class VectorUpsertRequest {
        public String collection;
        public String id;
        public List<Number> vector;
        public Map<String, Object> metadata;
    }

    public static class BatchUpsertRequest {
        public String collection;
        public List<VectorUpsertRequest> items;
    }

    public static class SearchRequest {
        public String collection;
        public List<Number> vector;
        public Integer topK;
        public Map<String, Object> filter;
    }

    // Force DistanceFunction to be referenced so static analysis keeps the
    // dependency (it's used in VectorCollection via the same enum).
    @SuppressWarnings("unused")
    private static final Class<?> KEEP_DISTANCE_FUNCTION = DistanceFunction.class;
}
