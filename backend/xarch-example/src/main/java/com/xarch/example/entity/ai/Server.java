package com.xarch.example.entity.ai;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Server entity for Linux server management
 */
@Data
@TableName("ai_server")
public class Server implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Server name */
    private String name;

    /** Server IP or hostname */
    private String host;

    /** SSH port */
    private Integer port;

    /** SSH username */
    private String username;

    /** Authentication type: password, key */
    private String authType;

    /** Encrypted password */
    private String password;

    /** Private key content */
    private String privateKey;

    /** Private key passphrase */
    private String passphrase;

    /** Server description */
    private String description;

    /** Server group/category */
    private String serverGroup;

    /** Operating system type (ubuntu, centos, debian, etc.) */
    private String osType;

    /** Connection status: 0-disconnected, 1-connected, 2-error */
    private Integer status;

    /** Last connection time */
    private LocalDateTime lastConnectedTime;

    /** Last error message */
    private String lastError;

    /** Tags for filtering */
    private String tags;

    /** Create user ID */
    private Long createUserId;

    /** Create user name */
    private String createUserName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Delete flag: 0-normal, 1-deleted */
    private Integer delFlag;
}