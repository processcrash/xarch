package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.McpServer;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * MCP server management service contract.
 *
 * <p>Operations on the {@link McpServer} registry plus a health check
 * that probes each registered server. The current implementation is a
 * stub — production code should plug in a real MCP client SDK.</p>
 */
public interface McpServerService {

    /**
     * Page through registered MCP servers.
     */
    PageResult<McpServer> page(String keyword, String tag, Integer healthStatus,
                               int pageNum, int pageSize);

    /**
     * List all registered MCP servers (used by the tool router).
     */
    List<McpServer> listAll();

    /**
     * Get an MCP server by primary key.
     */
    McpServer getById(Long id);

    /**
     * Register a new MCP server.
     */
    McpServer register(McpServer server);

    /**
     * Update an existing MCP server.
     */
    McpServer update(McpServer server);

    /**
     * Remove an MCP server.
     */
    void remove(Long id);

    /**
     * Probe a single MCP server for liveness.
     *
     * @return {@code true} when the server is reachable and the MCP
     *         handshake succeeds
     */
    boolean healthCheck(Long id);
}
