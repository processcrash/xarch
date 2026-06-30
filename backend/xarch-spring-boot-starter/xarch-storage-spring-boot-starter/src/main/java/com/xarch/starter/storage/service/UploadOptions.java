package com.xarch.starter.storage.service;

import com.xarch.starter.storage.core.StorageType;

import java.util.Map;

/**
 * Options controlling a single upload request.
 *
 * @param bucket              target bucket; when null the default bucket is used
 * @param objectKeyPrefix     prefix prepended to the generated object key
 * @param storageType         storage backend to use; null means default
 * @param generateUniqueKey   whether to generate a unique key (UUID-prefixed)
 * @param contentType         explicit content type override; null = auto-detect
 * @param metadata            optional user metadata stored alongside the object
 * @param originalFilename    original filename used to derive a sensible key suffix
 */
public record UploadOptions(
        String bucket,
        String objectKeyPrefix,
        StorageType storageType,
        boolean generateUniqueKey,
        String contentType,
        Map<String, String> metadata,
        String originalFilename
) {

    /**
     * Create a builder for {@link UploadOptions}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Build default options: use the default backend, no prefix, generate a
     * unique key.
     *
     * @return default options
     */
    public static UploadOptions defaults() {
        return builder().build();
    }

    /**
     * Mutable builder for {@link UploadOptions}.
     */
    public static final class Builder {
        private String bucket;
        private String objectKeyPrefix;
        private StorageType storageType;
        private boolean generateUniqueKey = true;
        private String contentType;
        private Map<String, String> metadata = Map.of();
        private String originalFilename;

        private Builder() {
        }

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder objectKeyPrefix(String objectKeyPrefix) {
            this.objectKeyPrefix = objectKeyPrefix;
            return this;
        }

        public Builder storageType(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        public Builder generateUniqueKey(boolean generateUniqueKey) {
            this.generateUniqueKey = generateUniqueKey;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? Map.of() : metadata;
            return this;
        }

        public Builder originalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
            return this;
        }

        /**
         * Build the immutable {@link UploadOptions}.
         *
         * @return a new options instance
         */
        public UploadOptions build() {
            return new UploadOptions(bucket, objectKeyPrefix, storageType, generateUniqueKey,
                    contentType, metadata, originalFilename);
        }
    }
}
