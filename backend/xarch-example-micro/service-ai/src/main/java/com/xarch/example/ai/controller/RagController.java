package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG Controller — retrieval-augmented generation endpoints.
 *
 * <p><b>Status: planned.</b>
 */
@Tag(name = "RAG", description = "Retrieval-augmented generation (planned)")
@RestController
@RequestMapping("/ai/rag")
@RequiredArgsConstructor
public class RagController {

    /**
     * Query the RAG pipeline.
     */
    @PostMapping("/query")
    public ApiResult<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        return ApiResult.success(Map.of("answer", "stub answer"));
    }

    /**
     * List knowledge bases.
     */
    @GetMapping("/knowledge-bases")
    public ApiResult<List<Map<String, Object>>> knowledgeBases() {
        return ApiResult.success(List.of());
    }

    /**
     * Ingest documents into a knowledge base.
     */
    @PostMapping("/knowledge-bases/{id}/ingest")
    public ApiResult<Void> ingest(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResult.ok();
    }
}