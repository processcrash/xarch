package com.xarch.mcp.vector.store;

/**
 * A single KNN search result, with similarity score normalized to [0, 1].
 */
public record SearchHit(String id, double score, Map<String, Object> metadata) {}
