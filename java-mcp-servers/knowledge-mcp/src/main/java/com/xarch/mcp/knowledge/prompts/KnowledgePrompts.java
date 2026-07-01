package com.xarch.mcp.knowledge.prompts;

import com.fasterxml.jackson.databind.JsonNode;

/** Prompt renderers for the knowledge MCP server. */
public final class KnowledgePrompts {

    private KnowledgePrompts() {}

    public static String ragSearch(JsonNode args) {
        String query = args.path("query").asText("");
        int topK = args.path("topK").asInt(5);
        return "You are answering a user question using the xarch knowledge base.\n\n"
                + "Question: " + query + "\n\n"
                + "Steps:\n"
                + "1. Call kb_search(query, " + topK + ") to retrieve relevant chunks\n"
                + "2. Cite the documentId of each chunk you use\n"
                + "3. If no chunk is relevant, say so explicitly\n\n"
                + "Answer:";
    }
}