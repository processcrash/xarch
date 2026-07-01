package com.xarch.mcp.vector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory vector store configuration. Mirrors the Node.js sibling's
 * config shape but is process-local (no remote connection).
 */
public class VectorConfig {
    private String type = "in-memory";
    private int defaultDimension = 1536;
    private String host = "";
    private int port = 0;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDefaultDimension() { return defaultDimension; }
    public void setDefaultDimension(int defaultDimension) { this.defaultDimension = defaultDimension; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("defaultDimension", defaultDimension);
        m.put("host", host);
        m.put("port", port);
        return m;
    }
}