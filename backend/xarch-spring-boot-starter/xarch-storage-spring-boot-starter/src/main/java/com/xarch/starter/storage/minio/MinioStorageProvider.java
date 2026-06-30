package com.xarch.starter.storage.minio;

import com.xarch.starter.storage.core.AbstractStorageProvider;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.xarch.starter.storage.core.StorageProvider} implementation
 * backed by MinIO (or any S3-compatible service that speaks the MinIO
 * protocol).
 * <p>
 * Instances are thread-safe. The underlying {@link MinioClient} is shared
 * across calls; HTTP connections are pooled by the SDK.
 * </p>
 */
public class MinioStorageProvider extends AbstractStorageProvider {

    private final MinioClient client;
    private final MinioProperties properties;

    /**
     * Create a new MinIO storage provider.
     *
     * @param properties the MinIO configuration
     */
    public MinioStorageProvider(MinioProperties properties) {
        this.properties = properties;
        if (properties.getAccessKey() == null || properties.getSecretKey() == null) {
            throw new StorageException("MinIO accessKey and secretKey must be configured");
        }
        this.client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
        log.info("Initialised MinioStorageProvider endpoint={} defaultBucket={}",
                properties.getEndpoint(), properties.getDefaultBucket());
    }

    @Override
    public StorageResult putObject(String bucket, String objectKey, InputStream is,
                                   long size, String contentType) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        ensureBucket(bucket);
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(is, size, -1)
                    .contentType(contentType == null ? "application/octet-stream" : contentType)
                    .build());
            log.info("Stored object bucket={} key={} size={}", bucket, objectKey, size);
            return StorageResult.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .accessUrl(buildAccessUrl(bucket, objectKey))
                    .etag(null)
                    .size(size)
                    .contentType(contentType)
                    .build();
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to MinIO: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            return client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to download object from MinIO: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            log.info("Deleted object bucket={} key={}", bucket, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object from MinIO: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
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
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(Math.toIntExact(expiry.toSeconds()), TimeUnit.SECONDS)
                    .build());
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
        return StorageType.MINIO;
    }

    /**
     * Build the public access URL for an object. If a CNAME/CDN domain is
     * configured, use that; otherwise fall back to the standard
     * {@code <endpoint>/<bucket>/<key>} pattern.
     */
    private String buildAccessUrl(String bucket, String objectKey) {
        if (properties.getCname() != null && !properties.getCname().isBlank()) {
            String prefix = properties.getCname();
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            return prefix + "/" + bucket + "/" + objectKey;
        }
        String endpoint = properties.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + bucket + "/" + objectKey;
    }

    /**
     * Ensure the bucket exists, creating it on demand if auto-create is
     * enabled in the configuration.
     */
    private void ensureBucket(String bucket) {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists && properties.isAutoCreateBucket()) {
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .region(properties.getRegion())
                        .build());
                log.info("Auto-created MinIO bucket {}", bucket);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to ensure bucket exists: " + bucket, e);
        }
    }

    /**
     * Expose the underlying {@link MinioClient} for advanced use cases.
     *
     * @return the internal MinIO client
     */
    public MinioClient getClient() {
        return client;
    }
}
