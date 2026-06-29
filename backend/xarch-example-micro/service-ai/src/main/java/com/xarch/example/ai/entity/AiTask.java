package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI task — async, long-running work submitted to the agent
 * (model training, bulk RAG ingest, batch tool execution, ...). Status
 * lifecycle: pending -&gt; running -&gt; succeeded / failed / cancelled.
 */
@Data
@Table("xarch_ai_task")
public class AiTask implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String taskCode;

    private String name;

    private String type;

    private String description;

    /** Lifecycle status: 0=pending, 1=running, 2=succeeded, 3=failed, 4=cancelled. */
    private Integer status;

    private Integer progress;

    private String result;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long createUserId;
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}
