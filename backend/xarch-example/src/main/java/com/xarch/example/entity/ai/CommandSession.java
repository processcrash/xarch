package com.xarch.example.entity.ai;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Command session - groups commands for a specific interaction
 */
@Data
@TableName("ai_command_session")
public class CommandSession implements Serializable {

    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastActivityTime;

    /** Delete flag */
    private Integer delFlag;
}