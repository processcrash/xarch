package com.xarch.mcp.filesystem.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xarch.mcp.filesystem.FileOps;
import com.xarch.mcp.filesystem.PathGuard;
import com.xarch.mcp.runtime.stdio.StdioMcpServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tool definitions for the filesystem MCP server. Exposes 10 tools that
 * mirror the {@code node-mcp-servers/filesystem-mcp} server. All file paths
 * are funnelled through {@link PathGuard} before any I/O is performed.
 */
public class FilesystemTools {

    private static final ObjectMapper MAPPER = StdioMcpServer.mapper();

    private final PathGuard guard;
    private final FileOps ops;
    private final OpsCounter counter;

    public FilesystemTools(PathGuard guard, FileOps ops, OpsCounter counter) {
        this.guard = guard;
        this.ops = ops;
        this.counter = counter;
    }

    /** Registers all 10 tools on the supplied {@link StdioMcpServer}. */
    public StdioMcpServer register(StdioMcpServer server) {
        server.tool("list_directory", "List files and directories in a path.",
                listDirectorySchema(), this::listDirectory);
        server.tool("read_file", "Read the contents of a file (utf-8 or base64).",
                readFileSchema(), this::readFile);
        server.tool("write_file", "Write content to a file (atomic).",
                writeFileSchema(), this::writeFile);
        server.tool("delete", "Delete a file or directory.",
                deleteSchema(), this::delete);
        server.tool("create_directory", "Create a directory (optionally with parents).",
                createDirectorySchema(), this::createDirectory);
        server.tool("search_files", "Search for files matching a glob pattern.",
                searchFilesSchema(), this::searchFiles);
        server.tool("get_file_info", "Stat a file or directory.",
                getFileInfoSchema(), this::getFileInfo);
        server.tool("copy_file", "Copy a file to a new location.",
                copyFileSchema(), this::copyFile);
        server.tool("move_file", "Move or rename a file.",
                moveFileSchema(), this::moveFile);
        server.tool("health", "Health check — returns status, version and the working directory.",
                emptySchema(), this::health);
        return server;
    }

    // ===============================================================
    // Tool handlers
    // ===============================================================

    private List<StdioMcpServer.ContentBlock> listDirectory(JsonNode args) throws Exception {
        String path = required(args, "path");
        boolean recursive = optBool(args, "recursive", false);
        boolean includeHidden = optBool(args, "includeHidden", false);
        Path p = guard.validate(path);
        List<FileOps.FileEntry> entries = ops.listDirectory(p, recursive, includeHidden);
        counter.inc("reads");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("path", p.toString());
        out.put("count", entries.size());
        ArrayNode arr = out.putArray("files");
        for (FileOps.FileEntry e : entries) {
            arr.add(fileEntryNode(e));
        }
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> readFile(JsonNode args) throws Exception {
        String path = required(args, "path");
        String encoding = optText(args, "encoding", "utf-8");
        long maxBytes = optLong(args, "maxBytes", FileOps.DEFAULT_MAX_BYTES);
        Path p = guard.validate(path);
        String content = ops.readFile(p, encoding, maxBytes);
        counter.inc("reads");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("path", p.toString());
        out.put("encoding", encoding);
        out.put("bytes", (long) (encoding.equalsIgnoreCase("base64") ? 0 : content.getBytes().length));
        out.put("content", content);
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> writeFile(JsonNode args) throws Exception {
        String path = required(args, "path");
        String content = required(args, "content");
        String encoding = optText(args, "encoding", "utf-8");
        boolean createDirs = optBool(args, "createDirs", true);
        Path p = guard.validate(path);
        FileOps.WriteResult result = ops.writeFile(p, content, encoding, createDirs);
        counter.inc("writes");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", result.success());
        out.put("path", result.path());
        out.put("bytesWritten", result.bytesWritten());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> delete(JsonNode args) throws Exception {
        String path = required(args, "path");
        boolean recursive = optBool(args, "recursive", false);
        Path p = guard.validate(path);
        FileOps.DeleteResult result = ops.delete(p, recursive);
        counter.inc("deletes");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", result.success());
        out.put("path", result.path());
        out.put("wasDirectory", result.wasDirectory());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> createDirectory(JsonNode args) throws Exception {
        String path = required(args, "path");
        boolean createParents = optBool(args, "createParents", true);
        Path p = guard.validate(path);
        FileOps.CreateDirResult result = ops.createDirectory(p, createParents);
        counter.inc("writes");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", result.success());
        out.put("path", result.path());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> searchFiles(JsonNode args) throws Exception {
        String rootArg = required(args, "root");
        String pattern = required(args, "pattern");
        Integer maxDepth = args.has("maxDepth") && !args.get("maxDepth").isNull()
                ? args.get("maxDepth").asInt() : null;
        Path root = guard.validate(rootArg);
        List<Path> hits = ops.searchFiles(root, pattern, maxDepth);
        counter.inc("reads");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        out.put("root", root.toString());
        out.put("pattern", pattern);
        out.put("count", hits.size());
        ArrayNode arr = out.putArray("files");
        for (Path p : hits) {
            ObjectNode n = arr.addObject();
            n.put("path", p.toString());
        }
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> getFileInfo(JsonNode args) throws Exception {
        String path = required(args, "path");
        Path p = guard.validate(path);
        FileOps.FileInfo info = ops.getFileInfo(p);
        counter.inc("reads");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", true);
        ObjectNode f = out.putObject("file");
        f.put("name", info.name());
        f.put("path", info.path());
        f.put("size", info.size());
        f.put("isDirectory", info.isDirectory());
        f.put("isSymbolicLink", info.isSymbolicLink());
        f.put("modifiedTime", info.modifiedTime());
        f.put("createdTime", info.createdTime());
        f.put("permissions", info.permissions());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> copyFile(JsonNode args) throws Exception {
        String source = required(args, "source");
        String destination = required(args, "destination");
        boolean overwrite = optBool(args, "overwrite", false);
        Path s = guard.validate(source);
        Path d = guard.validate(destination);
        FileOps.TransferResult result = ops.copyFile(s, d, overwrite);
        counter.inc("writes");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", result.success());
        out.put("source", result.source());
        out.put("destination", result.destination());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> moveFile(JsonNode args) throws Exception {
        String source = required(args, "source");
        String destination = required(args, "destination");
        boolean overwrite = optBool(args, "overwrite", false);
        Path s = guard.validate(source);
        Path d = guard.validate(destination);
        FileOps.TransferResult result = ops.moveFile(s, d, overwrite);
        counter.inc("writes");
        ObjectNode out = MAPPER.createObjectNode();
        out.put("success", result.success());
        out.put("source", result.source());
        out.put("destination", result.destination());
        return json(out);
    }

    private List<StdioMcpServer.ContentBlock> health(JsonNode args) {
        ObjectNode out = MAPPER.createObjectNode();
        out.put("status", "UP");
        out.put("server", "xarch-filesystem-mcp");
        out.put("version", "1.0.0");
        out.put("workingDirectory", guard.defaultWorkingDirectory());
        out.set("opsCounter", counter.snapshotJson());
        return json(out);
    }

    // ===============================================================
    // Schemas
    // ===============================================================

    private static ObjectNode emptySchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        return schema;
    }

    private static ObjectNode listDirectorySchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("path", "Directory path to list."),
                boolProp("recursive", "List subdirectories recursively."),
                boolProp("includeHidden", "Include dotfiles. Default false."));
        schema.putArray("required").add("path");
        return schema;
    }

    private static ObjectNode readFileSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("path", "File path to read."),
                stringProp("encoding", "Either 'utf-8' (default) or 'base64'."),
                longProp("maxBytes", "Hard cap on file size in bytes. Default 1MB."));
        schema.putArray("required").add("path");
        return schema;
    }

    private static ObjectNode writeFileSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("path", "File path to write."),
                stringProp("content", "Content to write. Text in UTF-8 or base64-encoded bytes."),
                stringProp("encoding", "Either 'utf-8' (default) or 'base64'."),
                boolProp("createDirs", "Create parent directories as needed. Default true."));
        schema.putArray("required").add("path", "content");
        return schema;
    }

    private static ObjectNode deleteSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("path", "Path to delete."),
                boolProp("recursive", "Recursively delete a directory. Default false."));
        schema.putArray("required").add("path");
        return schema;
    }

    private static ObjectNode createDirectorySchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("path", "Directory path to create."),
                boolProp("createParents", "Create parent directories as needed. Default true."));
        schema.putArray("required").add("path");
        return schema;
    }

    private static ObjectNode searchFilesSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("root", "Directory to search in."),
                stringProp("pattern", "Glob pattern e.g. *.java or *.txt."),
                longProp("maxDepth", "Maximum directory depth (0 = top-level only)."));
        schema.putArray("required").add("root", "pattern");
        return schema;
    }

    private static ObjectNode getFileInfoSchema() {
        ObjectNode schema = baseObjectSchema(stringProp("path", "File or directory path."));
        schema.putArray("required").add("path");
        return schema;
    }

    private static ObjectNode copyFileSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("source", "Source file path."),
                stringProp("destination", "Destination file path."),
                boolProp("overwrite", "Overwrite destination if it exists. Default false."));
        schema.putArray("required").add("source", "destination");
        return schema;
    }

    private static ObjectNode moveFileSchema() {
        ObjectNode schema = baseObjectSchema(
                stringProp("source", "Source file path."),
                stringProp("destination", "Destination file path."),
                boolProp("overwrite", "Overwrite destination if it exists. Default false."));
        schema.putArray("required").add("source", "destination");
        return schema;
    }

    // ===============================================================
    // Schema helpers
    // ===============================================================

    private static ObjectNode baseObjectSchema(ObjectNode... properties) {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        ObjectNode props = schema.putObject("properties");
        for (ObjectNode p : properties) props.setAll(p);
        return schema;
    }

    private static ObjectNode stringProp(String name, String desc) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "string");
        n.put("description", desc);
        ObjectNode out = MAPPER.createObjectNode();
        out.set(name, n);
        return out;
    }

    private static ObjectNode boolProp(String name, String desc) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "boolean");
        n.put("description", desc);
        ObjectNode out = MAPPER.createObjectNode();
        out.set(name, n);
        return out;
    }

    private static ObjectNode longProp(String name, String desc) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "integer");
        n.put("description", desc);
        ObjectNode out = MAPPER.createObjectNode();
        out.set(name, n);
        return out;
    }

    // ===============================================================
    // Tiny helpers
    // ===============================================================

    private static String required(JsonNode args, String name) {
        if (args == null || !args.has(name) || args.get(name).isNull()) {
            throw new IllegalArgumentException("Missing required argument: " + name);
        }
        return args.get(name).asText();
    }

    private static boolean optBool(JsonNode args, String name, boolean def) {
        if (args == null || !args.has(name) || args.get(name).isNull()) return def;
        return args.get(name).asBoolean(def);
    }

    private static String optText(JsonNode args, String name, String def) {
        if (args == null || !args.has(name) || args.get(name).isNull()) return def;
        return args.get(name).asText(def);
    }

    private static long optLong(JsonNode args, String name, long def) {
        if (args == null || !args.has(name) || args.get(name).isNull()) return def;
        return args.get(name).asLong(def);
    }

    private List<StdioMcpServer.ContentBlock> json(ObjectNode node) {
        return List.of(StdioMcpServer.ContentBlock.text(node.toString()));
    }

    private static ObjectNode fileEntryNode(FileOps.FileEntry e) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("name", e.name());
        n.put("path", e.path());
        n.put("size", e.size());
        n.put("isDirectory", e.isDirectory());
        n.put("modifiedTime", e.modifiedTime());
        return n;
    }

    // ===============================================================
    // Ops counter (used by resources/health)
    // ===============================================================

    /** Simple thread-safe counter, exposed as {@code fs://stats}. */
    public static final class OpsCounter {
        private final Map<String, AtomicLong> counters = new LinkedHashMap<>();
        public OpsCounter() {
            counters.put("reads", new AtomicLong());
            counters.put("writes", new AtomicLong());
            counters.put("deletes", new AtomicLong());
        }
        public void inc(String kind) {
            AtomicLong c = counters.get(kind);
            if (c != null) c.incrementAndGet();
        }
        public long get(String kind) {
            AtomicLong c = counters.get(kind);
            return c == null ? 0 : c.get();
        }
        public Map<String, Long> snapshot() {
            Map<String, Long> out = new LinkedHashMap<>();
            counters.forEach((k, v) -> out.put(k, v.get()));
            return out;
        }
        public ObjectNode snapshotJson() {
            ObjectNode n = MAPPER.createObjectNode();
            counters.forEach((k, v) -> n.put(k, v.get()));
            return n;
        }
    }

    // Public-test helper — exposes the registered tool handler set for tests.
    public Map<String, Function<JsonNode, List<StdioMcpServer.ContentBlock>>> handlersForTests() {
        Map<String, Function<JsonNode, List<StdioMcpServer.ContentBlock>>> h = new LinkedHashMap<>();
        h.put("list_directory", this::listDirectory);
        h.put("read_file", this::readFile);
        h.put("write_file", this::writeFile);
        h.put("delete", this::delete);
        h.put("create_directory", this::createDirectory);
        h.put("search_files", this::searchFiles);
        h.put("get_file_info", this::getFileInfo);
        h.put("copy_file", this::copyFile);
        h.put("move_file", this::moveFile);
        h.put("health", this::health);
        return h;
    }

    // Exposed for testing.
    public List<String> schemaTags() {
        return new ArrayList<>(List.of(
                "list_directory", "read_file", "write_file", "delete",
                "create_directory", "search_files", "get_file_info",
                "copy_file", "move_file", "health"));
    }

    /** Count how many tool names this provider registers. Used by tests. */
    public int toolCount() {
        return handlersForTests().size();
    }

    /** For documentation/test convenience. */
    public List<String> toolNames() {
        return new ArrayList<>(handlersForTests().keySet());
    }

    /** For documentation/test convenience. */
    public String toolSummary() {
        return toolNames().stream().collect(Collectors.joining(", "));
    }
}
