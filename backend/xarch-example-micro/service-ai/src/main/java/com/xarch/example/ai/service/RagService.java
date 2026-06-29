package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.RagDocument;
import com.xarch.example.ai.entity.RagKnowledgeBase;
import com.xarch.starter.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * RAG service contract — knowledge base / document management and
 * semantic search.
 *
 * <p>Production wiring should delegate to the
 * {@code knowledge-mcp} / {@code vector-mcp} servers. The current
 * implementation persists knowledge bases / documents and returns
 * deterministic stub results for search.</p>
 */
public interface RagService {

    /** Page through knowledge bases. */
    PageResult<RagKnowledgeBase> pageKnowledgeBases(String keyword, int pageNum, int pageSize);

    /** List every knowledge base. */
    List<RagKnowledgeBase> listKnowledgeBases();

    /** Get a knowledge base by id. */
    RagKnowledgeBase getKnowledgeBase(Long id);

    /** Create a knowledge base. */
    RagKnowledgeBase createKnowledgeBase(RagKnowledgeBase kb);

    /** Update a knowledge base. */
    RagKnowledgeBase updateKnowledgeBase(RagKnowledgeBase kb);

    /** Delete a knowledge base. */
    void deleteKnowledgeBase(Long id);

    /** Page documents in a knowledge base. */
    PageResult<RagDocument> pageDocuments(Long knowledgeBaseId, String keyword,
                                          int pageNum, int pageSize);

    /** Ingest a document. */
    RagDocument ingest(IngestRequest request);

    /** Delete a document. */
    void deleteDocument(Long id);

    /** Semantic search. */
    List<SearchHit> search(SearchRequest request);

    /** Ingest request bundle. */
    final class IngestRequest {
        private Long knowledgeBaseId;
        private String title;
        private String sourceUri;
        private String contentType;
        private Long createUserId;
        private String createUserName;

        public Long getKnowledgeBaseId() { return knowledgeBaseId; }
        public void setKnowledgeBaseId(Long knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSourceUri() { return sourceUri; }
        public void setSourceUri(String sourceUri) { this.sourceUri = sourceUri; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getCreateUserId() { return createUserId; }
        public void setCreateUserId(Long createUserId) { this.createUserId = createUserId; }
        public String getCreateUserName() { return createUserName; }
        public void setCreateUserName(String createUserName) { this.createUserName = createUserName; }
    }

    /** Search request bundle. */
    final class SearchRequest {
        private Long knowledgeBaseId;
        private String query;
        private int topK = 5;

        public Long getKnowledgeBaseId() { return knowledgeBaseId; }
        public void setKnowledgeBaseId(Long knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
    }

    /** Search hit payload returned to the client. */
    final class SearchHit {
        private Long documentId;
        private String title;
        private String snippet;
        private double score;
        private Map<String, Object> metadata;

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
