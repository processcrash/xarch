package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Storage configuration entity
 */
@Data
@TableName("sys_storage_config")
public class StorageConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Storage type: local, minio, aliyun_oss, qiniu, tencent_cos */
    private String storageType;

    /** Configuration name */
    private String configName;

    /** Is this the default configuration */
    private Integer isDefault;

    /** Endpoint URL (for MinIO, Aliyun OSS, etc.) */
    private String endpoint;

    /** Access key / Access ID */
    private String accessKey;

    /** Secret key */
    private String secretKey;

    /** Bucket name */
    private String bucketName;

    /** Region (for Aliyun OSS, Tencent COS) */
    private String region;

    /** Base path for files */
    private String basePath;

    /** Domain/CDN for accessing files */
    private String domain;

    /** Configuration status: 0-disabled, 1-enabled */
    private Integer status;

    /** Description */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}