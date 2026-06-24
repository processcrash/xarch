package com.xarch.mcp.vector.store;

import com.xarch.mcp.vector.distance.DistanceMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorStoreTest {

    @Test
    void createAndListCollection() {
        VectorStore store = new VectorStore();
        store.createCollection("docs", 4, DistanceMetric.COSINE);
        assertEquals(1, store.listCollections().size());
        assertEquals("docs", store.listCollections().get(0).name());
    }

    @Test
    void createDuplicateCollectionReturnsExisting() {
        VectorStore store = new VectorStore();
        store.createCollection("docs", 4, DistanceMetric.COSINE);
        store.createCollection("docs", 4, DistanceMetric.COSINE);
        assertEquals(1, store.listCollections().size());
    }

    @Test
    void createCollectionWithDifferentConfigFails() {
        VectorStore store = new VectorStore();
        store.createCollection("docs", 4, DistanceMetric.COSINE);
        assertThrows(IllegalStateException.class,
            () -> store.createCollection("docs", 8, DistanceMetric.COSINE));
    }

    @Test
    void insertAndGetVector() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 3, DistanceMetric.COSINE);
        col.upsert(new VectorEntry("a", new float[]{1f, 0f, 0f}, Map.of("tag", "x")));
        VectorEntry entry = col.get("a");
        assertNotNull(entry);
        assertEquals("a", entry.id());
        assertEquals("x", entry.metadata().get("tag"));
    }

    @Test
    void dimensionMismatchIsRejected() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 3, DistanceMetric.COSINE);
        assertThrows(IllegalArgumentException.class,
            () -> col.upsert(new VectorEntry("a", new float[]{1f, 0f}, Map.of())));
    }

    @Test
    void searchReturnsNearestNeighbors() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 3, DistanceMetric.COSINE);
        col.upsert(new VectorEntry("a", new float[]{1f, 0f, 0f}, Map.of()));
        col.upsert(new VectorEntry("b", new float[]{0f, 1f, 0f}, Map.of()));
        col.upsert(new VectorEntry("c", new float[]{0.9f, 0.1f, 0f}, Map.of()));

        List<SearchHit> hits = col.search(new float[]{1f, 0f, 0f}, 2, null);
        assertEquals(2, hits.size());
        // a (identical) should outscore c (almost identical)
        assertEquals("a", hits.get(0).id());
        assertEquals("c", hits.get(1).id());
        assertTrue(hits.get(0).score() >= hits.get(1).score());
    }

    @Test
    void searchWithMetadataFilter() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 2, DistanceMetric.COSINE);
        col.upsert(new VectorEntry("a", new float[]{1f, 0f}, Map.of("type", "doc")));
        col.upsert(new VectorEntry("b", new float[]{0.9f, 0.1f}, Map.of("type", "img")));

        List<SearchHit> hits = col.search(new float[]{1f, 0f}, 10, Map.of("type", "doc"));
        assertEquals(1, hits.size());
        assertEquals("a", hits.get(0).id());
    }

    @Test
    void searchWithNoMatchesReturnsEmpty() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 2, DistanceMetric.COSINE);
        col.upsert(new VectorEntry("a", new float[]{1f, 0f}, Map.of()));
        List<SearchHit> hits = col.search(new float[]{1f, 0f}, 10, Map.of("missing", "value"));
        assertTrue(hits.isEmpty());
    }

    @Test
    void deleteRemovesVector() {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 2, DistanceMetric.COSINE);
        col.upsert(new VectorEntry("a", new float[]{1f, 0f}, Map.of()));
        assertTrue(col.delete("a"));
        assertNull(col.get("a"));
        assertFalse(col.delete("a"));
    }

    @Test
    void deleteCollectionRemovesIt() {
        VectorStore store = new VectorStore();
        store.createCollection("docs", 2, DistanceMetric.COSINE);
        assertTrue(store.deleteCollection("docs"));
        assertTrue(store.listCollections().isEmpty());
    }

    @Test
    void concurrentInsertsAreThreadSafe() throws Exception {
        VectorStore store = new VectorStore();
        VectorCollection col = store.createCollection("docs", 2, DistanceMetric.COSINE);
        ExecutorService exec = Executors.newFixedThreadPool(8);
        try {
            int n = 500;
            for (int i = 0; i < n; i++) {
                final int id = i;
                exec.submit(() -> col.upsert(
                    new VectorEntry("v" + id, new float[]{id, 0f}, Map.of("i", id))));
            }
            exec.shutdown();
            assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS));
            assertEquals(n, col.size());
        } finally {
            exec.shutdownNow();
        }
    }
}
