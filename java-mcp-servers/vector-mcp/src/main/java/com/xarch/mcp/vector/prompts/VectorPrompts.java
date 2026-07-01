package com.xarch.mcp.vector.prompts;

import com.fasterxml.jackson.databind.JsonNode;

/** Prompt renderers for the vector MCP server. */
public final class VectorPrompts {

    private VectorPrompts() {}

    public static String semanticSearch(JsonNode args) {
        String collection = args.path("collection").asText("");
        String query = args.path("query").asText("");
        int limit = args.path("limit").asInt(10);
        return "You are performing semantic search in vector collection '" + collection + "'.\n\n"
                + "Question: " + query + "\n"
                + "Top-K: " + limit + "\n\n"
                + "Steps:\n"
                + "1. If needed, embed the query (text -> vector)\n"
                + "2. Call search(collection, vector, limit)\n"
                + "3. Use the chunk metadata to construct the answer\n\n"
                + "Answer:";
    }

    public static String similaritySearch(JsonNode args) {
        String collection = args.path("collection").asText("");
        return "You are finding vectors similar to a given embedding in collection '" + collection + "'.\n\n"
                + "Embedding: (already provided by the caller)\n\n"
                + "Steps:\n"
                + "1. Call search with the provided embedding\n"
                + "2. Sort by score\n"
                + "3. Return the top results with their metadata\n\n"
                + "Results:";
    }
}