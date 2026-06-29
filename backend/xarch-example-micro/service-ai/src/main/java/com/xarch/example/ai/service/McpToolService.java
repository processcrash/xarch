package com.xarch.example.ai.service;

import java.util.List;
import java.util.Map;

/**
 * MCP tool service contract — discovers and invokes tools provided by
 * registered MCP servers.
 *
 * <p>Production wiring should talk to the upstream MCP server using
 * the MCP JSON-RPC protocol. The current implementation returns a
 * deterministic stub so the API surface is exercisable.</p>
 */
public interface McpToolService {

    /**
     * List tools exposed by a single MCP server.
     */
    List<Map<String, Object>> listTools(Long serverId);

    /**
     * List tools exposed by every registered MCP server.
     */
    List<Map<String, Object>> listAllTools();

    /**
     * Invoke a tool on a specific MCP server.
     */
    Map<String, Object> invoke(Long serverId, String toolName, Map<String, Object> arguments);

    /**
     * Page through tool execution history.
     */
    com.xarch.starter.core.result.PageResult<com.xarch.example.ai.entity.CommandAudit>
            pageHistory(Long serverId, Long userId, int pageNum, int pageSize);

    /** Tool invocation request bundle. */
    final class ToolInvokeRequest {
        private Long serverId;
        private String toolName;
        private Map<String, Object> arguments;
        private Long userId;

        public Long getServerId() { return serverId; }
        public void setServerId(Long serverId) { this.serverId = serverId; }
        public String getToolName() { return toolName; }
        public void setToolName(String toolName) { this.toolName = toolName; }
        public Map<String, Object> getArguments() { return arguments; }
        public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
