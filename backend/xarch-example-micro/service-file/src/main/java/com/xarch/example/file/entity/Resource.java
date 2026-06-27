package com.xarch.example.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Resource entity owned by {@code service-file}. */
@Data
@Table("xarch_file_resource")
public class Resource implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    /** Resource name (original filename). */
    private String resourceName;

    /** Storage object key. */
    private String objectKey;

    /** Access URL. */
    private String accessUrl;

    /** Scene code for categorization. */
    private String sceneCode;

    /** File size in bytes. */
    private Long fileSize;

    /** MIME type. */
    private String fileType;

    /** Storage type: local, minio, aliyun_oss. */
    private String storageType;

    /** Business key for grouping. */
    private String bizKey;

    /** Create user ID. */
    private Long createUserId;

    /** Create user name. */
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    /** Delete flag: 0-normal, 1-deleted. */
    private Integer delFlag;
}