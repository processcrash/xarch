package com.xarch.mcp.database.prompts;

import com.fasterxml.jackson.databind.JsonNode;

/** Prompt renderers for the database MCP server. */
public final class DatabasePrompts {

    private DatabasePrompts() {}

    public static String sqlQuery(JsonNode args) {
        String database = args.path("database").asText("");
        String intent = args.path("intent").asText("");
        return "You are a SQL expert. Given the database schema and the user's intent, "
                + "produce a single safe parameterized SQL statement.\n\n"
                + "Database: " + database + "\n"
                + "Intent: " + intent + "\n\n"
                + "Constraints:\n"
                + "- Use parameter placeholders ($1, :name, ?) instead of string interpolation\n"
                + "- Never DROP / TRUNCATE without an explicit 'safe: true' parameter\n"
                + "- Add comments explaining each clause\n\n"
                + "SQL:";
    }
}