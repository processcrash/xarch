package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.RagDocument;
import com.xarch.example.ai.entity.RagKnowledgeBase;
import com.xarch.example.ai.service.RagService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG controller — retrieval-augmented generation endpoints.
 *
 * <p>Manages {@link RagKnowledgeBase} / {@link RagDocument} entities
 * and exposes a semantic search endpoint that delegates to
 * {@code knowledge-mcp} / {@code vector-mcp} in production.</p>
 */
@Slf4j
@Tag(name = "RAG", description = "Retrieval-augmented generation")
@RestController
@RequestMapping("/ai/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // ==================== Knowledge Base ====================

    /**
     * Page through knowledge bases.
     */
    @GetMapping("/knowledge-bases")
    @Operation(summary = "Page knowledge bases")
    public ApiResult<PageResult<RagKnowledgeBase>> pageKnowledgeBases(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(ragService.pageKnowledgeBases(keyword, pageNum, pageSize));
    }

    /**
     * List every knowledge base (used by the RAG selector UI).
     */
    @GetMapping("/knowledge-bases/all")
    @Operation(summary = "List every knowledge base")
    public ApiResult<List<RagKnowledgeBase>> listKnowledgeBases() {
        return ApiResult.success(ragService.listKnowledgeBases());
    }

    /**
     * Get a knowledge base by id.
     */
    @GetMapping("/knowledge-bases/{id}")
    @Operation(summary = "Get knowledge base detail")
    public ApiResult<RagKnowledgeBase> getKnowledgeBase(@PathVariable Long id) {
        RagKnowledgeBase kb = ragService.getKnowledgeBase(id);
        if (kb == null) {
            return ApiResult.fail("Knowledge base not found");
        }
        return ApiResult.success(kb);
    }

    /**
     * Create a new knowledge base.
     */
    @PostMapping("/knowledge-bases")
    @XarchLog(value = "Create knowledge base", type = "CREATE")
    @Operation(summary = "Create a knowledge base")
    public ApiResult<RagKnowledgeBase> createKnowledgeBase(@RequestBody RagKnowledgeBase kb) {
        kb.setCreateUserId(StpUtil.getLoginIdAsLong());
        kb.setCreateUserName(StpUtil.getLoginIdAsString());
        return ApiResult.success(ragService.createKnowledgeBase(kb));
    }

    /**
     * Update a knowledge base.
     */
    @PutMapping("/knowledge-bases/{id}")
    @XarchLog(value = "Update knowledge base", type = "UPDATE")
    @Operation(summary = "Update a knowledge base")
    public ApiResult<RagKnowledgeBase> updateKnowledgeBase(@PathVariable Long id,
                                                           @RequestBody RagKnowledgeBase kb) {
        kb.setId(id);
        return ApiResult.success(ragService.updateKnowledgeBase(kb));
    }

    /**
     * Delete a knowledge base.
     */
    @DeleteMapping("/knowledge-bases/{id}")
    @XarchLog(value = "Delete knowledge base", type = "DELETE")
    @Operation(summary = "Delete a knowledge base")
    public ApiResult<Void> deleteKnowledgeBase(@PathVariable Long id) {
        ragService.deleteKnowledgeBase(id);
        return ApiResult.success(null);
    }

    // ==================== Documents ====================

    /**
     * Page documents in a knowledge base.
     */
    @GetMapping("/knowledge-bases/{id}/documents")
    @Operation(summary = "Page documents in a knowledge base")
    public ApiResult<PageResult<RagDocument>> pageDocuments(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(ragService.pageDocuments(id, keyword, pageNum, pageSize));
    }

    /**
     * Ingest a document into a knowledge base.
     */
    @PostMapping("/knowledge-bases/{id}/ingest")
    @XarchLog(value = "Ingest RAG document", type = "CREATE")
    @Operation(summary = "Ingest a document into a knowledge base")
    public ApiResult<RagDocument> ingest(@PathVariable Long id, @RequestBody IngestRequest request) {
        RagService.IngestRequest svcRequest = new RagService.IngestRequest();
        svcRequest.setKnowledgeBaseId(id);
        svcRequest.setTitle(request.getTitle());
        svcRequest.setSourceUri(request.getSourceUri());
        svcRequest.setContentType(request.getContentType());
        svcRequest.setCreateUserId(StpUtil.getLoginIdAsLong());
        svcRequest.setCreateUserName(StpUtil.getLoginIdAsString());
        return ApiResult.success(ragService.ingest(svcRequest));
    }

    /**
     * Delete a RAG document.
     */
    @DeleteMapping("/documents/{id}")
    @XarchLog(value = "Delete RAG document", type = "DELETE")
    @Operation(summary = "Delete a RAG document")
    public ApiResult<Void> deleteDocument(@PathVariable Long id) {
        ragService.deleteDocument(id);
        return ApiResult.success(null);
    }

    // ==================== Search ====================

    /**
     * Run a semantic search over a knowledge base.
     */
    @PostMapping("/search")
    @XarchLog(value = "RAG search", type = "QUERY")
    @Operation(summary = "Run a semantic search over a knowledge base")
    public ApiResult<List<RagService.SearchHit>> search(@RequestBody SearchRequest request) {
        RagService.SearchRequest svcRequest = new RagService.SearchRequest();
        svcRequest.setKnowledgeBaseId(request.getKnowledgeBaseId());
        svcRequest.setQuery(request.getQuery());
        svcRequest.setTopK(request.getTopK() != null ? request.getTopK() : 5);
        return ApiResult.success(ragService.search(svcRequest));
    }

    /** Ingest request payload. */
    @Data
    public static class IngestRequest {
        private String title;
        private String sourceUri;
        private String contentType;
    }

    /** Search request payload. */
    @Data
    public static class SearchRequest {
        private Long knowledgeBaseId;
        private String query;
        private Integer topK;
    }
}
