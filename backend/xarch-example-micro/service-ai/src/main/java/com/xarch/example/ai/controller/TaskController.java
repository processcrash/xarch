package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.AiTask;
import com.xarch.example.ai.service.AiTaskService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Async AI task controller — submits, tracks and cancels long-running
 * AI work (model fine-tuning, bulk RAG ingest, batch tool execution).
 *
 * <p>Production code should drive task execution through an async
 * worker pool; the current scaffold persists the task records and
 * exposes full CRUD.</p>
 */
@Slf4j
@Tag(name = "AI Tasks", description = "Async AI task tracking")
@RestController
@RequestMapping("/ai/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final AiTaskService aiTaskService;

    /**
     * Page through tasks.
     */
    @GetMapping
    @Operation(summary = "Page through tasks")
    public ApiResult<PageResult<AiTask>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(aiTaskService.page(keyword, type, status, pageNum, pageSize));
    }

    /**
     * List the most recent N tasks.
     */
    @GetMapping("/recent")
    @Operation(summary = "List the most recent N tasks")
    public ApiResult<List<AiTask>> recent(@RequestParam(defaultValue = "10") int limit) {
        return ApiResult.success(aiTaskService.listRecent(limit));
    }

    /**
     * Get task by id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task detail")
    public ApiResult<AiTask> detail(@PathVariable Long id) {
        AiTask task = aiTaskService.getById(id);
        if (task == null) {
            return ApiResult.fail("Task not found");
        }
        return ApiResult.success(task);
    }

    /**
     * Get task by code.
     */
    @GetMapping("/by-code/{code}")
    @Operation(summary = "Get task detail by code")
    public ApiResult<AiTask> getByCode(@PathVariable String code) {
        AiTask task = aiTaskService.getByCode(code);
        if (task == null) {
            return ApiResult.fail("Task not found");
        }
        return ApiResult.success(task);
    }

    /**
     * Submit a new task.
     */
    @PostMapping
    @XarchLog(value = "Submit task", type = "CREATE")
    @Operation(summary = "Submit a new async task")
    public ApiResult<AiTask> submit(@RequestBody AiTask task) {
        task.setCreateUserId(StpUtil.getLoginIdAsLong());
        task.setCreateUserName(StpUtil.getLoginIdAsString());
        return ApiResult.success(aiTaskService.submit(task));
    }

    /**
     * Cancel a running task.
     */
    @PostMapping("/{id}/cancel")
    @XarchLog(value = "Cancel task", type = "OPERATION")
    @Operation(summary = "Cancel a running task")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        boolean ok = aiTaskService.cancel(id);
        return ok ? ApiResult.success(null) : ApiResult.fail("Task cannot be cancelled");
    }

    /**
     * Get task execution history snapshot.
     */
    @GetMapping("/{id}/history")
    @Operation(summary = "Get task execution history snapshot")
    public ApiResult<Map<String, Object>> history(@PathVariable Long id) {
        return ApiResult.success(aiTaskService.history(id));
    }

    /**
     * Delete a task (logical delete).
     */
    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete task", type = "DELETE")
    @Operation(summary = "Delete a task")
    public ApiResult<Void> delete(@PathVariable Long id) {
        aiTaskService.delete(id);
        return ApiResult.success(null);
    }
}
