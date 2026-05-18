package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Login log entity
 */
@Data
@TableName("sys_login_log")
public class LoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String ip;

    private String location;

    private LocalDateTime loginTime;

    private Integer loginType;

    private Integer status;

    private String message;
}