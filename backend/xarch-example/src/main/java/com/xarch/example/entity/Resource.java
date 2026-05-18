package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Resource entity for file management
 */
@Data
@TableName("sys_resource")
public class Resource implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String resourceName;

    private String objectKey;

    private String accessUrl;

    private String sceneCode;

    private Long fileSize;

    private String fileType;

    private Long createUserId;

    private String createUserName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}