package com.xarch.starter.storage.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the generic AWS S3 storage provider.
 * <p>
 * Compatible with any AWS S3-compatible service such as Cloudflare R2,
 * DigitalOcean Spaces, or self-hosted MinIO/Ceph gateways.
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "xarch.storage.configs.s3")
public class S3Properties {

    /** Whether the S3 provider is enabled. */
    private boolean enabled = true;

    /** AWS region, e.g. {@code us-east-1}. */
    private String region = "us-east-1";

    /** Access key id. */
    private String accessKeyId;

    /** Secret access key. */
    private String secretAccessKey;

    /** Optional explicit endpoint override (e.g. for S3-compatible services). */
    private String endpoint;

    /** Default bucket used when a caller does not supply one. */
    private String defaultBucket = "xarch";

    /** Path-style access (true for most non-AWS endpoints). */
    private boolean pathStyleAccess = true;

    /** Optional CDN/custom domain used to build access URLs. */
    private String cname;

    /** Whether to automatically create the bucket on first use. */
    private boolean autoCreateBucket = true;
}
