package com.xarch.mcp.knowledge.model;

/**
 * A slice of a {@link Document} suitable for indexing and search.
 *
 * <p>Documents are split into chunks to keep individual search units small
 * and to enable scoring at sub-document granularity. Each chunk keeps a
 * reference to its parent document via {@code documentId} and a stable
 * {@code ordinal} indicating its position within the original document.
 *
 * @param id          unique chunk identifier
 * @param documentId  owning document id
 * @param content     chunk text
 * @param ordinal     0-based position of this chunk within the document
 */
public record Chunk(
        String id,
        String documentId,
        String content,
        int ordinal
) {
}