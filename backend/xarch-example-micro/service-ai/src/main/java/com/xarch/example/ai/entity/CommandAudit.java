package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/** AI command audit log entity. */
@Data
@Table("xarch_ai_command_audit")
public class CommandAudit {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private Long serverId;
    private String serverName;
    private Long userId;
    private String userName;

    private String command;
    private String aiGeneratedCommand;
    private String aiPrompt;

    private String output;
    private Integer exitCode;

    private Long duration;
    private String sessionId;

    private String userIp;
    private String userAgent;

    private Integer riskLevel;
    private Integer approvalStatus;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedTime;
    private String approvalComment;

    private Integer status;
    private Integer delFlag;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;
}