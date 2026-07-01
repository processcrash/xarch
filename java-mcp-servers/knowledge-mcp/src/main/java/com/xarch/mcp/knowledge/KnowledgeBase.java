package com.xarch.mcp.knowledge;

import com.xarch.mcp.knowledge.model.Chunk;
import com.xarch.mcp.knowledge.model.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory knowledge base with chunked indexing and TF-IDF-like keyword
 * scoring. Thread-safe; all storage lives in {@link ConcurrentHashMap}s.
 *
 * <h2>Chunking strategy</h2>
 * Content is split first by paragraph (blank-line separated). If a single
 * paragraph exceeds {@code chunkSize} it is broken into overlapping
 * {@code chunkSize}-character windows with {@code overlap}-character
 * overlap. Empty input yields zero chunks.
 *
 * <h2>Search strategy</h2>
 * The query is tokenized to lowercase terms (length >= 2). For each
 * candidate chunk we count occurrences of every query term in the
 * chunk's lower-cased content; the score is {@code totalMatches /
 * chunkLength}. This is intentionally simple and deterministic so
 * behavior can be reproduced. Ties are broken by ordinal then chunk id,
 * which keeps ranking stable across runs.
 */
public class KnowledgeBase {

    /** Default chunk size in characters when callers don't override it. */
    public static final int DEFAULT_CHUNK_SIZE = 500;

    /** Default overlap in characters when callers don't override it. */
    public static final int DEFAULT_OVERLAP = 50;

    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, List<Chunk>> chunksByDocument = new ConcurrentHashMap<>();
    private final AtomicLong chunkSeq = new AtomicLong(0);

    // ------------------------------------------------------------------
    // Chunking
    // ------------------------------------------------------------------

    /**
     * Split {@code content} into chunks. Splits by paragraph first; if a
     * single paragraph is longer than {@code chunkSize} it is broken into
     * overlapping {@code chunkSize}-character windows with {@code overlap}
     * overlap. Empty or null content produces an empty list.
     *
     * @param content    text to chunk (may be null)
     * @param chunkSize  maximum characters per chunk (clamped to >= 1)
     * @param overlap    overlap between adjacent windows (clamped to {@code < chunkSize})
     * @return ordered list of chunks; never null
     */
    public List<String> chunk(String content, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return result;
        }
        int safeChunk = Math.max(1, chunkSize);
        int safeOverlap = Math.max(0, Math.min(overlap, safeChunk - 1));

        String[] paragraphs = content.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            String trimmed = para.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() <= safeChunk) {
                result.add(trimmed);
                continue;
            }
            int step = safeChunk - safeOverlap;
            for (int start = 0; start < trimmed.length(); start += step) {
                int end = Math.min(trimmed.length(), start + safeChunk);
                result.add(trimmed.substring(start, end));
                if (end == trimmed.length()) {
                    break;
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    /**
     * Index a new document. Generates an id when {@code id} is null/blank.
     * Chunking parameters fall back to {@link #DEFAULT_CHUNK_SIZE} and
     * {@link #DEFAULT_OVERLAP} when {@code <= 0}.
     *
     * @param title      document title (required)
     * @param content    document content (required)
     * @param type       document type tag (defaults to "document")
     * @param chunkSize  chunk size override; {@code <= 0} uses default
     * @param overlap    chunk overlap override; {@code <= 0} uses default
     * @param id         explicit id; null/blank auto-generates a UUID
     * @return the resolved id
     * @throws IllegalArgumentException if {@code title} or {@code content} is blank
     */
    public String indexDocument(String id,
                                String title,
                                String content,
                                String type,
                                int chunkSize,
                                int overlap) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        String resolvedId = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        int cs = chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
        int ov = overlap >= 0 ? overlap : DEFAULT_OVERLAP;

        Document doc = new Document(
                resolvedId,
                title,
                content,
                (type == null || type.isBlank()) ? "document" : type,
                Map.of(),
                Instant.now()
        );
        documents.put(resolvedId, doc);

        List<String> pieces = chunk(content, cs, ov);
        List<Chunk> stored = new ArrayList<>(pieces.size());
        for (int i = 0; i < pieces.size(); i++) {
            String chunkId = resolvedId + "#" + chunkSeq.incrementAndGet();
            stored.add(new Chunk(chunkId, resolvedId, pieces.get(i), i));
        }
        chunksByDocument.put(resolvedId, stored);
        return resolvedId;
    }

    /**
     * Look up a document by id.
     *
     * @param id document id
     * @return optional document
     */
    public Optional<Document> getDocument(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.get(id));
    }

    /**
     * Delete a document and its chunks.
     *
     * @param id document id
     * @return {@code true} if a document was removed
     */
    public boolean deleteDocument(String id) {
        if (id == null) {
            return false;
        }
        chunksByDocument.remove(id);
        return documents.remove(id) != null;
    }

    /**
     * Lightweight summary of every indexed document.
     *
     * @return immutable list of {@link DocumentSummary}
     */
    public List<DocumentSummary> listDocuments() {
        List<DocumentSummary> out = new ArrayList<>(documents.size());
        for (Document doc : documents.values()) {
            int count = chunksByDocument.getOrDefault(doc.id(), List.of()).size();
            out.add(new DocumentSummary(doc.id(), doc.title(), doc.type(), count, doc.createdAt()));
        }
        // Deterministic ordering: newest first by createdAt, then by id for stability.
        out.sort(Comparator
                .comparing(DocumentSummary::createdAt).reversed()
                .thenComparing(DocumentSummary::id));
        return List.copyOf(out);
    }

    /**
     * Replace an existing document's title, content, or both. Re-indexes
     * chunks when content changes.
     *
     * @param id          document id
     * @param newTitle    replacement title, or null to leave unchanged
     * @param newContent  replacement content, or null to leave unchanged
     * @return {@code true} if the document existed and was updated
     */
    public boolean updateDocument(String id, String newTitle, String newContent) {
        Document existing = documents.get(id);
        if (existing == null) {
            return false;
        }
        String title = (newTitle == null || newTitle.isBlank()) ? existing.title() : newTitle;
        String content = (newContent == null) ? existing.content() : newContent;

        Document updated = new Document(
                existing.id(),
                title,
                content,
                existing.type(),
                existing.metadata(),
                existing.createdAt()
        );
        documents.put(id, updated);

        if (newContent != null) {
            List<String> pieces = chunk(content, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
            List<Chunk> stored = new ArrayList<>(pieces.size());
            for (int i = 0; i < pieces.size(); i++) {
                String chunkId = id + "#" + chunkSeq.incrementAndGet();
                stored.add(new Chunk(chunkId, id, pieces.get(i), i));
            }
            chunksByDocument.put(id, stored);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------

    /**
     * Tokenize text into lower-case terms of length >= 2 (alphanumeric +
     * underscore). Whitespace and punctuation act as separators.
     */
    static List<String> tokenize(String text) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        String[] raw = text.toLowerCase().split("[^\\p{L}\\p{N}_]+");
        for (String tok : raw) {
            if (tok.length() >= 2) {
                out.add(tok);
            }
        }
        return out;
    }

    /**
     * Token-level keyword search. Each chunk is scored by the total
     * occurrence count of all query tokens divided by its character
     * length. Empty queries return an empty result.
     *
     * @param query search text
     * @param topK  maximum number of results to return
     * @return ordered list of {@link ScoredChunk}, highest score first
     */
    public List<ScoredChunk> search(String query, int topK) {
        List<String> tokens = tokenize(query);
        int k = topK > 0 ? topK : 5;
        if (tokens.isEmpty()) {
            return List.of();
        }

        List<ScoredChunk> scored = new ArrayList<>();
        for (Map.Entry<String, List<Chunk>> entry : chunksByDocument.entrySet()) {
            String documentId = entry.getKey();
            for (Chunk chunk : entry.getValue()) {
                String lower = chunk.content().toLowerCase();
                int totalHits = 0;
                for (String t : tokens) {
                    int from = 0;
                    while (true) {
                        int idx = lower.indexOf(t, from);
                        if (idx < 0) break;
                        totalHits++;
                        from = idx + t.length();
                    }
                }
                if (totalHits == 0) {
                    continue;
                }
                double score = (double) totalHits / Math.max(1, chunk.content().length());
                scored.add(new ScoredChunk(chunk.id(), documentId, chunk.content(), chunk.ordinal(), score));
            }
        }

        // Sort by score desc, then ordinal asc, then chunkId asc for determinism.
        scored.sort(Comparator
                .comparingDouble(ScoredChunk::score).reversed()
                .thenComparingInt(ScoredChunk::ordinal)
                .thenComparing(ScoredChunk::chunkId));

        if (scored.size() <= k) {
            return List.copyOf(scored);
        }
        return List.copyOf(scored.subList(0, k));
    }

    // ------------------------------------------------------------------
    // Stats
    // ------------------------------------------------------------------

    /**
     * Snapshot of knowledge base statistics.
     */
    public record Stats(int documentCount, int chunkCount, Map<String, Integer> byType) {}

    /**
     * Compute current statistics: total documents, total chunks, and
     * documents grouped by type.
     *
     * @return immutable {@link Stats}
     */
    public Stats stats() {
        Map<String, Integer> byType = new TreeMap<>();
        for (Document d : documents.values()) {
            byType.merge(d.type(), 1, Integer::sum);
        }
        int totalChunks = chunksByDocument.values().stream().mapToInt(List::size).sum();
        return new Stats(documents.size(), totalChunks, Map.copyOf(byType));
    }

    /**
     * @return unmodifiable view of every stored document (testing/inspection).
     */
    public Collection<Document> allDocuments() {
        return Map.copyOf(documents).values();
    }

    /**
     * @return chunks for a given document, or empty list if none.
     */
    public List<Chunk> chunksOf(String documentId) {
        if (documentId == null) return List.of();
        return List.copyOf(chunksByDocument.getOrDefault(documentId, List.of()));
    }

    // ------------------------------------------------------------------
    // Value types
    // ------------------------------------------------------------------

    /**
     * Lightweight per-document row used by {@link #listDocuments()}.
     */
    public record DocumentSummary(
            String id,
            String title,
            String type,
            int chunkCount,
            Instant createdAt
    ) {}

    /**
     * A chunk with its computed search score.
     */
    public record ScoredChunk(
            String chunkId,
            String documentId,
            String content,
            int ordinal,
            double score
    ) {}
}