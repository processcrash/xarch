package com.xarch.example.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Login log entity
 */
@Data
@Table("sys_login_log")
public class LoginLog implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String username;

    private String ip;

    private String location;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime loginTime;

    private Integer loginType;

    private Integer status;

    private String message;
}