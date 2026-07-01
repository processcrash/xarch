package com.xarch.mcp.vector.store;

import java.util.Map;

/**
 * A single KNN search result, with similarity score normalized to [0, 1].
 */
public record SearchHit(String id, double score, Map<String, Object> metadata) {}