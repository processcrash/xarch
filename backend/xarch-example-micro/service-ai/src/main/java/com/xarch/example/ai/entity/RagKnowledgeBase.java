package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RAG knowledge base — a logical grouping of documents that share an
 * embedding model and retriever.
 */
@Data
@Table("xarch_ai_rag_knowledge_base")
public class RagKnowledgeBase implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String name;

    private String description;

    /** Embedding model used to vectorise the documents. */
    private String embeddingModel;

    /** Chunking strategy: recursive / sentence / fixed / markdown. */
    private String chunkStrategy;

    /** Chunk size in characters / tokens. */
    private Integer chunkSize;

    private Integer chunkOverlap;

    /** Vector store backend: pgvector / milvus / chroma. */
    private String vectorStore;

    private Integer documentCount;

    private Long totalChunks;

    private Long createUserId;
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}
