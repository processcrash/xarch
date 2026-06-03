package com.xarch.example.storage;

import com.xarch.example.entity.StorageConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

/**
 * Aliyun OSS storage strategy
 * Supports Aliyun Object Storage Service
 */
@Component
public class AliyunOssStorageStrategy implements StorageStrategy {

    @Autowired
    private StorageConfigService storageConfigService;

    @Override
    public String upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }

        try {
            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );

            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentLength(contentLength > 0 ? contentLength : -1);
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            PutObjectRequest request = new PutObjectRequest(
                    config.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata
            );

            ossClient.putObject(request);

            return buildAccessUrl(config, objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to Aliyun OSS", e);
        }
    }

    @Override
    public boolean download(String objectKey, OutputStream outputStream) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }

        try {
            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );

            OSSObject object = ossClient.getObject(config.getBucketName(), objectKey);
            if (object == null) {
                return false;
            }

            try (InputStream inputStream = object.getObjectContent()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from Aliyun OSS", e);
        }
    }

    @Override
    public boolean delete(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }

        try {
            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );

            ossClient.deleteObject(config.getBucketName(), objectKey);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from Aliyun OSS", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }

        try {
            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );

            return ossClient.doesObjectExist(config.getBucketName(), objectKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }
        return buildAccessUrl(config, objectKey);
    }

    private String buildAccessUrl(StorageConfig config, String objectKey) {
        if (config.getDomain() != null && !config.getDomain().isEmpty()) {
            return config.getDomain().replaceAll("/$", "") + "/" + objectKey;
        }
        return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + objectKey;
    }

    @Override
    public String getObjectKeyFromUrl(String url) {
        // Extract object key from OSS URL
        // Format: https://bucket.endpoint/key or https://domain/key
        if (url == null) return null;

        // Try to extract after bucket name or after known patterns
        int idx = url.indexOf(".oss-");
        if (idx > 0) {
            int slashIdx = url.indexOf("/", idx + 5);
            if (slashIdx > 0) {
                return url.substring(slashIdx + 1);
            }
        }

        // Try domain-based extraction
        if (configHasDomain()) {
            int lastSlash = url.lastIndexOf("/");
            if (lastSlash > 0) {
                return url.substring(lastSlash + 1);
            }
        }

        return url;
    }

    private boolean configHasDomain() {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        return config != null && config.getDomain() != null && !config.getDomain().isEmpty();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.ALIYUN_OSS;
    }

    /**
     * Get presigned URL for temporary access
     */
    public String getPresignedUrl(String objectKey, int expiryMinutes) {
        StorageConfig config = storageConfigService.getDefaultConfig(StorageType.ALIYUN_OSS);
        if (config == null) {
            throw new RuntimeException("Aliyun OSS storage not configured");
        }

        try {
            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );

            Date expiration = new Date(System.currentTimeMillis() + expiryMinutes * 60 * 1000L);
            URL url = ossClient.generatePresignedUrl(config.getBucketName(), objectKey, expiration);
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}