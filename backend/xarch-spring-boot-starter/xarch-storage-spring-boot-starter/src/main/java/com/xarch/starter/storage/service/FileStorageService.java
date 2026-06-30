package com.xarch.starter.storage.service;

import com.xarch.starter.storage.StorageProperties;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageProvider;
import com.xarch.starter.storage.core.StorageProviderFactory;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/**
 * High-level service facade over the storage providers.
 * <p>
 * Encapsulates the common concerns of any file upload workflow:
 * <ul>
 *   <li>Picking the right provider based on the configured default or the
 *       supplied {@link StorageType}.</li>
 *   <li>Resolving the target bucket (per-call or default).</li>
 *   <li>Generating a unique, collision-free object key.</li>
 *   <li>Detecting the content type from the file metadata.</li>
 *   <li>Wrapping {@link MultipartFile} uploads and presigned URL lookups.</li>
 * </ul>
 * </p>
 */
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final StorageProviderFactory providerFactory;
    private final StorageProperties properties;

    /**
     * Create a new service instance.
     *
     * @param providerFactory the provider factory
     * @param properties      the storage configuration
     */
    public FileStorageService(StorageProviderFactory providerFactory, StorageProperties properties) {
        this.providerFactory = providerFactory;
        this.properties = properties;
    }

    /**
     * Upload a {@link MultipartFile} using default options.
     *
     * @param file the multipart file to upload
     * @return a {@link StorageResult} describing the stored object
     * @throws StorageException on upload failure
     */
    public StorageResult upload(MultipartFile file) {
        return upload(file, UploadOptions.defaults());
    }

    /**
     * Upload a {@link MultipartFile} with custom options.
     *
     * @param file    the multipart file to upload
     * @param options the upload options
     * @return a {@link StorageResult} describing the stored object
     * @throws StorageException on upload failure
     */
    public StorageResult upload(MultipartFile file, UploadOptions options) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot upload empty file");
        }
        StorageType type = options.storageType() != null
                ? options.storageType()
                : properties.resolvedDefaultType();
        StorageProvider provider = providerFactory.getProvider(type);
        String bucket = resolveBucket(type, options.bucket());
        String objectKey = generateObjectKey(options, file.getOriginalFilename());
        String contentType = options.contentType() != null
                ? options.contentType()
                : (file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        try (InputStream is = file.getInputStream()) {
            return provider.putObject(bucket, objectKey, is, file.getSize(), contentType);
        } catch (IOException e) {
            throw new StorageException("Failed to read multipart file", e);
        }
    }

    /**
     * Upload raw bytes using default options.
     *
     * @param bytes     the content bytes
     * @param filename  optional original filename (used to derive the key)
     * @param mimeType  the content type
     * @return a {@link StorageResult}
     */
    public StorageResult upload(byte[] bytes, String filename, String mimeType) {
        return upload(bytes, filename, mimeType, UploadOptions.defaults());
    }

    /**
     * Upload raw bytes with custom options.
     *
     * @param bytes     the content bytes
     * @param filename  optional original filename
     * @param mimeType  the content type
     * @param options   the upload options
     * @return a {@link StorageResult}
     */
    public StorageResult upload(byte[] bytes, String filename, String mimeType, UploadOptions options) {
        if (bytes == null) {
            throw new StorageException("Cannot upload null bytes");
        }
        StorageType type = options.storageType() != null
                ? options.storageType()
                : properties.resolvedDefaultType();
        StorageProvider provider = providerFactory.getProvider(type);
        String bucket = resolveBucket(type, options.bucket());
        String objectKey = generateObjectKey(options, filename);
        String contentType = options.contentType() != null
                ? options.contentType()
                : (mimeType != null ? mimeType : "application/octet-stream");
        try (InputStream is = new java.io.ByteArrayInputStream(bytes)) {
            return provider.putObject(bucket, objectKey, is, bytes.length, contentType);
        } catch (IOException e) {
            throw new StorageException("Failed to upload bytes", e);
        }
    }

    /**
     * Download an object as an {@link InputStream}.
     *
     * @param storageType the storage type to use
     * @param bucket      the bucket
     * @param objectKey   the object key
     * @return a non-null input stream; caller is responsible for closing
     */
    public InputStream download(StorageType storageType, String bucket, String objectKey) {
        StorageProvider provider = providerFactory.getProvider(storageType);
        String resolvedBucket = bucket != null ? bucket : resolveBucket(storageType, null);
        return provider.getObject(resolvedBucket, objectKey);
    }

    /**
     * Delete an object.
     *
     * @param storageType the storage type
     * @param bucket      the bucket
     * @param objectKey   the object key
     */
    public void delete(StorageType storageType, String bucket, String objectKey) {
        StorageProvider provider = providerFactory.getProvider(storageType);
        String resolvedBucket = bucket != null ? bucket : resolveBucket(storageType, null);
        provider.deleteObject(resolvedBucket, objectKey);
    }

    /**
     * Generate a presigned URL for the given object.
     *
     * @param storageType the storage type
     * @param bucket      the bucket
     * @param objectKey   the object key
     * @param expiry      expiry duration
     * @return the presigned URL
     */
    public String presignedUrl(StorageType storageType, String bucket, String objectKey, Duration expiry) {
        StorageProvider provider = providerFactory.getProvider(storageType);
        String resolvedBucket = bucket != null ? bucket : resolveBucket(storageType, null);
        return provider.getPresignedUrl(resolvedBucket, objectKey, expiry);
    }

    /**
     * Return the public access URL for the given object.
     *
     * @param storageType the storage type
     * @param bucket      the bucket
     * @param objectKey   the object key
     * @return the access URL
     */
    public String accessUrl(StorageType storageType, String bucket, String objectKey) {
        StorageProvider provider = providerFactory.getProvider(storageType);
        String resolvedBucket = bucket != null ? bucket : resolveBucket(storageType, null);
        return provider.getAccessUrl(resolvedBucket, objectKey);
    }

    /**
     * Resolve the target bucket for the given storage type, applying the
     * per-call override or falling back to the configured default.
     *
     * @param type           the storage type
     * @param explicitBucket the per-call bucket override (may be null)
     * @return a non-null bucket name
     */
    private String resolveBucket(StorageType type, String explicitBucket) {
        if (explicitBucket != null && !explicitBucket.isBlank()) {
            return explicitBucket;
        }
        var configs = properties.getConfigs();
        if (configs == null) {
            return "default";
        }
        var config = configs.get(type.getCode());
        if (config != null && config.getDefaultBucket() != null) {
            return config.getDefaultBucket();
        }
        return switch (type) {
            case LOCAL, MINIO, ALIYUN_OSS, S3 -> "xarch";
        };
    }

    /**
     * Generate a unique object key for the given options.
     *
     * @param options  the upload options
     * @param filename the original filename (may be null)
     * @return a non-null object key
     */
    private String generateObjectKey(UploadOptions options, String filename) {
        String prefix = "";
        StorageType type = options.storageType() != null
                ? options.storageType()
                : properties.resolvedDefaultType();
        var config = properties.getConfigs() == null ? null
                : properties.getConfigs().get(type.getCode());
        if (config != null && config.getKeyPrefix() != null) {
            prefix = config.getKeyPrefix();
            if (!prefix.isEmpty() && !prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
        }
        if (options.objectKeyPrefix() != null) {
            prefix = prefix + options.objectKeyPrefix();
            if (!prefix.isEmpty() && !prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
        }
        if (!options.generateUniqueKey()) {
            return prefix + (filename == null ? UUID.randomUUID().toString() : filename);
        }
        String suffix = filename != null ? suffixOf(filename) : "";
        return String.format(Locale.ROOT, "%s%s%s", prefix, UUID.randomUUID(), suffix);
    }

    /**
     * Extract the file extension (including the dot) from a filename.
     *
     * @param filename the original filename
     * @return the extension, or empty string if none
     */
    private String suffixOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot);
    }

    /**
     * Expose the underlying provider factory.
     *
     * @return the {@link StorageProviderFactory}
     */
    public StorageProviderFactory getProviderFactory() {
        return providerFactory;
    }
}
