package com.xarch.mcp.database.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.database.DatabaseClient;
import com.xarch.mcp.database.DatabaseConfig;
import com.xarch.mcp.runtime.stdio.StdioMcpServer.ContentBlock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool handlers for the database MCP server. Each method maps to a single
 * tool exposed via {@link com.xarch.mcp.runtime.stdio.StdioMcpServer#tool}.
 */
public final class DatabaseTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DatabaseTools() {}

    public static List<ContentBlock> configure(DatabaseConfig config, JsonNode args) {
        if (args.has("type")) config.setType(args.path("type").asText());
        if (args.has("host")) config.setHost(args.path("host").asText());
        if (args.has("port")) config.setPort(args.path("port").asInt());
        if (args.has("database")) config.setDatabase(args.path("database").asText());
        if (args.has("username")) config.setUsername(args.path("username").asText());
        if (args.has("password")) config.setPassword(args.path("password").asText());
        if (args.has("ssl")) config.setSsl(args.path("ssl").asBoolean());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "configured");
        result.put("type", config.getType());
        result.put("host", config.getHost());
        result.put("port", config.getPort());
        result.put("database", config.getDatabase());
        result.put("username", config.getUsername());
        result.put("password", config.maskedPassword());
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> query(DatabaseClient client, JsonNode args) {
        if (!args.has("sql")) throw new IllegalArgumentException("sql is required");
        String sql = args.path("sql").asText();
        Map<String, Object> params = args.has("parameters") && args.path("parameters").isObject()
                ? MAPPER.convertValue(args.path("parameters"), Map.class)
                : Map.of();
        List<Map<String, Object>> rows = client.query(sql, params);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rowCount", rows.size());
        result.put("rows", rows);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> update(DatabaseClient client, JsonNode args) {
        if (!args.has("sql")) throw new IllegalArgumentException("sql is required");
        String sql = args.path("sql").asText();
        Map<String, Object> params = args.has("parameters") && args.path("parameters").isObject()
                ? MAPPER.convertValue(args.path("parameters"), Map.class)
                : Map.of();
        int affected = client.update(sql, params);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("affectedRows", affected);
        result.put("sql", sql);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> schema(DatabaseClient client) {
        List<String> ddl = client.getSchema();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statementCount", ddl.size());
        result.put("statements", ddl);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> listTables(DatabaseClient client) {
        List<String> tables = client.listTables();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tableCount", tables.size());
        result.put("tables", tables);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> describeTable(DatabaseClient client, JsonNode args) {
        if (!args.has("table")) throw new IllegalArgumentException("table is required");
        String table = args.path("table").asText();
        Map<String, Object> info = client.describeTable(table);
        return List.of(ContentBlock.text(toJson(info)));
    }

    public static List<ContentBlock> listIndexes(DatabaseClient client, JsonNode args) {
        String table = args.has("table") ? args.path("table").asText() : null;
        List<Map<String, Object>> indexes = client.listIndexes(table);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("indexCount", indexes.size());
        result.put("indexes", indexes);
        return List.of(ContentBlock.text(toJson(result)));
    }

    public static List<ContentBlock> health(DatabaseConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("service", "database-mcp");
        result.put("version", "1.0.0");
        result.put("configured", config != null && !config.getDatabase().isEmpty());
        result.put("host", config.getHost());
        result.put("port", config.getPort());
        return List.of(ContentBlock.text(toJson(result)));
    }

    private static String toJson(Object value) {
        try { return MAPPER.writeValueAsString(value); }
        catch (Exception e) { return "{\"error\":\"" + e.getMessage() + "\"}"; }
    }
}