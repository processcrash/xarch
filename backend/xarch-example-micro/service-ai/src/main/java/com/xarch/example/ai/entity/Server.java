package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** AI-managed Linux server entity. */
@Data
@Table("xarch_ai_server")
public class Server implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String name;

    private String host;

    private Integer port;

    private String username;

    private String authType;

    private String password;

    private String privateKey;

    private String passphrase;

    private String description;

    private String serverGroup;

    private String osType;

    private Integer status;

    private LocalDateTime lastConnectedTime;

    private String lastError;

    private String tags;

    private Long createUserId;

    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}