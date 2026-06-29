package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.UserBehavior;
import com.xarch.example.ai.service.UserBehaviorService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI user-behaviour controller — records and analyses user actions
 * performed in the AI product (chat, tool invocation, RAG query, ...).
 *
 * <p>Persists through {@link UserBehaviorService}. Behaviour
 * statistics are computed in Java for the scaffold; production code
 * should push them into ClickHouse / Elasticsearch for scale.</p>
 */
@Slf4j
@Tag(name = "User Behaviour", description = "AI user-behaviour tracking")
@RestController
@RequestMapping("/ai/behaviors")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    /**
     * Record a behaviour event.
     */
    @PostMapping
    @XarchLog(value = "Record user behaviour", type = "OPERATION")
    @Operation(summary = "Record a behaviour event")
    public ApiResult<UserBehavior> record(@RequestBody UserBehavior behavior) {
        if (behavior.getUserId() == null) {
            behavior.setUserId(StpUtil.getLoginIdAsLong());
        }
        if (behavior.getUserName() == null) {
            behavior.setUserName(StpUtil.getLoginIdAsString());
        }
        behavior.setCreateTime(LocalDateTime.now());
        return ApiResult.success(userBehaviorService.record(behavior));
    }

    /**
     * Page through behaviour events.
     */
    @GetMapping
    @Operation(summary = "Page through behaviour events")
    public ApiResult<PageResult<UserBehavior>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(userBehaviorService.page(userId, action, pageNum, pageSize));
    }

    /**
     * Recent behaviour for a user.
     */
    @GetMapping("/recent")
    @Operation(summary = "List the most recent behaviour for a user")
    public ApiResult<List<UserBehavior>> recent(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        Long resolvedUserId = userId != null ? userId : StpUtil.getLoginIdAsLong();
        return ApiResult.success(userBehaviorService.recentByUser(resolvedUserId, limit));
    }

    /**
     * Aggregate behaviour statistics for a user.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get behaviour statistics")
    public ApiResult<Map<String, Object>> statistics(
            @RequestParam(required = false) Long userId) {
        return ApiResult.success(userBehaviorService.statistics(userId));
    }

    /**
     * Behaviour analysis for the current user.
     */
    @GetMapping("/analysis")
    @Operation(summary = "Get behaviour analysis for the current user")
    public ApiResult<Map<String, Object>> analysis() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(userBehaviorService.statistics(userId));
    }
}
