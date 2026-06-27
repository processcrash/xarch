package com.xarch.example.message.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** OAuth/SSO client entity owned by {@code service-message}. */
@Data
@Table("xarch_message_client")
public class Client implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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