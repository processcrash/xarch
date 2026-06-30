package com.xarch.starter.storage.aliyun;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Aliyun OSS storage provider.
 */
@Data
@ConfigurationProperties(prefix = "xarch.storage.configs.aliyun-oss")
public class AliyunOssProperties {

    /** Whether the Aliyun OSS provider is enabled. */
    private boolean enabled = true;

    /** OSS endpoint, e.g. {@code https://oss-cn-hangzhou.aliyuncs.com}. */
    private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";

    /** Access key ID. */
    private String accessKeyId;

    /** Access key secret. */
    private String accessKeySecret;

    /** Default bucket used when a caller does not supply one. */
    private String bucketName = "xarch";

    /** Optional security token for STS-based temporary credentials. */
    private String securityToken;

    /** Optional CDN/custom domain used to build access URLs. */
    private String cname;

    /** Whether to automatically create the bucket on first use. */
    private boolean autoCreateBucket = true;
}
