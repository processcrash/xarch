package com.xarch.starter.storage.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MinIO storage provider.
 * <p>
 * Mirrors the configuration typically used by the MinIO console and
 * official Java SDK. {@code endpoint} should be a fully qualified URL such
 * as {@code http://localhost:9000}. {@code defaultBucket} is used when a
 * caller does not supply a bucket.
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "xarch.storage.configs.minio")
public class MinioProperties {

    /**
     * Whether the MinIO provider is enabled. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * MinIO endpoint URL, e.g. {@code http://localhost:9000}.
     */
    private String endpoint = "http://localhost:9000";

    /**
     * Access key (username).
     */
    private String accessKey;

    /**
     * Secret key (password).
     */
    private String secretKey;

    /**
     * Optional region (required for AWS S3, optional for MinIO).
     */
    private String region = "us-east-1";

    /**
     * Default bucket used when a caller does not specify one.
     */
    private String defaultBucket = "xarch";

    /**
     * Optional CDN/custom domain used to build access URLs.
     */
    private String cname;

    /**
     * Whether to automatically create the bucket on first use.
     */
    private boolean autoCreateBucket = true;
}
