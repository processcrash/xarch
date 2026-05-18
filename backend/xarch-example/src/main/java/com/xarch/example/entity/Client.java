package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Client entity for OAuth/sso client management
 */
@Data
@TableName("sys_client")
public class Client implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String clientName;

    private String grantTypes;

    private String scope;

    private Integer status;

    private String redirectUri;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}