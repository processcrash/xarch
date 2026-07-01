package com.xarch.mcp.knowledge;

import com.xarch.mcp.runtime.stdio.StdioMcpServer;
import tools.KnowledgeTools;

import java.util.List;

/**
 * Entry point for the knowledge MCP stdio server. Mirrors the tool set
 * exposed by the Node.js sibling at
 * {@code node-mcp-servers/knowledge-mcp/src/index.ts}.
 */
public class KnowledgeMcpServerMain {

    public static void main(String[] args) {
        KnowledgeBase kb = new KnowledgeBase();
        KnowledgeTools tools = new KnowledgeTools(kb);

        StdioMcpServer server = new StdioMcpServer("xarch-knowledge-mcp", "1.0.0");

        server
            .tool("kb_index_document", "Index a document", tools.schemaIndexDocument(), tools::handleIndexDocument)
            .tool("kb_index_file", "Index a file from disk", tools.schemaIndexFile(), tools::handleIndexFile)
            .tool("kb_search", "Search the knowledge base", tools.schemaSearch(), tools::handleSearch)
            .tool("kb_get_document", "Get a document by ID", tools.schemaGetDocument(), tools::handleGetDocument)
            .tool("kb_delete", "Delete a document by ID", tools.schemaDelete(), tools::handleDelete)
            .tool("kb_list", "List indexed documents", tools.schemaList(), tools::handleList)
            .tool("kb_update", "Update an existing document", tools.schemaUpdate(), tools::handleUpdate)
            .tool("kb_stats", "Knowledge-base statistics", tools.schemaStats(), tools::handleStats)
            .tool("health", "Health check", tools.schemaHealth(), tools::handleHealth)
            .resource("kb://stats", "Knowledge Base Statistics", "Current KB document/chunk counts", "application/json",
                a -> resources.KnowledgeResources.stats(kb))
            .prompt("rag-search", "RAG retrieval-augmented search", List.of(
                new StdioMcpServer.PromptArgument("query", "Search query", true),
                new StdioMcpServer.PromptArgument("topK", "Max results to retrieve", false)
            ), a -> prompts.KnowledgePrompts.ragSearch(a))
            .run();
    }
}