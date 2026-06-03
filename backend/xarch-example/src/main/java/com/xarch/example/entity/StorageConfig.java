package com.xarch.example.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Storage configuration entity
 */
@Data
@Table("sys_storage_config")
public class StorageConfig implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}