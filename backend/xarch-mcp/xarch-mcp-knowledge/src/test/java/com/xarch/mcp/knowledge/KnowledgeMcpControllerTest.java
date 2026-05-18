package com.xarch.mcp.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xarch.starter.core.result.ApiResult;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KnowledgeMcpController unit tests
 */
@SpringBootTest
class KnowledgeMcpControllerTest {

    @Autowired
    private KnowledgeMcpController controller;

    @Test
    void testHealth() {
        ApiResult<Map<String, Object>> result = controller.health();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertEquals("UP", result.getData().get("status"));
    }

    @Test
    void testTools() {
        ApiResult<List<Map<String, String>>> result = controller.tools();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().size() >= 5);
    }

    @Test
    void testIndexDocument() {
        var result = controller.indexDocument(Map.of(
            "id", "test-doc-1",
            "title", "Test Document",
            "content", "This is a test document for knowledge base indexing.",
            "type", "test"
        ));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testSearch() {
        // First index a document
        controller.indexDocument(Map.of(
            "id", "test-doc-search",
            "title", "Search Test Document",
            "content", "The quick brown fox jumps over the lazy dog.",
            "type", "test"
        ));

        // Then search
        var result = controller.search(Map.of(
            "query", "quick brown fox",
            "topK", 5
        ));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testSearchWithEmptyQuery() {
        var result = controller.search(Map.of());
        assertNotNull(result);
        // Should return error for empty query
        assertEquals("1002", result.getCode());
    }

    @Test
    void testGetDocument() {
        // First index a document
        controller.indexDocument(Map.of(
            "id", "test-doc-get",
            "title", "Get Test Document",
            "content", "Document content for retrieval test.",
            "type", "test"
        ));

        // Then retrieve it
        var result = controller.getDocument(Map.of("documentId", "test-doc-get"));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetDocumentNotFound() {
        var result = controller.getDocument(Map.of("documentId", "nonexistent-id"));
        assertNotNull(result);
        // Should return error for non-existent document
        assertEquals("1002", result.getCode());
    }

    @Test
    void testListDocuments() {
        var result = controller.listDocuments();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }
}