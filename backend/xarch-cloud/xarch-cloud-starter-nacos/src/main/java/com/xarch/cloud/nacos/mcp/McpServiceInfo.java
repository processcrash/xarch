package com.xarch.cloud.nacos.mcp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * MCP Service Information for Nacos registration
 */
public class McpServiceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private String host;
    private int port;
    private String version = "1.0.0";
    private List<String> capabilities;
    private Map<String, String> metadata;

    public McpServiceInfo() {
    }

    public McpServiceInfo(String name, String type, String host, int port) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getServiceId() {
        return name + ":" + version;
    }
}