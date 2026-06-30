package com.xarch.starter.storage.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.xarch.starter.storage.core.AbstractStorageProvider;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;

import java.io.InputStream;
import java.time.Duration;
import java.util.Date;

/**
 * {@link com.xarch.starter.storage.core.StorageProvider} implementation
 * backed by Aliyun Object Storage Service (OSS).
 * <p>
 * The underlying {@link OSS} client is thread-safe and pooled internally by
 * the SDK, so a single instance can be shared across requests.
 * </p>
 */
public class AliyunOssStorageProvider extends AbstractStorageProvider {

    private final OSS client;
    private final AliyunOssProperties properties;

    /**
     * Create a new Aliyun OSS storage provider.
     *
     * @param properties the Aliyun OSS configuration
     */
    public AliyunOssStorageProvider(AliyunOssProperties properties) {
        this.properties = properties;
        if (properties.getAccessKeyId() == null || properties.getAccessKeySecret() == null) {
            throw new StorageException("Aliyun OSS accessKeyId and accessKeySecret must be configured");
        }
        this.client = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getSecurityToken()
        );
        log.info("Initialised AliyunOssStorageProvider endpoint={} bucket={}",
                properties.getEndpoint(), properties.getBucketName());
    }

    @Override
    public StorageResult putObject(String bucket, String objectKey, InputStream is,
                                   long size, String contentType) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        ensureBucket(bucket);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size > 0 ? size : -1);
            if (contentType != null && !contentType.isBlank()) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, is, metadata);
            PutObjectResult result = client.putObject(request);
            log.info("Stored object bucket={} key={} size={} etag={}",
                    bucket, objectKey, size, result.getETag());
            return StorageResult.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .accessUrl(buildAccessUrl(bucket, objectKey))
                    .etag(result.getETag())
                    .size(size)
                    .contentType(contentType)
                    .build();
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to Aliyun OSS: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            OSSObject object = client.getObject(bucket, objectKey);
            if (object == null) {
                throw new StorageException("Object not found: " + bucket + "/" + objectKey);
            }
            return object.getObjectContent();
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to download object from Aliyun OSS: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            client.deleteObject(bucket, objectKey);
            log.info("Deleted object bucket={} key={}", bucket, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object from Aliyun OSS: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        try {
            return client.doesObjectExist(bucket, objectKey);
        } catch (Exception e) {
            log.warn("Existence check failed for {}/{}: {}", bucket, objectKey, e.getMessage());
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String objectKey, Duration expiry) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            Date expiration = new Date(System.currentTimeMillis() + expiry.toMillis());
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey);
            request.setExpiration(expiration);
            return client.generatePresignedUrl(request).toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL for " + objectKey, e);
        }
    }

    @Override
    public String getAccessUrl(String bucket, String objectKey) {
        return buildAccessUrl(bucket, objectKey);
    }

    @Override
    public StorageType getType() {
        return StorageType.ALIYUN_OSS;
    }

    /**
     * Build the public access URL for an object. If a CNAME/CDN is
     * configured, use that; otherwise use the standard
     * {@code https://<bucket>.<endpoint>/<key>} pattern.
     */
    private String buildAccessUrl(String bucket, String objectKey) {
        if (properties.getCname() != null && !properties.getCname().isBlank()) {
            String prefix = properties.getCname();
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            return prefix + "/" + objectKey;
        }
        String endpoint = properties.getEndpoint();
        if (endpoint.startsWith("https://")) {
            endpoint = endpoint.substring("https://".length());
        } else if (endpoint.startsWith("http://")) {
            endpoint = endpoint.substring("http://".length());
        }
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return "https://" + bucket + "." + endpoint + "/" + objectKey;
    }

    /**
     * Ensure the bucket exists, creating it on demand if auto-create is
     * enabled in the configuration.
     */
    private void ensureBucket(String bucket) {
        try {
            if (!client.doesBucketExist(bucket) && properties.isAutoCreateBucket()) {
                client.createBucket(bucket);
                log.info("Auto-created Aliyun OSS bucket {}", bucket);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to ensure bucket exists: " + bucket, e);
        }
    }

    /**
     * Expose the underlying {@link OSS} client for advanced use cases.
     *
     * @return the internal OSS client
     */
    public OSS getClient() {
        return client;
    }
}
