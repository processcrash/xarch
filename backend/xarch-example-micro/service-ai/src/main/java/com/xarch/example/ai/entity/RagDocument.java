package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RAG document — a single document ingested into a
 * {@link RagKnowledgeBase}. Documents are split into chunks by the
 * configured chunker and vectorised into the backing vector store.
 */
@Data
@Table("xarch_ai_rag_document")
public class RagDocument implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private Long knowledgeBaseId;

    private String title;

    /** Source URI or path of the document. */
    private String sourceUri;

    /** Mime / content type. */
    private String contentType;

    private Long sizeBytes;

    private Integer chunkCount;

    /** Lifecycle status: 0=pending, 1=indexed, 2=failed. */
    private Integer status;

    private String errorMessage;

    private Long createUserId;
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}
