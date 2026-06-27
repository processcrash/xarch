package com.xarch.example.ai.controller;

import com.xarch.example.ai.entity.UserBehavior;
import com.xarch.example.ai.mapper.UserBehaviorMapper;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Behaviour Controller — records and analyses AI user actions.
 *
 * <p>Writes directly through {@link UserBehaviorMapper}; no service
 * layer is required for the simple append-only pattern.
 */
@Tag(name = "User Behaviour", description = "AI user-behaviour tracking")
@RestController
@RequestMapping("/ai/behaviors")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorMapper userBehaviorMapper;

    /**
     * Record a behaviour event.
     */
    @PostMapping
    public ApiResult<Void> record(@RequestBody UserBehavior behavior) {
        behavior.setCreateTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);
        return ApiResult.ok();
    }

    /**
     * Page query behaviour events.
     */
    @GetMapping
    public ApiResult<PageResult<UserBehavior>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        // Stub: real query lands when the migration continues.
        return ApiResult.ok(List.of());
    }
}