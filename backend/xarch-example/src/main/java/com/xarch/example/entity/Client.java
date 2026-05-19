package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Client entity for OAuth/sso client management
 */
@Data
@Table("sys_client")
public class Client implements Serializable {

    @Id(auto = true)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String clientName;

    private String grantTypes;

    private String scope;

    private Integer status;

    private String redirectUri;

    private String description;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}