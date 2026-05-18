package com.xarch.mcp.knowledge;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Knowledge Base MCP Server Controller
 * Provides RAG (Retrieval Augmented Generation) capabilities
 */
@RestController
@RequestMapping("/mcp/knowledge")
public class KnowledgeMcpController {

    private final KnowledgeBase knowledgeBase = new KnowledgeBase();

    /**
     * Health check
     */
    @GetMapping("/health")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
            "status", "UP",
            "service", "knowledge-mcp",
            "version", "1.0.0",
            "documentCount", knowledgeBase.getDocumentCount(),
            "chunkCount", knowledgeBase.getChunkCount()
        ));
    }

    /**
     * List available tools
     */
    @GetMapping("/tools")
    public ApiResult<List<Map<String, String>>> tools() {
        return ApiResult.success(List.of(
            Map.of("name", "kb_index_document", "description", "Index a document for search"),
            Map.of("name", "kb_index_file", "description", "Index a file for search"),
            Map.of("name", "kb_search", "description", "Semantic search across documents"),
            Map.of("name", "kb_get_document", "description", "Retrieve document by ID"),
            Map.of("name", "kb_delete", "description", "Delete document from index"),
            Map.of("name", "kb_list", "description", "List all indexed documents")
        ));
    }

    /**
     * Index a document
     */
    @PostMapping("/tools/kb_index_document")
    public ApiResult<Map<String, Object>> indexDocument(@RequestBody Map<String, Object> params) {
        try {
            String id = params.getOrDefault("id", UUID.randomUUID().toString()).toString();
            String title = (String) params.getOrDefault("title", "Untitled");
            String content = (String) params.get("content");
            String type = (String) params.getOrDefault("type", "text");

            if (content == null || content.isEmpty()) {
                return ApiResult.error("Content is required");
            }

            int chunkSize = (int) params.getOrDefault("chunkSize", 500);
            int overlap = (int) params.getOrDefault("overlap", 50);

            KnowledgeBase.Document doc = new KnowledgeBase.Document(id, title, content, type);
            knowledgeBase.indexDocument(doc, chunkSize, overlap);

            return ApiResult.success(Map.of(
                "documentId", id,
                "title", title,
                "status", "indexed",
                "chunkCount", knowledgeBase.getChunkCount()
            ));
        } catch (Exception e) {
            return ApiResult.error("Indexing failed: " + e.getMessage());
        }
    }

    /**
     * Index a file
     */
    @PostMapping("/tools/kb_index_file")
    public ApiResult<Map<String, Object>> indexFile(@RequestBody Map<String, Object> params) {
        try {
            String filePath = (String) params.get("filePath");
            if (filePath == null || filePath.isEmpty()) {
                return ApiResult.error("File path is required");
            }

            int chunkSize = (int) params.getOrDefault("chunkSize", 500);
            int overlap = (int) params.getOrDefault("overlap", 50);

            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return ApiResult.error("File not found: " + filePath);
            }

            String content = Files.readString(path);
            String fileName = path.getFileName().toString();
            String type = getFileType(fileName);

            String docId = UUID.randomUUID().toString();
            KnowledgeBase.Document doc = new KnowledgeBase.Document(docId, fileName, content, type);
            knowledgeBase.indexDocument(doc, chunkSize, overlap);

            return ApiResult.success(Map.of(
                "documentId", docId,
                "fileName", fileName,
                "status", "indexed",
                "type", type
            ));
        } catch (Exception e) {
            return ApiResult.error("File indexing failed: " + e.getMessage());
        }
    }

    /**
     * Semantic search
     */
    @PostMapping("/tools/kb_search")
    public ApiResult<Map<String, Object>> search(@RequestBody Map<String, Object> params) {
        try {
            String query = (String) params.get("query");
            if (query == null || query.isEmpty()) {
                return ApiResult.error("Query is required");
            }

            int topK = (int) params.getOrDefault("topK", 5);

            KnowledgeBase.SearchResult result = knowledgeBase.search(query, topK);

            List<Map<String, Object>> results = new ArrayList<>();
            for (KnowledgeBase.ScoredChunk chunk : result.results) {
                results.add(Map.of(
                    "chunkId", chunk.chunkId,
                    "documentId", chunk.documentId,
                    "content", chunk.content,
                    "score", chunk.similarity
                ));
            }

            return ApiResult.success(Map.of(
                "query", query,
                "results", results,
                "total", results.size()
            ));
        } catch (Exception e) {
            return ApiResult.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * Get document by ID
     */
    @PostMapping("/tools/kb_get_document")
    public ApiResult<KnowledgeBase.Document> getDocument(@RequestBody Map<String, Object> params) {
        try {
            String documentId = (String) params.get("documentId");
            if (documentId == null || documentId.isEmpty()) {
                return ApiResult.error("Document ID is required");
            }

            KnowledgeBase.Document doc = knowledgeBase.getDocument(documentId);
            if (doc == null) {
                return ApiResult.error("Document not found");
            }

            return ApiResult.success(doc);
        } catch (Exception e) {
            return ApiResult.error("Get document failed: " + e.getMessage());
        }
    }

    /**
     * Delete document
     */
    @PostMapping("/tools/kb_delete")
    public ApiResult<Map<String, Object>> deleteDocument(@RequestBody Map<String, Object> params) {
        try {
            String documentId = (String) params.get("documentId");
            if (documentId == null || documentId.isEmpty()) {
                return ApiResult.error("Document ID is required");
            }

            boolean deleted = knowledgeBase.deleteDocument(documentId);

            return ApiResult.success(Map.of(
                "documentId", documentId,
                "deleted", deleted
            ));
        } catch (Exception e) {
            return ApiResult.error("Delete failed: " + e.getMessage());
        }
    }

    /**
     * List all documents
     */
    @GetMapping("/tools/kb_list")
    public ApiResult<Map<String, Object>> listDocuments() {
        try {
            // This would need to expose list from KnowledgeBase
            return ApiResult.success(Map.of(
                "documentCount", knowledgeBase.getDocumentCount(),
                "chunkCount", knowledgeBase.getChunkCount()
            ));
        } catch (Exception e) {
            return ApiResult.error("List failed: " + e.getMessage());
        }
    }

    private String getFileType(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}