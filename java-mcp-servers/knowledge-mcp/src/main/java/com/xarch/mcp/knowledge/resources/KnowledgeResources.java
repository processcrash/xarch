package com.xarch.mcp.knowledge.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.knowledge.KnowledgeBase;

import java.util.LinkedHashMap;
import java.util.Map;

/** Resource providers for the knowledge MCP server. */
public final class KnowledgeResources {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private KnowledgeResources() {}

    public static String stats(KnowledgeBase kb) {
        KnowledgeBase.Stats s = kb.stats();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("documentCount", s.documentCount());
        result.put("chunkCount", s.chunkCount());
        result.put("byType", s.byType());
        try { return MAPPER.writeValueAsString(result); }
        catch (Exception e) { return "{}"; }
    }
}