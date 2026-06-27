package com.xarch.example.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Storage configuration entity. */
@Data
@Table("xarch_file_storage_config")
public class StorageConfig implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String storageType;

    private String configName;

    private Integer isDefault;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    private String basePath;

    private String domain;

    private Integer status;

    private String description;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}