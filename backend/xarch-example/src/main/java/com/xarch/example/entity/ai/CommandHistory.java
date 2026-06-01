package com.xarch.example.entity.ai;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Command history - records all command executions
 */
@Data
@Table("ai_command_history")
public class CommandHistory implements Serializable {

    @Id(auto = true)
    private Long id;

    /** Server ID */
    private Long serverId;

    /** Server name (denormalized for history) */
    private String serverName;

    /** User who executed the command */
    private Long userId;

    /** User name */
    private String userName;

    /** Command that was executed */
    private String command;

    /** AI generated command (if applicable) */
    private String aiGeneratedCommand;

    /** Original natural language prompt (if AI generated) */
    private String aiPrompt;

    /** Command output */
    private String output;

    /** Exit code */
    private Integer exitCode;

    /** Execution duration in milliseconds */
    private Long duration;

    /** Execution status: 0-running, 1-success, 2-failed */
    private Integer status;

    /** Session ID for grouping related commands */
    private String sessionId;

    /** Working directory */
    private String workingDir;

    /** IP address of the user */
    private String userIp;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    /** Delete flag: 0-normal, 1-deleted */
    private Integer delFlag;
}