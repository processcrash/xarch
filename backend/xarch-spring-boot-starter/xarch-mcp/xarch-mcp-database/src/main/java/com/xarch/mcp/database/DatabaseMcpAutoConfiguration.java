package com.xarch.mcp.database;

import com.alibaba.cloud.nacos.serviceregistry.NacosServiceRegistry;
import com.xarch.cloud.nacos.annotation.McpServer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Database MCP Server auto-configuration for Nacos registration
 */
@Configuration
@ConditionalOnClass(NacosServiceRegistry.class)
public class DatabaseMcpAutoConfiguration {

    @Autowired
    private NacosServiceRegistry nacosServiceRegistry;

    @PostConstruct
    public void init() {
        // Register database MCP service to Nacos
        // Service will be registered with name: xarch-mcp-database
    }
}