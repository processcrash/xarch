package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Chat message entity — individual message in a {@link ChatSession}.
 *
 * <p>Stores the role (user / assistant / system), the message content, and
 * optional model metadata (token count, latency, etc.).</p>
 */
@Data
@Table("xarch_ai_chat_message")
public class ChatMessage implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    /** Owning session identifier (matches {@link ChatSession#getSessionId()}). */
    private String sessionId;

    /** Role of the message author: user / assistant / system. */
    private String role;

    /** Message body. */
    private String content;

    /** Model used to produce the message (assistant messages only). */
    private String model;

    /** Token count consumed by this message. */
    private Integer tokens;

    /** Generation latency in milliseconds. */
    private Long latencyMs;

    private Long createUserId;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    private Integer delFlag;
}
