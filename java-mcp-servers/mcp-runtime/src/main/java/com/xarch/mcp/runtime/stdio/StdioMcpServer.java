package com.xarch.mcp.runtime.stdio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Minimal MCP 2024-11-05 stdio server. Reads JSON-RPC 2.0 messages from
 * stdin (one per line) and writes responses to stdout.
 *
 * <p>Mirrors what the Node.js SDK does internally — a deliberately small
 * surface so each xarch MCP server can declare its own tools and run
 * in-process. Stderr is reserved for human-readable logs; NEVER write to
 * stdout outside of this class or MCP clients will fail to parse messages.
 *
 * <h2>Supported methods</h2>
 * <ul>
 *   <li>{@code initialize} — returns server info and capabilities</li>
 *   <li>{@code notifications/initialized} — no response (notification)</li>
 *   <code>tools/list</code> — returns the list of registered tools
 *   <li>{@code tools/call} — invokes a tool, returns the content array</li>
 *   <li>{@code resources/list} — returns the list of registered resources</li>
 *   <li>{@code resources/read} — reads a resource by URI</li>
 *   <li>{@code prompts/list} — returns the list of registered prompts</li>
 *   <li>{@code prompts/get} — renders a prompt by name</li>
 *   <li>{@code ping} — no-op</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * public static void main(String[] args) {
 *     new StdioMcpServer("xarch-database-mcp", "1.0.0")
 *         .tool("query", "Execute a SQL query", schema, (args) -> {...})
 *         .resource("config://current", "Current connection config", ...)
 *         .run();
 * }
 * }</pre>
 */
public class StdioMcpServer {

    private static final Logger log = LoggerFactory.getLogger(StdioMcpServer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PROTOCOL_VERSION = "2024-11-05";

    private final String name;
    private final String version;
    private final List<Tool> tools = new ArrayList<>();
    private final List<Resource> resources = new ArrayList<>();
    private final List<Prompt> prompts = new ArrayList<>();
    private final AtomicLong requestId = new AtomicLong(0);
    private final boolean announceResources;
    private final boolean announcePrompts;

    public StdioMcpServer(String name, String version) {
        this(name, version, true, true);
    }

    public StdioMcpServer(String name, String version, boolean announceResources, boolean announcePrompts) {
        this.name = name;
        this.version = version;
        this.announceResources = announceResources;
        this.announcePrompts = announcePrompts;
    }

    public StdioMcpServer tool(String name, String description, ObjectNode inputSchema, Function<JsonNode, List<ContentBlock>> handler) {
        tools.add(new Tool(name, description, inputSchema, handler));
        return this;
    }

    public StdioMcpServer resource(String uri, String name, String description, String mimeType, Function<JsonNode, String> handler) {
        resources.add(new Resource(uri, name, description, mimeType, handler));
        return this;
    }

    public StdioMcpServer prompt(String name, String description, List<PromptArgument> args, Function<JsonNode, String> handler) {
        prompts.add(new Prompt(name, description, args, handler));
        return this;
    }

    /**
     * Start reading JSON-RPC requests from stdin. Blocks until EOF.
     */
    public void run() {
        run(System.in, System.out);
    }

    /**
     * Test hook — lets unit tests drive the protocol with a fake stdin/stdout.
     */
    public void run(InputStream in, OutputStream out) {
        PrintStream stdout = new PrintStream(out, true, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        log.info("MCP stdio server '{}' v{} starting", name, version);

        String line;
        while ((line = readLine(reader)) != null) {
            if (line.isBlank()) continue;
            try {
                ObjectNode request = (ObjectNode) MAPPER.readTree(line);
                if (!request.has("method")) continue;
                String method = request.path("method").asText();
                ObjectNode id = request.has("id") && !request.path("id").isNull()
                        ? (ObjectNode) request.get("id") : null;
                JsonNode params = request.get("params");
                boolean isNotification = id == null;

                ObjectNode response = dispatch(method, params, isNotification);
                if (isNotification) {
                    log.debug("notification {} handled (no response sent)", method);
                    continue;
                }
                response.set("jsonrpc", MAPPER.getNodeFactory().textNode("2.0"));
                response.set("id", id);
                stdout.println(MAPPER.writeValueAsString(response));
            } catch (Exception e) {
                log.error("error handling request: {}", e.getMessage(), e);
                ObjectNode err = MAPPER.createObjectNode();
                err.put("code", -32603);
                err.put("message", "Internal error: " + e.getMessage());
                stdout.println(MAPPER.writeValueAsString(errorResponse(null, err)));
            }
        }
        log.info("MCP stdio server '{}' shutting down (stdin closed)", name);
    }

    private ObjectNode dispatch(String method, JsonNode params, boolean isNotification) {
        ObjectNode response = MAPPER.createObjectNode();
        try {
            switch (method) {
                case "initialize" -> response.set("result", handleInitialize());
                case "notifications/initialized" -> {
                    log.info("client initialized");
                    return MAPPER.createObjectNode();
                }
                case "ping" -> response.set("result", MAPPER.createObjectNode());
                case "tools/list" -> response.set("result", handleToolsList());
                case "tools/call" -> response.set("result", handleToolsCall(params));
                case "resources/list" -> response.set("result", handleResourcesList());
                case "resources/read" -> response.set("result", handleResourcesRead(params));
                case "prompts/list" -> response.set("result", handlePromptsList());
                case "prompts/get" -> response.set("result", handlePromptsGet(params));
                default -> {
                    if (!isNotification) {
                        ObjectNode err = MAPPER.createObjectNode();
                        err.put("code", -32601);
                        err.put("message", "Method not found: " + method);
                        return errorResponse(null, err);
                    }
                }
            }
        } catch (Exception e) {
            log.error("dispatch error for {}: {}", method, e.getMessage(), e);
            ObjectNode err = MAPPER.createObjectNode();
            err.put("code", -32603);
            err.put("message", e.getMessage());
            response.set("error", err);
        }
        return response;
    }

    private ObjectNode handleInitialize() {
        ObjectNode result = MAPPER.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);
        ObjectNode caps = MAPPER.createObjectNode();
        caps.put("tools", MAPPER.createObjectNode());
        if (announceResources && !resources.isEmpty()) caps.put("resources", MAPPER.createObjectNode());
        if (announcePrompts && !prompts.isEmpty()) caps.put("prompts", MAPPER.createObjectNode());
        result.set("capabilities", caps);
        ObjectNode info = MAPPER.createObjectNode();
        info.put("name", name);
        info.put("version", version);
        result.set("serverInfo", info);
        return result;
    }

    private ObjectNode handleToolsList() {
        ArrayNode arr = MAPPER.createArrayNode();
        for (Tool t : tools) {
            ObjectNode tn = MAPPER.createObjectNode();
            tn.put("name", t.name);
            tn.put("description", t.description);
            tn.set("inputSchema", t.inputSchema);
            arr.add(tn);
        }
        ObjectNode result = MAPPER.createObjectNode();
        result.set("tools", arr);
        return result;
    }

    private ObjectNode handleToolsCall(JsonNode params) {
        if (params == null || !params.has("name")) {
            throw new IllegalArgumentException("tools/call requires params.name");
        }
        String toolName = params.path("name").asText();
        JsonNode args = params.path("arguments");
        Tool tool = tools.stream().filter(t -> t.name.equals(toolName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));
        List<ContentBlock> content = tool.handler.apply(args);
        ArrayNode arr = MAPPER.createArrayNode();
        for (ContentBlock c : content) {
            ObjectNode cn = MAPPER.createObjectNode();
            cn.put("type", c.type);
            cn.put(c.type, c.value);
            arr.add(cn);
        }
        ObjectNode result = MAPPER.createObjectNode();
        result.set("content", arr);
        return result;
    }

    private ObjectNode handleResourcesList() {
        ArrayNode arr = MAPPER.createArrayNode();
        for (Resource r : resources) {
            ObjectNode rn = MAPPER.createObjectNode();
            rn.put("uri", r.uri);
            rn.put("name", r.name);
            rn.put("description", r.description);
            rn.put("mimeType", r.mimeType);
            arr.add(rn);
        }
        ObjectNode result = MAPPER.createObjectNode();
        result.set("resources", arr);
        return result;
    }

    private ObjectNode handleResourcesRead(JsonNode params) {
        if (params == null || !params.has("uri")) {
            throw new IllegalArgumentException("resources/read requires params.uri");
        }
        String uri = params.path("uri").asText();
        Resource r = resources.stream().filter(x -> x.uri.equals(uri)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown resource: " + uri));
        String text = r.handler.apply(MAPPER.createObjectNode());
        ObjectNode contents = MAPPER.createObjectNode();
        contents.put("uri", uri);
        contents.put("mimeType", r.mimeType);
        contents.put("text", text);
        ArrayNode arr = MAPPER.createArrayNode();
        arr.add(contents);
        ObjectNode result = MAPPER.createObjectNode();
        result.set("contents", arr);
        return result;
    }

    private ObjectNode handlePromptsList() {
        ArrayNode arr = MAPPER.createArrayNode();
        for (Prompt p : prompts) {
            ObjectNode pn = MAPPER.createObjectNode();
            pn.put("name", p.name);
            pn.put("description", p.description);
            ArrayNode args = MAPPER.createArrayNode();
            for (PromptArgument a : p.args) {
                ObjectNode an = MAPPER.createObjectNode();
                an.put("name", a.name);
                an.put("description", a.description);
                an.put("required", a.required);
                args.add(an);
            }
            pn.set("arguments", args);
            arr.add(pn);
        }
        ObjectNode result = MAPPER.createObjectNode();
        result.set("prompts", arr);
        return result;
    }

    private ObjectNode handlePromptsGet(JsonNode params) {
        if (params == null || !params.has("name")) {
            throw new IllegalArgumentException("prompts/get requires params.name");
        }
        String name = params.path("name").asText();
        Prompt p = prompts.stream().filter(x -> x.name.equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown prompt: " + name));
        String text = p.handler.apply(params.path("arguments"));
        ObjectNode message = MAPPER.createObjectNode();
        message.put("role", "user");
        ObjectNode content = MAPPER.createObjectNode();
        content.put("type", "text");
        content.put("text", text);
        ArrayNode contentArr = MAPPER.createArrayNode();
        contentArr.add(content);
        message.set("content", contentArr);
        ArrayNode messages = MAPPER.createArrayNode();
        messages.add(message);
        ObjectNode result = MAPPER.createObjectNode();
        result.set("messages", messages);
        return result;
    }

    private ObjectNode errorResponse(JsonNode id, ObjectNode err) {
        ObjectNode r = MAPPER.createObjectNode();
        r.put("jsonrpc", "2.0");
        if (id != null) r.set("id", id);
        r.set("error", err);
        return r;
    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            log.error("stdin read failed: {}", e.getMessage());
            return null;
        }
    }

    // ---- records -------------------------------------------------------

    public record ContentBlock(String type, String value) {
        public static ContentBlock text(String value) { return new ContentBlock("text", value); }
        public static ContentBlock json(String value) { return new ContentBlock("text", value); }
    }

    public record Resource(String uri, String name, String description, String mimeType,
                           Function<JsonNode, String> handler) {}

    public record PromptArgument(String name, String description, boolean required) {}

    public record Prompt(String name, String description, List<PromptArgument> args,
                         Function<JsonNode, String> handler) {}

    public record Tool(String name, String description, ObjectNode inputSchema,
                       Function<JsonNode, List<ContentBlock>> handler) {}

    public static ObjectMapper mapper() { return MAPPER; }
}
