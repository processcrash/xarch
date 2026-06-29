package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.McpServer;
import com.xarch.example.ai.service.McpServerService;
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
 * MCP server management controller — administrative endpoints for the
 * MCP server registry.
 *
 * <p>Allows registering, updating, removing and probing MCP servers.
 * In production the health check should perform a real MCP handshake;
 * the current implementation marks servers as healthy on registration
 * and during an explicit {@code /health} call.</p>
 */
@Slf4j
@Tag(name = "MCP Management", description = "MCP server registry administration")
@RestController
@RequestMapping("/ai/mcp/management")
@RequiredArgsConstructor
public class McpManagementController {

    private final McpServerService mcpServerService;

    /**
     * Page through registered MCP servers.
     */
    @GetMapping("/servers")
    @Operation(summary = "Page registered MCP servers")
    public ApiResult<PageResult<McpServer>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer healthStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(mcpServerService.page(keyword, tag, healthStatus, pageNum, pageSize));
    }

    /**
     * List all registered MCP servers (used by the tool router).
     */
    @GetMapping("/servers/all")
    @Operation(summary = "List all registered MCP servers")
    public ApiResult<List<McpServer>> listAll() {
        return ApiResult.success(mcpServerService.listAll());
    }

    /**
     * Get an MCP server by id.
     */
    @GetMapping("/servers/{id}")
    @Operation(summary = "Get MCP server detail")
    public ApiResult<McpServer> detail(@PathVariable Long id) {
        McpServer server = mcpServerService.getById(id);
        if (server == null) {
            return ApiResult.fail("MCP server not found");
        }
        // Mask api key
        server.setApiKey(null);
        return ApiResult.success(server);
    }

    /**
     * Register a new MCP server.
     */
    @PostMapping("/servers")
    @XarchLog(value = "Register MCP server", type = "CREATE")
    @Operation(summary = "Register a new MCP server")
    public ApiResult<McpServer> register(@RequestBody McpServer server) {
        server.setCreateUserId(StpUtil.getLoginIdAsLong());
        server.setCreateUserName(StpUtil.getLoginIdAsString());
        return ApiResult.success(mcpServerService.register(server));
    }

    /**
     * Update an existing MCP server.
     */
    @PutMapping("/servers/{id}")
    @XarchLog(value = "Update MCP server", type = "UPDATE")
    @Operation(summary = "Update an MCP server")
    public ApiResult<McpServer> update(@PathVariable Long id, @RequestBody McpServer server) {
        server.setId(id);
        return ApiResult.success(mcpServerService.update(server));
    }

    /**
     * Remove an MCP server (logical delete).
     */
    @DeleteMapping("/servers/{id}")
    @XarchLog(value = "Remove MCP server", type = "DELETE")
    @Operation(summary = "Remove an MCP server")
    public ApiResult<Void> remove(@PathVariable Long id) {
        mcpServerService.remove(id);
        return ApiResult.success(null);
    }

    /**
     * Probe the health of a single MCP server.
     */
    @PostMapping("/servers/{id}/health")
    @XarchLog(value = "Health check MCP server", type = "OPERATION")
    @Operation(summary = "Probe a single MCP server for liveness")
    public ApiResult<HealthCheckResult> healthCheck(@PathVariable Long id) {
        boolean healthy = mcpServerService.healthCheck(id);
        return ApiResult.success(new HealthCheckResult(id, healthy ? 1 : 2, healthy ? "healthy" : "unhealthy"));
    }

    /**
     * Probe every registered MCP server.
     */
    @PostMapping("/servers/health-all")
    @XarchLog(value = "Health check all MCP servers", type = "OPERATION")
    @Operation(summary = "Probe all registered MCP servers")
    public ApiResult<List<HealthCheckResult>> healthCheckAll() {
        List<McpServer> servers = mcpServerService.listAll();
        List<HealthCheckResult> results = servers.stream()
                .map(s -> {
                    boolean healthy = mcpServerService.healthCheck(s.getId());
                    return new HealthCheckResult(s.getId(), healthy ? 1 : 2,
                            healthy ? "healthy" : "unhealthy");
                })
                .toList();
        return ApiResult.success(results);
    }

    /** Health check result payload. */
    @Data
    public static class HealthCheckResult {
        private Long serverId;
        private Integer status;
        private String statusLabel;

        public HealthCheckResult(Long serverId, Integer status, String statusLabel) {
            this.serverId = serverId;
            this.status = status;
            this.statusLabel = statusLabel;
        }
    }
}
