package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Login log entity
 */
@Data
@Table("sys_login_log")
public class LoginLog implements Serializable {

    @Id(auto = true)
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