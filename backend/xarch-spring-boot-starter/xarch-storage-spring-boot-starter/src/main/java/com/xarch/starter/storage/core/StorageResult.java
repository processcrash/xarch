package com.xarch.starter.storage.core;

/**
 * Result returned by a successful storage operation.
 *
 * @param bucket      the storage bucket the object lives in
 * @param objectKey   the unique key identifying the object within the bucket
 * @param accessUrl   a publicly accessible URL (may be null for private objects)
 * @param etag        the entity tag returned by the storage backend, may be null
 * @param size        the stored object size in bytes
 * @param contentType the MIME type of the stored object, may be null
 */
public record StorageResult(
        String bucket,
        String objectKey,
        String accessUrl,
        String etag,
        long size,
        String contentType
) {

    /**
     * Create a builder for {@link StorageResult}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Mutable builder for {@link StorageResult}.
     */
    public static final class Builder {
        private String bucket;
        private String objectKey;
        private String accessUrl;
        private String etag;
        private long size;
        private String contentType;

        private Builder() {
        }

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder objectKey(String objectKey) {
            this.objectKey = objectKey;
            return this;
        }

        public Builder accessUrl(String accessUrl) {
            this.accessUrl = accessUrl;
            return this;
        }

        public Builder etag(String etag) {
            this.etag = etag;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Build the immutable {@link StorageResult}.
         *
         * @return a new {@link StorageResult} with the configured values
         */
        public StorageResult build() {
            return new StorageResult(bucket, objectKey, accessUrl, etag, size, contentType);
        }
    }
}
