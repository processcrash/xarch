package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.CommandAudit;
import com.xarch.example.ai.entity.McpServer;
import com.xarch.example.ai.mapper.CommandAuditMapper;
import com.xarch.example.ai.mapper.McpServerMapper;
import com.xarch.example.ai.service.McpServerService;
import com.xarch.example.ai.service.McpToolService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stub McpTool service — produces deterministic tool descriptors and
 * invocation results. Production code should route invocations to the
 * real MCP server using JSON-RPC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolServiceImpl implements McpToolService {

    private final McpServerMapper mcpServerMapper;
    private final McpServerService mcpServerService;
    private final CommandAuditMapper commandAuditMapper;

    @Override
    public List<Map<String, Object>> listTools(Long serverId) {
        McpServer server = mcpServerService.getById(serverId);
        if (server == null) {
            return List.of();
        }
        return List.of(
                Map.of(
                        "name", "echo",
                        "description", "Echoes back the supplied argument",
                        "serverId", serverId,
                        "serverName", server.getName()
                ),
                Map.of(
                        "name", "health",
                        "description", "Reports the health of the MCP server",
                        "serverId", serverId,
                        "serverName", server.getName()
                )
        );
    }

    @Override
    public List<Map<String, Object>> listAllTools() {
        List<McpServer> servers = mcpServerService.listAll();
        return servers.stream()
                .flatMap(s -> listTools(s.getId()).stream())
                .toList();
    }

    @Override
    public Map<String, Object> invoke(Long serverId, String toolName, Map<String, Object> arguments) {
        // Production: serialise to JSON-RPC and POST to the MCP server.
        // For the scaffold we return a deterministic stub result.
        try {
            McpServer server = mcpServerService.getById(serverId);
            if (server == null) {
                return Map.of("error", "MCP server not found");
            }
            recordAudit(serverId, toolName, arguments);
            return Map.of(
                    "serverId", serverId,
                    "serverName", server.getName(),
                    "tool", toolName,
                    "result", "stub: invoked " + toolName + " with " + arguments
            );
        } catch (Exception e) {
            log.warn("McpToolService.invoke failed: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public PageResult<CommandAudit> pageHistory(Long serverId, Long userId, int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create()
                    .where("del_flag = 0 AND ai_prompt IS NOT NULL");
            if (serverId != null) {
                wrapper.and("server_id = ?", serverId);
            }
            if (userId != null) {
                wrapper.and("user_id = ?", userId);
            }
            wrapper.orderBy("create_time", false);
            var page = commandAuditMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("McpToolService.pageHistory unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    private void recordAudit(Long serverId, String toolName, Map<String, Object> arguments) {
        try {
            CommandAudit audit = new CommandAudit();
            audit.setServerId(serverId);
            audit.setCommand("[mcp] " + toolName);
            audit.setAiPrompt("tool:" + toolName + " args:" + arguments);
            audit.setStatus(1);
            audit.setDelFlag(0);
            audit.setCreateTime(LocalDateTime.now());
            audit.setUpdateTime(LocalDateTime.now());
            commandAuditMapper.insert(audit);
        } catch (Exception e) {
            log.warn("McpToolService.recordAudit failed: {}", e.getMessage());
        }
    }
}
