package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MCP (Model Context Protocol) server registry entry.
 *
 * <p>Tracks external MCP-compatible servers that the AI agent can route
 * tool calls through. Discovery can come from Nacos, a config file, or
 * manual registration through {@code McpManagementController}.</p>
 */
@Data
@Table("xarch_ai_mcp_server")
public class McpServer implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    /** Display name for the MCP server. */
    private String name;

    /** MCP server base URL (e.g. https://mcp.example.com). */
    private String baseUrl;

    /** Transport protocol: sse / stdio / websocket. */
    private String transport;

    /** API key or bearer token used to authenticate. */
    private String apiKey;

    /** Free-form description shown in the management UI. */
    private String description;

    /** Health-check status: 0=unknown, 1=healthy, 2=unhealthy. */
    private Integer healthStatus;

    /** Last successful health check timestamp. */
    private LocalDateTime lastHealthCheck;

    /** Last health-check error message, if any. */
    private String lastError;

    /** Tag list (comma-separated) used for grouping in the UI. */
    private String tags;

    private Long createUserId;
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}
