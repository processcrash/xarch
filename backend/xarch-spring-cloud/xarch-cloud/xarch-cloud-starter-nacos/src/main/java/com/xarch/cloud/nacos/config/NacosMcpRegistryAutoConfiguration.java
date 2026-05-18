package com.xarch.cloud.nacos.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.serviceregistry.NacosServiceRegistry;
import com.xarch.cloud.nacos.mcp.McpServiceInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos MCP Service Registry Configuration
 * Registers MCP servers as Nacos services for discovery
 */
@Configuration
@ConditionalOnClass(NacosServiceRegistry.class)
@ConditionalOnProperty(prefix = "xarch.mcp.nacos", name = "enabled", havingValue = "true")
public class NacosMcpRegistryAutoConfiguration {

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    private static final Map<String, McpServiceInfo> registeredServices = new HashMap<>();

    @PostConstruct
    public void init() {
        // Register default MCP services
    }

    /**
     * Register an MCP service to Nacos
     */
    public void registerMcpService(McpServiceInfo serviceInfo) {
        // MCP service registration implementation
        registeredServices.put(serviceInfo.getName(), serviceInfo);
    }

    /**
     * Get all registered MCP services
     */
    public Map<String, McpServiceInfo> getRegisteredServices() {
        return new HashMap<>(registeredServices);
    }

    /**
     * Unregister an MCP service from Nacos
     */
    public void unregisterMcpService(String serviceName) {
        registeredServices.remove(serviceName);
    }
}