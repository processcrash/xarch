package com.xarch.starter.storage;

import com.xarch.starter.storage.core.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration properties for the storage starter.
 * <p>
 * Binds to the {@code xarch.storage} prefix. Use {@link #defaultType} to
 * select which backend is used by default when callers do not specify one.
 * Provider-specific settings live in {@link #configs} keyed by
 * {@link StorageType#getCode()}.
 * </p>
 *
 * <pre>
 * xarch:
 *   storage:
 *     default-type: minio
 *     configs:
 *       local:
 *         base-path: /var/xarch/files
 *       minio:
 *         endpoint: http://localhost:9000
 *         access-key: minioadmin
 *         secret-key: minioadmin
 *         default-bucket: xarch
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "xarch.storage")
public class StorageProperties {

    /**
     * The default storage backend to use.
     * <p>
     * One of {@code local}, {@code minio}, {@code aliyun_oss}, {@code s3}.
     * Defaults to {@code local} so that the starter works out of the box
     * without any further configuration.
     * </p>
     */
    private String defaultType = "local";

    /**
     * Map of backend-specific configurations, keyed by
     * {@link com.xarch.starter.storage.core.StorageType#getCode()}.
     */
    private Map<String, BackendConfig> configs = new HashMap<>();

    /**
     * Resolve the {@link StorageType} corresponding to {@link #defaultType}.
     *
     * @return the configured default storage type, never null
     */
    public StorageType resolvedDefaultType() {
        return StorageType.fromCode(defaultType);
    }

    /**
     * Common settings shared by all provider types.
     */
    @Data
    public static class BackendConfig {

        /** Whether this backend is enabled. */
        private boolean enabled = true;

        /** Optional default bucket for this backend. */
        private String defaultBucket;

        /** Optional public/CDN domain used to build access URLs. */
        private String cname;

        /** Optional prefix prepended to every object key. */
        private String keyPrefix;
    }
}
