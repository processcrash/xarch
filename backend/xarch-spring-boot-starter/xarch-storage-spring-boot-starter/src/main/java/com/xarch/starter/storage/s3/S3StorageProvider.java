package com.xarch.starter.storage.s3;

import com.xarch.starter.storage.core.AbstractStorageProvider;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

/**
 * {@link com.xarch.starter.storage.core.StorageProvider} implementation
 * backed by AWS S3 (or any S3-compatible service) using AWS SDK v2.
 */
public class S3StorageProvider extends AbstractStorageProvider {

    private final S3Client client;
    private final S3Presigner presigner;
    private final S3Properties properties;

    /**
     * Create a new S3 storage provider.
     *
     * @param properties the S3 configuration
     */
    public S3StorageProvider(S3Properties properties) {
        this.properties = properties;
        if (properties.getAccessKeyId() == null || properties.getSecretAccessKey() == null) {
            throw new StorageException("S3 accessKeyId and secretAccessKey must be configured");
        }
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .build();

        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())))
                .serviceConfiguration(s3Config);

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(properties.getEndpoint()));
        }
        this.client = builder.build();

        var presignerBuilder = S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())))
                .serviceConfiguration(s3Config);
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            presignerBuilder = presignerBuilder.endpointOverride(URI.create(properties.getEndpoint()));
        }
        this.presigner = presignerBuilder.build();

        log.info("Initialised S3StorageProvider region={} defaultBucket={} endpoint={}",
                properties.getRegion(), properties.getDefaultBucket(), properties.getEndpoint());
    }

    @Override
    public StorageResult putObject(String bucket, String objectKey, InputStream is,
                                   long size, String contentType) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        ensureBucket(bucket);
        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentLength(size > 0 ? size : null);
            if (contentType != null && !contentType.isBlank()) {
                requestBuilder.contentType(contentType);
            }
            PutObjectResponse response = client.putObject(requestBuilder.build(),
                    RequestBody.fromInputStream(is, size));
            log.info("Stored object bucket={} key={} size={} etag={}",
                    bucket, objectKey, size, response.eTag());
            return StorageResult.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .accessUrl(buildAccessUrl(bucket, objectKey))
                    .etag(response.eTag())
                    .size(size)
                    .contentType(contentType)
                    .build();
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to S3: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            return client.getObject(request, ResponseTransformer.toInputStream());
        } catch (S3Exception e) {
            throw new StorageException("Object not found in S3: " + bucket + "/" + objectKey, e);
        } catch (Exception e) {
            throw new StorageException("Failed to download object from S3: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            log.info("Deleted object bucket={} key={}", bucket, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object from S3: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        try {
            client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            return true;
        } catch (S3Exception e) {
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
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build())
                    .build();
            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
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
        return StorageType.S3;
    }

    /**
     * Build the public access URL for an object. If a CNAME/CDN is
     * configured, use that; otherwise ask the S3 client for the URL.
     */
    private String buildAccessUrl(String bucket, String objectKey) {
        if (properties.getCname() != null && !properties.getCname().isBlank()) {
            String prefix = properties.getCname();
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            return prefix + "/" + objectKey;
        }
        try {
            return client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build()).toString();
        } catch (Exception e) {
            log.warn("Failed to compute S3 access URL, falling back to standard pattern", e);
            String endpoint = properties.getEndpoint() != null ? properties.getEndpoint()
                    : "https://s3." + properties.getRegion() + ".amazonaws.com";
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint + "/" + bucket + "/" + objectKey;
        }
    }

    /**
     * Ensure the bucket exists, creating it on demand if auto-create is
     * enabled.
     */
    private void ensureBucket(String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (properties.isAutoCreateBucket() && e.statusCode() == 404) {
                client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Auto-created S3 bucket {}", bucket);
            } else if (!properties.isAutoCreateBucket()) {
                throw new StorageException("Bucket does not exist: " + bucket, e);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to ensure bucket exists: " + bucket, e);
        }
    }

    /**
     * Expose the underlying {@link S3Client} for advanced use cases.
     *
     * @return the internal S3 client
     */
    public S3Client getClient() {
        return client;
    }
}
