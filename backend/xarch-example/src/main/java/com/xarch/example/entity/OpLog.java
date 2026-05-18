package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Operation log entity
 */
@Data
@TableName("sys_op_log")
public class OpLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String operation;

    private String type;

    private String method;

    private String ip;

    private String location;

    private String params;

    private String result;

    private Integer status;

    private Long costTime;

    private LocalDateTime createTime;
}