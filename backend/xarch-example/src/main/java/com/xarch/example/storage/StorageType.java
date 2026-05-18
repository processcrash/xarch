package com.xarch.example.storage;

/**
 * Storage type enumeration
 */
public enum StorageType {
    LOCAL("local", "本地存储"),
    MINIO("minio", "MinIO 对象存储"),
    ALIYUN_OSS("aliyun_oss", "阿里云 OSS"),
    QINIU("qiniu", "七牛云存储"),
    TENCENT_COS("tencent_cos", "腾讯云 COS");

    private final String code;
    private final String description;

    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return LOCAL;
    }
}