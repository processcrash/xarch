package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** AI command session entity — groups related commands. */
@Data
@Table("xarch_ai_command_session")
public class CommandSession implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String sessionId;

    private Long serverId;

    private Long userId;

    private String title;

    private Integer commandCount;

    private Integer successCount;

    private Integer failedCount;

    private Integer status;

    private Long totalDuration;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime lastActivityTime;

    private Integer delFlag;
}