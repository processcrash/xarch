package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.CommandAudit;
import com.xarch.example.ai.service.McpToolService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool controller — exposes tools provided by registered MCP
 * servers and routes invocation requests.
 *
 * <p>Production wiring should serialise requests as JSON-RPC and POST
 * them to the upstream MCP server. The current implementation returns
 * a deterministic stub result.</p>
 */
@Slf4j
@Tag(name = "MCP Tools", description = "MCP tool discovery and invocation")
@RestController
@RequestMapping("/ai/mcp/tools")
@RequiredArgsConstructor
public class McpToolController {

    private final McpToolService mcpToolService;

    /**
     * List tools exposed by a single MCP server.
     */
    @GetMapping("/list")
    @Operation(summary = "List tools exposed by an MCP server")
    public ApiResult<List<Map<String, Object>>> list(@RequestParam Long serverId) {
        return ApiResult.success(mcpToolService.listTools(serverId));
    }

    /**
     * List tools exposed by every registered MCP server.
     */
    @GetMapping("/list-all")
    @Operation(summary = "List tools exposed by every registered MCP server")
    public ApiResult<List<Map<String, Object>>> listAll() {
        return ApiResult.success(mcpToolService.listAllTools());
    }

    /**
     * Invoke a tool on a specific MCP server.
     */
    @PostMapping("/invoke")
    @XarchLog(value = "Invoke MCP tool", type = "OPERATION")
    @Operation(summary = "Invoke a tool on a specific MCP server")
    public ApiResult<Map<String, Object>> invoke(@RequestBody InvokeRequest request) {
        try {
            Map<String, Object> arguments = request.getArguments() != null
                    ? request.getArguments()
                    : new HashMap<>();
            arguments.putIfAbsent("userId", StpUtil.getLoginIdAsLong());
            return ApiResult.success(
                    mcpToolService.invoke(request.getServerId(), request.getToolName(), arguments));
        } catch (Exception e) {
            log.warn("MCP tool invocation failed: {}", e.getMessage());
            return ApiResult.fail("Tool invocation failed: " + e.getMessage());
        }
    }

    /**
     * Page through tool execution history.
     */
    @GetMapping("/history")
    @Operation(summary = "Page through MCP tool execution history")
    public ApiResult<PageResult<CommandAudit>> history(
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(mcpToolService.pageHistory(serverId, userId, pageNum, pageSize));
    }

    /** Tool invocation request payload. */
    @Data
    public static class InvokeRequest {
        private Long serverId;
        private String toolName;
        private Map<String, Object> arguments;
    }
}
