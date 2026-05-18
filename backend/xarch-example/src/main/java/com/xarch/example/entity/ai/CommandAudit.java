package com.xarch.example.entity.ai;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI Command Audit Log
 * Records all command executions for compliance and security
 */
@Data
@TableName("ai_command_audit")
public class CommandAudit {

    @TableId(type = IdType.AUTO)
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

    private Integer riskLevel; // 0=safe, 1=low, 2=medium, 3=high
    private Integer approvalStatus; // 0=pending, 1=approved, 2=rejected, 3=bypassed
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedTime;
    private String approvalComment;

    private Integer status; // 0=running, 1=success, 2=failed, 3=cancelled
    private Integer delFlag;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}