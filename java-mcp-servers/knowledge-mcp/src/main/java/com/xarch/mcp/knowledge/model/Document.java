package com.xarch.mcp.knowledge.model;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable document stored in the knowledge base.
 *
 * <p>A document has a stable identity ({@code id}), a human-readable
 * {@code title}, the original {@code content} body, a free-form
 * {@code type} tag (e.g. "article", "policy", "faq"), an arbitrary
 * {@code metadata} map, and the {@code createdAt} timestamp.
 *
 * @param id        unique document identifier
 * @param title     human-readable title
 * @param content   the original text body
 * @param type      document type tag (defaults to "document")
 * @param metadata  arbitrary additional key/value metadata
 * @param createdAt creation timestamp (UTC)
 */
public record Document(
        String id,
        String title,
        String content,
        String type,
        Map<String, Object> metadata,
        Instant createdAt
) {
}