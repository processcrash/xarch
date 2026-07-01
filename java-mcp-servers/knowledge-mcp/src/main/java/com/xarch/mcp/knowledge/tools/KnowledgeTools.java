package com.xarch.mcp.knowledge.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xarch.mcp.knowledge.KnowledgeBase;
import com.xarch.mcp.knowledge.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handlers for the 9 MCP tools exposed by this server. Each handler
 * receives a JSON-RPC {@code arguments} object and returns a list of
 * content blocks (always a single text block containing JSON).
 *
 * <p>The tools mirror the Node.js implementation at
 * {@code node-mcp-servers/knowledge-mcp/src/index.ts}.
 */
public final class KnowledgeTools {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeTools.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KnowledgeBase kb;

    public KnowledgeTools(KnowledgeBase kb) {
        this.kb = kb;
    }

    // ------------------------------------------------------------------
    // Schemas
    // ------------------------------------------------------------------

    /** Schema for {@code kb_index_document}. */
    public ObjectNode schemaIndexDocument() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("id", textNode("Optional document ID (auto-generated if not provided)"));
        props.set("title", textNode("Document title"));
        props.set("content", textNode("Document content to index"));
        props.set("type", textNode("Document type (e.g., \"article\", \"policy\", \"faq\")"));
        props.set("chunkSize", intNode("Chunk size in characters (default 500)", 500));
        props.set("overlap", intNode("Overlap between chunks in characters (default 50)", 50));
        ArrayNode req = schema.putArray("required");
        req.add("title");
        req.add("content");
        return schema;
    }

    /** Schema for {@code kb_index_file}. */
    public ObjectNode schemaIndexFile() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("filePath", textNode("Path to file on disk"));
        props.set("chunkSize", intNode("Chunk size in characters (default 500)", 500));
        props.set("overlap", intNode("Overlap between chunks in characters (default 50)", 50));
        ArrayNode req = schema.putArray("required");
        req.add("filePath");
        return schema;
    }

    /** Schema for {@code kb_search}. */
    public ObjectNode schemaSearch() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("query", textNode("Search query"));
        ObjectNode topK = props.putObject("topK");
        topK.put("type", "integer");
        topK.put("default", 5);
        topK.put("description", "Max results");
        ArrayNode req = schema.putArray("required");
        req.add("query");
        return schema;
    }

    /** Schema for {@code kb_get_document}. */
    public ObjectNode schemaGetDocument() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("documentId", textNode("Document ID"));
        ArrayNode req = schema.putArray("required");
        req.add("documentId");
        return schema;
    }

    /** Schema for {@code kb_delete}. */
    public ObjectNode schemaDelete() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("documentId", textNode("Document ID to delete"));
        ArrayNode req = schema.putArray("required");
        req.add("documentId");
        return schema;
    }

    /** Schema for {@code kb_list} — no parameters. */
    public ObjectNode schemaList() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        return schema;
    }

    /** Schema for {@code kb_update}. */
    public ObjectNode schemaUpdate() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        props.set("documentId", textNode("Document ID to update"));
        props.set("title", textNode("New title"));
        props.set("content", textNode("New content"));
        ArrayNode req = schema.putArray("required");
        req.add("documentId");
        return schema;
    }

    /** Schema for {@code kb_stats} — no parameters. */
    public ObjectNode schemaStats() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        return schema;
    }

    /** Schema for {@code health} — no parameters. */
    public ObjectNode schemaHealth() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        return schema;
    }

    // ------------------------------------------------------------------
    // Handlers
    // ------------------------------------------------------------------

    /** Handler for {@code kb_index_document}. */
    public List<String> handleIndexDocument(JsonNode args) {
        String id = optString(args, "id");
        String title = requiredString(args, "title");
        String content = requiredString(args, "content");
        String type = optString(args, "type");
        int chunkSize = optInt(args, "chunkSize", KnowledgeBase.DEFAULT_CHUNK_SIZE);
        int overlap = optInt(args, "overlap", KnowledgeBase.DEFAULT_OVERLAP);

        String resolvedId = kb.indexDocument(id, title, content, type, chunkSize, overlap);
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("id", resolvedId);
        return List.of(out.toString());
    }

    /** Handler for {@code kb_index_file}. */
    public List<String> handleIndexFile(JsonNode args) {
        String filePath = requiredString(args, "filePath");
        int chunkSize = optInt(args, "chunkSize", KnowledgeBase.DEFAULT_CHUNK_SIZE);
        int overlap = optInt(args, "overlap", KnowledgeBase.DEFAULT_OVERLAP);

        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }
        String content;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
        String title = path.getFileName().toString();
        String type = "file." + extensionOf(filePath);
        String resolvedId = kb.indexDocument(null, title, content, type, chunkSize, overlap);

        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("id", resolvedId);
        out.put("filePath", filePath);
        return List.of(out.toString());
    }

    /** Handler for {@code kb_search}. */
    public List<String> handleSearch(JsonNode args) {
        String query = requiredString(args, "query");
        int topK = optInt(args, "topK", 5);
        List<KnowledgeBase.ScoredChunk> results = kb.search(query, topK);

        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("count", results.size());
        ArrayNode arr = out.putArray("results");
        for (KnowledgeBase.ScoredChunk sc : results) {
            ObjectNode rn = MAPPER.createObjectNode();
            rn.put("chunkId", sc.chunkId());
            rn.put("documentId", sc.documentId());
            rn.put("ordinal", sc.ordinal());
            rn.put("score", sc.score());
            rn.put("content", sc.content());
            arr.add(rn);
        }
        return List.of(out.toString());
    }

    /** Handler for {@code kb_get_document}. */
    public List<String> handleGetDocument(JsonNode args) {
        String documentId = requiredString(args, "documentId");
        Document doc = kb.getDocument(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        ObjectNode docNode = out.putObject("document");
        docNode.put("id", doc.id());
        docNode.put("title", doc.title());
        docNode.put("content", doc.content());
        docNode.put("type", doc.type());
        docNode.put("createdAt", doc.createdAt().toString());
        docNode.put("chunkCount", kb.chunksOf(doc.id()).size());
        return List.of(out.toString());
    }

    /** Handler for {@code kb_delete}. */
    public List<String> handleDelete(JsonNode args) {
        String documentId = requiredString(args, "documentId");
        boolean removed = kb.deleteDocument(documentId);
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", removed);
        if (!removed) {
            out.put("error", "Document not found: " + documentId);
        }
        return List.of(out.toString());
    }

    /** Handler for {@code kb_list}. */
    public List<String> handleList(JsonNode args) {
        List<KnowledgeBase.DocumentSummary> docs = kb.listDocuments();
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("documentCount", docs.size());
        out.put("chunkCount", kb.stats().chunkCount());
        ArrayNode arr = out.putArray("documents");
        for (KnowledgeBase.DocumentSummary ds : docs) {
            ObjectNode n = MAPPER.createObjectNode();
            n.put("id", ds.id());
            n.put("title", ds.title());
            n.put("type", ds.type());
            n.put("chunkCount", ds.chunkCount());
            n.put("createdAt", ds.createdAt().toString());
            arr.add(n);
        }
        return List.of(out.toString());
    }

    /** Handler for {@code kb_update}. */
    public List<String> handleUpdate(JsonNode args) {
        String documentId = requiredString(args, "documentId");
        String newTitle = optString(args, "title");
        String newContent = optString(args, "content");
        boolean updated = kb.updateDocument(documentId, newTitle, newContent);
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", updated);
        if (!updated) {
            out.put("error", "Document not found: " + documentId);
        }
        return List.of(out.toString());
    }

    /** Handler for {@code kb_stats}. */
    public List<String> handleStats(JsonNode args) {
        KnowledgeBase.Stats stats = kb.stats();
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        ObjectNode s = out.putObject("stats");
        s.put("documentCount", stats.documentCount());
        s.put("chunkCount", stats.chunkCount());
        ObjectNode byType = s.putObject("byType");
        stats.byType().forEach(byType::put);
        return List.of(out.toString());
    }

    /** Handler for {@code health}. */
    public List<String> handleHealth(JsonNode args) {
        ObjectNode out = MAPPER.createObjectNode();
        out.put("status", "UP");
        out.put("documents", kb.stats().documentCount());
        return List.of(out.toString());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static ObjectNode textNode(String description) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "string");
        n.put("description", description);
        return n;
    }

    private static ObjectNode intNode(String description, int defaultValue) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "integer");
        n.put("default", defaultValue);
        n.put("description", description);
        return n;
    }

    private static String optString(JsonNode args, String field) {
        if (args == null || !args.has(field) || args.get(field).isNull()) return null;
        JsonNode v = args.get(field);
        return v.isTextual() ? v.asText() : v.asText();
    }

    private static String requiredString(JsonNode args, String field) {
        String v = optString(args, field);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return v;
    }

    private static int optInt(JsonNode args, String field, int defaultValue) {
        if (args == null || !args.has(field) || args.get(field).isNull()) return defaultValue;
        JsonNode v = args.get(field);
        if (v.isInt() || v.isLong() || v.canConvertToInt()) return v.asInt();
        return defaultValue;
    }

    private static String extensionOf(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return "txt";
        return path.substring(dot + 1).toLowerCase();
    }
}