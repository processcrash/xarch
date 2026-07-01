package com.xarch.mcp.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xarch.mcp.runtime.stdio.StdioMcpServer;

import java.util.List;

/**
 * Entry point for the database MCP stdio server.
 *
 * <p>Wires the 8 tools + 1 resource + 1 prompt defined by the database MCP
 * surface onto the shared {@link StdioMcpServer} runtime.
 */
public class DatabaseMcpServerMain {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        DatabaseConfig config = new DatabaseConfig();
        DatabaseClient client = new DatabaseClient(config);

        new StdioMcpServer("xarch-database-mcp", "1.0.0")
            .tool("configure", "Configure database connection", configureSchema(), a -> tools.DatabaseTools.configure(config, a))
            .tool("query_execute", "Execute a read-only SQL query", querySchema(), a -> tools.DatabaseTools.query(client, a))
            .tool("execute_update", "Execute INSERT/UPDATE/DELETE", updateSchema(), a -> tools.DatabaseTools.update(client, a))
            .tool("schema_get", "Get full database schema", emptySchema(), a -> tools.DatabaseTools.schema(client))
            .tool("table_list", "List all tables", emptySchema(), a -> tools.DatabaseTools.listTables(client))
            .tool("table_describe", "Describe a table", describeSchema(), a -> tools.DatabaseTools.describeTable(client, a))
            .tool("index_list", "List indexes", indexSchema(), a -> tools.DatabaseTools.listIndexes(client, a))
            .tool("health", "Health check", emptySchema(), a -> tools.DatabaseTools.health(config))
            .resource("config://current", "Database Configuration", "Current connection config (password masked)", "application/json",
                a -> resources.DatabaseResources.current(config))
            .prompt("sql-query", "Generate SQL for a natural-language intent", List.of(
                new StdioMcpServer.PromptArgument("database", "Target database name", true),
                new StdioMcpServer.PromptArgument("intent", "Natural-language intent", true)
            ), a -> prompts.DatabasePrompts.sqlQuery(a))
            .run();
    }

    // ---- input schemas ----

    private static ObjectNode emptySchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        s.putObject("properties");
        return s;
    }

    private static ObjectNode configureSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("type").put("type", "string").put("description", "Database type: mysql|postgresql");
        p.putObject("host").put("type", "string");
        p.putObject("port").put("type", "integer").put("default", 3306);
        p.putObject("database").put("type", "string");
        p.putObject("username").put("type", "string");
        p.putObject("password").put("type", "string");
        p.putObject("ssl").put("type", "boolean").put("default", false);
        return s;
    }

    private static ObjectNode querySchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("sql").put("type", "string").put("description", "SQL SELECT to execute");
        p.putObject("parameters").put("type", "object").put("description", "Bound parameters");
        s.putArray("required").add("sql");
        return s;
    }

    private static ObjectNode updateSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("sql").put("type", "string");
        p.putObject("parameters").put("type", "object");
        s.putArray("required").add("sql");
        return s;
    }

    private static ObjectNode describeSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("table").put("type", "string").put("description", "Table name to describe");
        s.putArray("required").add("table");
        return s;
    }

    private static ObjectNode indexSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("table").put("type", "string").put("description", "Table name (omit for all tables)");
        return s;
    }
}