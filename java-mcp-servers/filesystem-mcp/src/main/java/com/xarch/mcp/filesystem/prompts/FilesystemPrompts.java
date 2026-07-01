package com.xarch.mcp.filesystem.prompts;

import com.fasterxml.jackson.databind.JsonNode;

/** Prompt renderers for the filesystem MCP server. */
public final class FilesystemPrompts {

    private FilesystemPrompts() {}

    public static String fileSearch(JsonNode args) {
        String desc = args.path("description").asText("");
        return "You are finding files in the user's filesystem.\n\n"
                + "Description: " + desc + "\n\n"
                + "Steps:\n"
                + "1. Translate the description into one or more glob patterns\n"
                + "2. Call search_files for each pattern, prefer narrow roots\n"
                + "3. Return the matching paths in priority order\n\n"
                + "Matches:";
    }
}