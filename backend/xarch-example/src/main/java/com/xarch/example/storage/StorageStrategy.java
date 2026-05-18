package com.xarch.example.storage;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Storage strategy interface
 * Implement this to add new storage backends
 */
public interface StorageStrategy {

    /**
     * Upload file
     * @param objectKey unique key for the file
     * @param inputStream file content
     * @param contentLength file size
     * @param contentType MIME type
     * @return access URL for the uploaded file
     */
    String upload(String objectKey, InputStream inputStream, long contentLength, String contentType);

    /**
     * Download file to output stream
     * @param objectKey unique key for the file
     * @param outputStream stream to write content
     * @return true if successful
     */
    boolean download(String objectKey, OutputStream outputStream);

    /**
     * Delete file
     * @param objectKey unique key for the file
     * @return true if successful
     */
    boolean delete(String objectKey);

    /**
     * Check if file exists
     * @param objectKey unique key for the file
     * @return true if exists
     */
    boolean exists(String objectKey);

    /**
     * Get file access URL
     * @param objectKey unique key for the file
     * @return URL string
     */
    String getAccessUrl(String objectKey);

    /**
     * Get object key from URL
     * @param url file URL
     * @return object key
     */
    String getObjectKeyFromUrl(String url);

    /**
     * Get storage type
     */
    StorageType getStorageType();
}