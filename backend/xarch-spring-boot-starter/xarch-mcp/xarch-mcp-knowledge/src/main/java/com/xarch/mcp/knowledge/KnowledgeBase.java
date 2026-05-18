package com.xarch.mcp.knowledge;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Knowledge Base with Vector Search
 * Supports document ingestion, chunking, and semantic search
 */
public class KnowledgeBase {

    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, List<Chunk>> chunks = new ConcurrentHashMap<>();
    private final Map<String, float[]> embeddings = new ConcurrentHashMap<>();

    public static class Document {
        private String id;
        private String title;
        private String content;
        private String type;
        private long createdAt;
        private Map<String, String> metadata;

        public Document() {}

        public Document(String id, String title, String content, String type) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.type = type;
            this.createdAt = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }

    public static class Chunk {
        private String id;
        private String documentId;
        private String content;
        private int chunkIndex;

        public Chunk() {}

        public Chunk(String id, String documentId, String content, int chunkIndex) {
            this.id = id;
            this.documentId = documentId;
            this.content = content;
            this.chunkIndex = chunkIndex;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public int getChunkIndex() { return chunkIndex; }
        public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    }

    /**
     * Index a document with automatic chunking
     */
    public void indexDocument(Document document, int chunkSize, int overlap) {
        documents.put(document.getId(), document);

        List<Chunk> docChunks = new ArrayList<>();
        String content = document.getContent();

        int start = 0;
        int chunkIndex = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String chunkText = content.substring(start, end);

            String chunkId = document.getId() + "_chunk_" + chunkIndex;
            Chunk chunk = new Chunk(chunkId, document.getId(), chunkText, chunkIndex);
            docChunks.add(chunk);

            // Generate simple embedding (in production, use AI embedding model)
            embeddings.put(chunkId, generateEmbedding(chunkText));

            start += chunkSize - overlap;
            chunkIndex++;
        }

        chunks.put(document.getId(), docChunks);
    }

    /**
     * Index a document from file
     */
    public void indexFile(String filePath, int chunkSize, int overlap) throws Exception {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);
        String fileName = path.getFileName().toString();
        String type = getFileType(fileName);

        String docId = UUID.randomUUID().toString();
        Document doc = new Document(docId, fileName, content, type);
        indexDocument(doc, chunkSize, overlap);
    }

    /**
     * Semantic search using cosine similarity
     */
    public SearchResult search(String query, int topK) {
        float[] queryEmbedding = generateEmbedding(query);

        List<ScoredChunk> scoredChunks = new ArrayList<>();

        for (Map.Entry<String, float[]> entry : embeddings.entrySet()) {
            String chunkId = entry.getKey();
            float[] embedding = entry.getValue();
            double similarity = cosineSimilarity(queryEmbedding, embedding);

            // Find the chunk content
            String chunkContent = findChunkContent(chunkId);
            String docId = findChunkDocumentId(chunkId);

            scoredChunks.add(new ScoredChunk(chunkId, docId, chunkContent, similarity));
        }

        // Sort by similarity and take top K
        scoredChunks.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        List<ScoredChunk> topResults = scoredChunks.subList(0, Math.min(topK, scoredChunks.size()));

        return new SearchResult(query, topResults);
    }

    /**
     * Get document by ID
     */
    public Document getDocument(String documentId) {
        return documents.get(documentId);
    }

    /**
     * Delete document from index
     */
    public boolean deleteDocument(String documentId) {
        Document removed = documents.remove(documentId);
        if (removed != null) {
            List<Chunk> docChunks = chunks.remove(documentId);
            if (docChunks != null) {
                for (Chunk chunk : docChunks) {
                    embeddings.remove(chunk.getId());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get document count
     */
    public int getDocumentCount() {
        return documents.size();
    }

    /**
     * Get total chunk count
     */
    public int getChunkCount() {
        return embeddings.size();
    }

    // Simple embedding generation (in production, use AI model)
    private float[] generateEmbedding(String text) {
        // Simple hash-based embedding for demonstration
        // In production, use: OpenAI embeddings, Ollama, or local AI model
        int dim = 128;
        float[] embedding = new float[dim];
        for (int i = 0; i < dim; i++) {
            embedding[i] = (float) Math.random() * 2 - 1;
        }

        // Modulate by text content
        for (char c : text.toCharArray()) {
            int idx = Math.abs(c) % dim;
            embedding[idx] += (c % 0.1) - 0.05;
        }

        // Normalize
        float norm = 0;
        for (float v : embedding) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < dim; i++) embedding[i] /= norm;
        }

        return embedding;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }

    private String findChunkContent(String chunkId) {
        for (List<Chunk> chunkList : chunks.values()) {
            for (Chunk chunk : chunkList) {
                if (chunk.getId().equals(chunkId)) {
                    return chunk.getContent();
                }
            }
        }
        return "";
    }

    private String findChunkDocumentId(String chunkId) {
        for (Map.Entry<String, List<Chunk>> entry : chunks.entrySet()) {
            for (Chunk chunk : entry.getValue()) {
                if (chunk.getId().equals(chunkId)) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    private String getFileType(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    public static class ScoredChunk {
        public String chunkId;
        public String documentId;
        public String content;
        public double similarity;

        public ScoredChunk(String chunkId, String documentId, String content, double similarity) {
            this.chunkId = chunkId;
            this.documentId = documentId;
            this.content = content;
            this.similarity = similarity;
        }
    }

    public static class SearchResult {
        public String query;
        public List<ScoredChunk> results;
        public long tookMs;

        public SearchResult(String query, List<ScoredChunk> results) {
            this.query = query;
            this.results = results;
            this.tookMs = System.currentTimeMillis();
        }
    }
}