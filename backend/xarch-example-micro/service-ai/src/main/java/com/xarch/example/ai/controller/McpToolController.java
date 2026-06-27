package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Controller — invokes tools exposed by MCP servers.
 *
 * <p><b>Status: planned.</b>
 */
@Tag(name = "MCP Tools", description = "MCP tool invocation (planned)")
@RestController
@RequestMapping("/ai/mcp/tools")
@RequiredArgsConstructor
public class McpToolController {

    /**
     * List tools provided by an MCP server.
     */
    @GetMapping("/list")
    public ApiResult<List<Map<String, Object>>> list(@RequestParam String serverId) {
        return ApiResult.success(List.of());
    }

    /**
     * Invoke a tool by name.
     */
    @PostMapping("/invoke")
    public ApiResult<Map<String, Object>> invoke(@RequestBody Map<String, Object> payload) {
        return ApiResult.success(Map.of("result", "stub"));
    }
}