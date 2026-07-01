package com.xarch.mcp.database.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.database.DatabaseConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/** Resource providers for the database MCP server. */
public final class DatabaseResources {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DatabaseResources() {}

    public static String current(DatabaseConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", config.getType());
        result.put("host", config.getHost());
        result.put("port", config.getPort());
        result.put("database", config.getDatabase());
        result.put("username", config.getUsername());
        result.put("password", config.maskedPassword());
        result.put("ssl", config.isSsl());
        try { return MAPPER.writeValueAsString(result); }
        catch (Exception e) { return "{}"; }
    }
}