package com.xarch.mcp.filesystem;

import com.xarch.mcp.runtime.stdio.StdioMcpServer;
import tools.FilesystemTools;

import java.util.Arrays;
import java.util.List;

/**
 * Entry point for the filesystem MCP stdio server. Mirrors the tool set
 * exposed by the Node.js sibling at
 * {@code node-mcp-servers/filesystem-mcp/src/index.ts}.
 *
 * <p>The default allowed root is {@code $XARCH_FS_ALLOWED_ROOTS} (colon
 * separated) or, if absent, the JVM working directory. All file
 * operations are validated against the {@link PathGuard} to prevent
 * directory-traversal attacks.
 */
public class FilesystemMcpServerMain {

    public static void main(String[] args) {
        PathGuard guard = initGuard();
        FileOps ops = new FileOps();
        FilesystemTools.OpsCounter counter = new FilesystemTools.OpsCounter();
        FilesystemTools tools = new FilesystemTools(guard, ops, counter);

        StdioMcpServer server = new StdioMcpServer("xarch-filesystem-mcp", "1.0.0");
        tools.register(server);

        server
            .resource("fs://config", "Filesystem Configuration", "Allowed roots and counters", "application/json",
                a -> resources.FilesystemResources.config(guard))
            .resource("fs://stats", "Filesystem Stats", "Running operation counters", "application/json",
                a -> resources.FilesystemResources.stats(counter))
            .prompt("file-search", "Find files matching a description", List.of(
                new StdioMcpServer.PromptArgument("description", "Natural-language description of the files to find", true)
            ), a -> prompts.FilesystemPrompts.fileSearch(a))
            .run();
    }

    private static PathGuard initGuard() {
        String env = System.getenv("XARCH_FS_ALLOWED_ROOTS");
        if (env != null && !env.isBlank()) {
            return new PathGuard(Arrays.asList(env.split(":")));
        }
        return new PathGuard(List.of(System.getProperty("user.dir")));
    }
}