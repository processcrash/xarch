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

    /** Resource name (original filename) */
    private String resourceName;

    /** Storage object key */
    private String objectKey;

    /** Access URL */
    private String accessUrl;

    /** Scene code for categorization */
    private String sceneCode;

    /** File size in bytes */
    private Long fileSize;

    /** MIME type */
    private String fileType;

    /** Storage type: local, minio, aliyun_oss */
    private String storageType;

    /** Business key for grouping */
    private String bizKey;

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