package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP Management Controller — administrative endpoints for MCP servers.
 *
 * <p><b>Status: planned.</b> Real implementation lands when the MCP
 * client integration is migrated from the monolith.
 */
@Tag(name = "MCP Management", description = "MCP server registry (planned)")
@RestController
@RequestMapping("/ai/mcp/management")
@RequiredArgsConstructor
public class McpManagementController {

    /**
     * List registered MCP servers.
     */
    @GetMapping("/servers")
    public ApiResult<List<Map<String, Object>>> listServers() {
        return ApiResult.success(List.of());
    }

    /**
     * Register a new MCP server.
     */
    @PostMapping("/servers")
    public ApiResult<Void> register(@RequestBody Map<String, Object> payload) {
        return ApiResult.ok();
    }

    /**
     * Remove an MCP server.
     */
    @DeleteMapping("/servers/{id}")
    public ApiResult<Void> remove(@PathVariable Long id) {
        return ApiResult.ok();
    }
}