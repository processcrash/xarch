package com.xarch.mcp.vector.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.mcp.vector.VectorConfig;
import com.xarch.mcp.vector.store.CollectionConfig;
import com.xarch.mcp.vector.store.VectorCollection;
import com.xarch.mcp.vector.store.VectorStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resource providers for the vector MCP server. */
public final class VectorResources {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private VectorResources() {}

    public static String config(VectorConfig config) {
        try { return MAPPER.writeValueAsString(config.snapshot()); }
        catch (Exception e) { return "{}"; }
    }

    public static String collections(VectorStore store) {
        List<Map<String, Object>> cols = new ArrayList<>();
        for (CollectionConfig cfg : store.listCollections()) {
            VectorCollection col = store.getCollection(cfg.name()).orElseThrow();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", cfg.name());
            m.put("dimension", cfg.dimension());
            m.put("distance", cfg.metric().name().toLowerCase());
            m.put("count", col.size());
            cols.add(m);
        }
        try { return MAPPER.writeValueAsString(cols); }
        catch (Exception e) { return "[]"; }
    }
}