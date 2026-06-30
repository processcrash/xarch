package com.xarch.starter.storage.core;

/**
 * Storage backend type enumeration.
 * <p>
 * Used to identify which underlying storage system a {@link StorageProvider}
 * implementation talks to. The starter ships with adapters for local
 * filesystem, MinIO, Aliyun OSS, and generic S3-compatible services.
 * </p>
 */
public enum StorageType {

    /** Local filesystem storage. */
    LOCAL("local", "Local Filesystem"),

    /** MinIO (and S3-compatible) object storage. */
    MINIO("minio", "MinIO Object Storage"),

    /** Aliyun Object Storage Service. */
    ALIYUN_OSS("aliyun_oss", "Aliyun OSS"),

    /** Generic AWS S3 storage. */
    S3("s3", "AWS S3");

    private final String code;
    private final String description;

    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Return the short string code for the storage type.
     *
     * @return storage type code, never null
     */
    public String getCode() {
        return code;
    }

    /**
     * Return the human readable description.
     *
     * @return storage type description, never null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Resolve a {@link StorageType} from its short code.
     * <p>
     * Lookup is case-insensitive. If the supplied code does not match any
     * known type, {@link #LOCAL} is returned as a safe default to avoid
     * null-pointer issues in user code.
     * </p>
     *
     * @param code the short code to resolve
     * @return matching {@link StorageType}, never null
     */
    public static StorageType fromCode(String code) {
        if (code == null) {
            return LOCAL;
        }
        for (StorageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return LOCAL;
    }
}
