package com.xarch.mcp.filesystem.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.filesystem.FileOps;
import com.xarch.mcp.filesystem.PathGuard;

import java.util.LinkedHashMap;
import java.util.Map;

/** Resource providers for the filesystem MCP server. */
public final class FilesystemResources {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FilesystemResources() {}

    public static String config(PathGuard guard) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("allowedRoots", guard.allowedRoots().stream().map(Object::toString).toList());
        result.put("primaryRoot", guard.primaryRoot().toString());
        try { return MAPPER.writeValueAsString(result); }
        catch (Exception e) { return "{}"; }
    }

    public static String stats(FileOps ops) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reads", ops.readCount());
        result.put("writes", ops.writeCount());
        result.put("deletes", ops.deleteCount());
        result.put("copies", ops.copyCount());
        result.put("moves", ops.moveCount());
        try { return MAPPER.writeValueAsString(result); }
        catch (Exception e) { return "{}"; }
    }
}