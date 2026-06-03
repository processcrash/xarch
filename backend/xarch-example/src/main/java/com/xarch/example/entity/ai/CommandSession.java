package com.xarch.example.entity.ai;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Command session - groups commands for a specific interaction
 */
@Data
@Table("ai_command_session")
public class CommandSession implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    /** Session unique identifier */
    private String sessionId;

    /** Server ID */
    private Long serverId;

    /** User ID */
    private Long userId;

    /** Session title/description */
    private String title;

    /** Total commands in this session */
    private Integer commandCount;

    /** Success count */
    private Integer successCount;

    /** Failed count */
    private Integer failedCount;

    /** Session status: 0-active, 1-closed */
    private Integer status;

    /** Total duration in milliseconds */
    private Long totalDuration;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime lastActivityTime;

    /** Delete flag */
    private Integer delFlag;
}