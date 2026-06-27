package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Task Controller — async AI task tracking.
 *
 * <p><b>Status: planned.</b>
 */
@Tag(name = "AI Tasks", description = "Async AI task tracking (planned)")
@RestController
@RequestMapping("/ai/tasks")
@RequiredArgsConstructor
public class TaskController {

    /**
     * List recent tasks.
     */
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        return ApiResult.success(List.of());
    }

    /**
     * Get task by id.
     */
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.success(Map.of("id", id, "status", "stub"));
    }

    /**
     * Cancel a running task.
     */
    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        return ApiResult.ok();
    }
}