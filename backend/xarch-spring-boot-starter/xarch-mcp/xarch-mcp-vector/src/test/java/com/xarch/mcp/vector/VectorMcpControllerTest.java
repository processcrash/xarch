package com.xarch.mcp.vector;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorMcpControllerTest {

    @Test
    void healthExposesStoreMetadata() {
        VectorMcpController controller = new VectorMcpController();
        @SuppressWarnings("unchecked")
        Map<String, Object> health =
            (Map<String, Object>) controller.health().getData();
        assertEquals("UP", health.get("status"));
        assertEquals("vector-mcp", health.get("service"));
        assertEquals(0, health.get("collections"));
    }

    @Test
    void createInsertAndSearchFlow() {
        VectorMcpController controller = new VectorMcpController();

        VectorMcpController.CreateCollectionRequest create = new VectorMcpController.CreateCollectionRequest();
        create.name = "demo";
        create.dimension = 3;
        create.distance = "cosine";
        controller.createCollection(create);

        VectorMcpController.VectorUpsertRequest v = new VectorMcpController.VectorUpsertRequest();
        v.collection = "demo";
        v.id = "a";
        v.vector = List.of(1.0, 0.0, 0.0);
        v.metadata = Map.of("tag", "x");
        controller.insert(v);

        VectorMcpController.VectorUpsertRequest v2 = new VectorMcpController.VectorUpsertRequest();
        v2.collection = "demo";
        v2.id = "b";
        v2.vector = List.of(0.0, 1.0, 0.0);
        v2.metadata = Map.of("tag", "y");
        controller.insert(v2);

        VectorMcpController.SearchRequest search = new VectorMcpController.SearchRequest();
        search.collection = "demo";
        search.vector = List.of(1.0, 0.0, 0.0);
        search.topK = 1;

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
            (Map<String, Object>) controller.search(search).getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hits =
            (List<Map<String, Object>>) result.get("results");
        assertEquals(1, hits.size());
        assertEquals("a", hits.get(0).get("id"));
    }

    @Test
    void createCollectionRejectsInvalidInput() {
        VectorMcpController controller = new VectorMcpController();
        VectorMcpController.CreateCollectionRequest req = new VectorMcpController.CreateCollectionRequest();
        req.name = "";
        req.dimension = 3;
        assertNotNull(controller.createCollection(req).getMessage());
    }

    @Test
    void unknownCollectionYieldsError() {
        VectorMcpController controller = new VectorMcpController();
        VectorMcpController.SearchRequest search = new VectorMcpController.SearchRequest();
        search.collection = "missing";
        search.vector = List.of(1.0, 0.0);
        search.topK = 5;
        assertNotNull(controller.search(search).getMessage());
    }

    @Test
    void insertBatchReportsCount() {
        VectorMcpController controller = new VectorMcpController();

        VectorMcpController.CreateCollectionRequest create = new VectorMcpController.CreateCollectionRequest();
        create.name = "batch";
        create.dimension = 2;
        create.distance = "euclidean";
        controller.createCollection(create);

        VectorMcpController.VectorUpsertRequest v1 = new VectorMcpController.VectorUpsertRequest();
        v1.collection = "batch";
        v1.id = "a";
        v1.vector = List.of(1.0, 0.0);
        VectorMcpController.VectorUpsertRequest v2 = new VectorMcpController.VectorUpsertRequest();
        v2.collection = "batch";
        v2.id = "b";
        v2.vector = List.of(0.0, 1.0);

        VectorMcpController.BatchUpsertRequest batch = new VectorMcpController.BatchUpsertRequest();
        batch.collection = "batch";
        batch.items = List.of(v1, v2);
        @SuppressWarnings("unchecked")
        Map<String, Object> result =
            (Map<String, Object>) controller.insertBatch(batch).getData();
        assertEquals(2, result.get("count"));
    }

    @Test
    void deleteCollectionRemovesEntries() {
        VectorMcpController controller = new VectorMcpController();
        VectorMcpController.CreateCollectionRequest create = new VectorMcpController.CreateCollectionRequest();
        create.name = "tmp";
        create.dimension = 2;
        create.distance = "dot";
        controller.createCollection(create);

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
            (Map<String, Object>) controller.deleteCollection(Map.of("name", "tmp")).getData();
        assertTrue((Boolean) result.get("deleted"));

        @SuppressWarnings("unchecked")
        Map<String, Object> list =
            (Map<String, Object>) controller.listCollections().getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> collections =
            (List<Map<String, Object>>) list.get("collections");
        assertFalse(collections.stream().anyMatch(m -> "tmp".equals(m.get("name"))));
    }
}
