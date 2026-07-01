package com.xarch.mcp.vector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xarch.mcp.runtime.stdio.StdioMcpServer;
import com.xarch.mcp.vector.store.VectorStore;

import java.util.List;

/**
 * Entry point for the vector MCP stdio server. Mirrors the tool set
 * exposed by the Node.js sibling at
 * {@code node-mcp-servers/vector-mcp/src/index.ts}.
 *
 * <p>Uses an in-memory {@link VectorStore} by default; the store math is
 * reused from the REST sibling at
 * {@code backend/xarch-spring-boot-starter/xarch-mcp/xarch-mcp-vector}.
 */
public class VectorMcpServerMain {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        VectorStore store = new VectorStore();
        VectorConfig config = new VectorConfig();

        new StdioMcpServer("xarch-vector-mcp", "1.0.0")
            .tool("configure", "Configure vector store connection", configureSchema(),
                a -> tools.VectorTools.configure(config, a))
            .tool("create_collection", "Create a new vector collection", createCollectionSchema(),
                a -> tools.VectorTools.createCollection(store, a))
            .tool("upsert", "Insert or update vectors", upsertSchema(),
                a -> tools.VectorTools.upsert(store, a))
            .tool("search", "KNN search", searchSchema(),
                a -> tools.VectorTools.search(store, a))
            .tool("text_search", "Text-based search (placeholder for LLM embedding)", textSearchSchema(),
                a -> tools.VectorTools.textSearch(store, a))
            .tool("delete", "Delete a vector by ID", deleteSchema(),
                a -> tools.VectorTools.delete(store, a))
            .tool("list_collections", "List all collections", emptySchema(),
                a -> tools.VectorTools.listCollections(store))
            .tool("get_stats", "Get collection stats", getStatsSchema(),
                a -> tools.VectorTools.getStats(store, a))
            .tool("health", "Health check", emptySchema(),
                a -> tools.VectorTools.health(config, store))
            .resource("vector://config", "Vector Store Configuration", "Current vector store config", "application/json",
                a -> resources.VectorResources.config(config))
            .resource("vector://collections", "Vector Collections", "List of all vector collections", "application/json",
                a -> resources.VectorResources.collections(store))
            .prompt("semantic-search", "Perform semantic search across vector collections", List.of(
                new StdioMcpServer.PromptArgument("collection", "Target collection", true),
                new StdioMcpServer.PromptArgument("query", "Search query", true),
                new StdioMcpServer.PromptArgument("limit", "Maximum number of results", false)
            ), a -> prompts.VectorPrompts.semanticSearch(a))
            .prompt("similarity-search", "Find similar vectors using embedding", List.of(
                new StdioMcpServer.PromptArgument("collection", "Target collection", true),
                new StdioMcpServer.PromptArgument("embedding", "Vector embedding array", true)
            ), a -> prompts.VectorPrompts.similaritySearch(a))
            .run();
    }

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
        p.putObject("type").put("type", "string").put("default", "in-memory");
        p.putObject("defaultDimension").put("type", "integer").put("default", 1536);
        return s;
    }

    private static ObjectNode createCollectionSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("name").put("type", "string");
        p.putObject("dimension").put("type", "integer");
        p.putObject("distance").put("type", "string").put("default", "cosine");
        s.putArray("required").add("name").add("dimension");
        return s;
    }

    private static ObjectNode upsertSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        ObjectNode vec = p.putObject("vectors");
        vec.put("type", "array");
        ObjectNode items = vec.putObject("items");
        items.put("type", "object");
        ObjectNode ip = items.putObject("properties");
        ip.putObject("id").put("type", "string");
        ip.putObject("vector").put("type", "array").putObject("items").put("type", "number");
        ip.putObject("content").put("type", "string");
        ip.putObject("metadata").put("type", "object");
        s.putArray("required").add("collection").add("vectors");
        return s;
    }

    private static ObjectNode searchSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("collection").put("type", "string");
        p.putObject("vector").put("type", "array").putObject("items").put("type", "number");
        p.putObject("limit").put("type", "integer").put("default", 10);
        p.putObject("filter").put("type", "object");
        s.putArray("required").add("collection").add("vector");
        return s;
    }

    private static ObjectNode textSearchSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("collection").put("type", "string");
        p.putObject("query").put("type", "string");
        p.putObject("limit").put("type", "integer").put("default", 10);
        s.putArray("required").add("collection").add("query");
        return s;
    }

    private static ObjectNode deleteSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("collection").put("type", "string");
        p.putObject("id").put("type", "string");
        s.putArray("required").add("collection").add("id");
        return s;
    }

    private static ObjectNode getStatsSchema() {
        ObjectNode s = MAPPER.createObjectNode();
        s.put("type", "object");
        ObjectNode p = s.putObject("properties");
        p.putObject("collection").put("type", "string");
        s.putArray("required").add("collection");
        return s;
    }
}