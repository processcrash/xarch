package com.xarch.starter.storage.core;

import java.io.InputStream;
import java.time.Duration;

/**
 * Unified storage provider abstraction.
 * <p>
 * Implementations of this interface encapsulate the concrete storage backend
 * (local filesystem, MinIO, Aliyun OSS, S3, ...). Consumers interact with the
 * abstraction rather than the underlying SDK so that the storage backend can
 * be swapped without changing call sites.
 * </p>
 * <p>
 * All methods may throw {@link StorageException} on failure. Callers are
 * expected to treat {@link InputStream} instances returned by
 * {@link #getObject(String, String)} as resources that must be closed
 * (try-with-resources is recommended).
 * </p>
 */
public interface StorageProvider {

    /**
     * Upload an object to the given bucket.
     *
     * @param bucket      the target bucket
     * @param objectKey   the unique object key within the bucket
     * @param is          the input stream of the content to upload
     * @param size        the content size in bytes (use -1 if unknown)
     * @param contentType the MIME type, may be null
     * @return a {@link StorageResult} describing the stored object
     * @throws StorageException on upload failure
     */
    StorageResult putObject(String bucket, String objectKey, InputStream is,
                            long size, String contentType);

    /**
     * Open an input stream to download an object.
     * <p>
     * The caller is responsible for closing the returned stream.
     * </p>
     *
     * @param bucket    the source bucket
     * @param objectKey the object key
     * @return an open {@link InputStream}, never null
     * @throws StorageException if the object cannot be retrieved
     */
    InputStream getObject(String bucket, String objectKey);

    /**
     * Delete an object from storage.
     *
     * @param bucket    the bucket
     * @param objectKey the object key
     * @throws StorageException on deletion failure
     */
    void deleteObject(String bucket, String objectKey);

    /**
     * Check whether an object exists.
     *
     * @param bucket    the bucket
     * @param objectKey the object key
     * @return true if the object exists, false otherwise
     */
    boolean exists(String bucket, String objectKey);

    /**
     * Generate a presigned URL that grants temporary access to the object.
     *
     * @param bucket    the bucket
     * @param objectKey the object key
     * @param expiry    the time until the URL expires
     * @return a presigned URL string
     * @throws StorageException if the URL cannot be generated
     */
    String getPresignedUrl(String bucket, String objectKey, Duration expiry);

    /**
     * Return the permanent public access URL of an object.
     * <p>
     * For private storage this typically resolves to a CDN or custom domain
     * URL. For object stores without public access, this method may return
     * a presigned URL with a long expiry.
     * </p>
     *
     * @param bucket    the bucket
     * @param objectKey the object key
     * @return the access URL, never null
     */
    String getAccessUrl(String bucket, String objectKey);

    /**
     * Return the {@link StorageType} that this provider implements.
     *
     * @return the storage type, never null
     */
    StorageType getType();
}
