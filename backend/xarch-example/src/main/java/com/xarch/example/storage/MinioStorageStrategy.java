package com.xarch.example.storage;

import com.xarch.example.entity.StorageConfig;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO storage strategy
 * Supports MinIO object storage and compatible S3-compatible services
 */
@Component
public class MinioStorageStrategy implements StorageStrategy {

    @Autowired
    private StorageConfigService storageConfigService;

    @Override
    public String upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            }

            client.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, contentLength, -1)
                    .contentType(contentType)
                    .build());

            return buildAccessUrl(config, objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }

    @Override
    public boolean download(String objectKey, OutputStream outputStream) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            client.getObject(GetObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .stream(outputStream)
                    .build());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from MinIO", e);
        }
    }

    @Override
    public boolean delete(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            client.statObject(StatObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }
        return buildAccessUrl(config, objectKey);
    }

    private String buildAccessUrl(StorageConfig config, String objectKey) {
        if (config.getDomain() != null && !config.getDomain().isEmpty()) {
            return config.getDomain().replaceAll("/$", "") + "/" + objectKey;
        }
        return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
    }

    @Override
    public String getObjectKeyFromUrl(String url) {
        // Extract object key from MinIO URL
        // Format: http://endpoint/bucket/key or https://domain/bucket/key
        if (url == null) return null;

        String[] patterns = {
                "/" + (getStorageType().getCode()) + "/",
                "/minio/",
                "/files/"
        };
        for (String pattern : patterns) {
            int idx = url.indexOf(pattern);
            if (idx > 0) {
                return url.substring(idx + pattern.length());
            }
        }
        return url;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MINIO;
    }

    /**
     * Get presigned URL for temporary access
     */
    public String getPresignedUrl(String objectKey, int expiryMinutes) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.MINIO);
        if (config == null) {
            throw new RuntimeException("MinIO storage not configured");
        }

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}