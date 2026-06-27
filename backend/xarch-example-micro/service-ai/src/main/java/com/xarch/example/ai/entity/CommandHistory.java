package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** AI command history entity. */
@Data
@Table("xarch_ai_command_history")
public class CommandHistory implements Serializable {

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

    private Integer status;

    private String sessionId;

    private String workingDir;

    private String userIp;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    private Integer delFlag;
}