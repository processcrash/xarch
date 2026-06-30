package com.xarch.starter.storage.local;

import com.xarch.starter.storage.core.AbstractStorageProvider;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;

/**
 * {@link com.xarch.starter.storage.core.StorageProvider} implementation
 * backed by the local filesystem.
 * <p>
 * Each object is stored at
 * {@code <basePath>/<bucket>/<objectKey>}. The bucket segment is part of
 * the path so multiple logical buckets can coexist on a single disk.
 * </p>
 */
public class LocalStorageProvider extends AbstractStorageProvider {

    private final Path basePath;
    private final String publicBaseUrl;

    /**
     * Create a new local storage provider.
     *
     * @param properties the local storage configuration properties
     */
    public LocalStorageProvider(LocalStorageProperties properties) {
        this(properties.getBasePath(), properties.getPublicBaseUrl());
    }

    /**
     * Create a new local storage provider.
     *
     * @param basePath       base directory on the filesystem
     * @param publicBaseUrl  URL prefix used to build access URLs
     */
    public LocalStorageProvider(String basePath, String publicBaseUrl) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl == null ? "/files" : publicBaseUrl;
        try {
            Files.createDirectories(this.basePath);
            log.info("Local storage base path: {}", this.basePath);
        } catch (IOException e) {
            throw new StorageException("Failed to create local storage base path: " + this.basePath, e);
        }
    }

    @Override
    public StorageResult putObject(String bucket, String objectKey, InputStream is,
                                   long size, String contentType) {
        requireNonBlank(bucket, "bucket");
        requireNonBlank(objectKey, "objectKey");
        Path target = resolvePath(bucket, objectKey);
        try {
            Files.createDirectories(target.getParent());
            byte[] data = readAllBytes(is);
            Files.write(target, data);
            String etag = md5(data);
            log.info("Stored object bucket={} key={} size={}", bucket, objectKey, data.length);
            return StorageResult.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .accessUrl(buildAccessUrl(bucket, objectKey))
                    .etag(etag)
                    .size(data.length)
                    .contentType(contentType)
                    .build();
        } catch (IOException e) {
            throw new StorageException("Failed to write object " + objectKey + " to local storage", e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        Path path = resolvePath(bucket, objectKey);
        if (!Files.exists(path)) {
            throw new StorageException("Object not found: " + bucket + "/" + objectKey);
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new StorageException("Failed to read object " + objectKey + " from local storage", e);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        Path path = resolvePath(bucket, objectKey);
        try {
            Files.deleteIfExists(path);
            log.info("Deleted object bucket={} key={}", bucket, objectKey);
        } catch (IOException e) {
            throw new StorageException("Failed to delete object " + objectKey + " from local storage", e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        return Files.exists(resolvePath(bucket, objectKey));
    }

    @Override
    public String getPresignedUrl(String bucket, String objectKey, Duration expiry) {
        // Local storage does not support signed URLs, return a public URL instead
        log.debug("Local storage does not support presigned URLs, returning public URL for {}",
                objectKey);
        return buildAccessUrl(bucket, objectKey);
    }

    @Override
    public String getAccessUrl(String bucket, String objectKey) {
        return buildAccessUrl(bucket, objectKey);
    }

    @Override
    public StorageType getType() {
        return StorageType.LOCAL;
    }

    /**
     * Resolve a storage path for the given bucket and key, applying path
     * traversal protection.
     *
     * @param bucket    bucket name
     * @param objectKey object key
     * @return the resolved absolute path
     */
    private Path resolvePath(String bucket, String objectKey) {
        Path resolved = basePath
                .resolve(bucket)
                .resolve(objectKey)
                .normalize();
        if (!resolved.startsWith(basePath)) {
            throw new StorageException("Invalid object key: " + objectKey);
        }
        return resolved;
    }

    /**
     * Build the public access URL for the given object.
     *
     * @param bucket    bucket name
     * @param objectKey object key
     * @return URL string
     */
    private String buildAccessUrl(String bucket, String objectKey) {
        String prefix = publicBaseUrl;
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + bucket + "/" + objectKey;
    }

    /**
     * Copy a local object to another location, useful for replication and
     * snapshotting.
     *
     * @param sourceBucket source bucket
     * @param sourceKey    source key
     * @param targetPath   destination path
     * @throws StorageException on copy failure
     */
    public void copyToPath(String sourceBucket, String sourceKey, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(resolvePath(sourceBucket, sourceKey), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Failed to copy local object " + sourceKey, e);
        }
    }

    /**
     * Expose the configured base path. Useful for tests and diagnostics.
     *
     * @return absolute base path
     */
    public Path getBasePath() {
        return basePath;
    }

    /**
     * Return the size in bytes of the local file referenced by the given
     * bucket/key, or {@code -1} if the file does not exist.
     *
     * @param bucket    bucket name
     * @param objectKey object key
     * @return file size in bytes or -1
     */
    public long sizeOf(String bucket, String objectKey) {
        try {
            return Files.size(resolvePath(bucket, objectKey));
        } catch (IOException e) {
            return -1L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "LocalStorageProvider{basePath=%s, publicBaseUrl=%s}",
                basePath, publicBaseUrl);
    }
}
