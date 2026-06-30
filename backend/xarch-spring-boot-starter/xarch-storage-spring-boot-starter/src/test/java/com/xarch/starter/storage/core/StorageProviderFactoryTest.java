package com.xarch.starter.storage.core;

import com.xarch.starter.storage.StorageProperties;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link StorageProviderFactory}.
 */
class StorageProviderFactoryTest {

    @Test
    void returnsExactProviderForKnownType() {
        StorageProvider local = stubProvider(StorageType.LOCAL);
        StorageProvider minio = stubProvider(StorageType.MINIO);

        StorageProperties props = new StorageProperties();
        StorageProviderFactory factory = new StorageProviderFactory(List.of(local, minio), props);

        assertThat(factory.getProvider(StorageType.LOCAL)).isSameAs(local);
        assertThat(factory.getProvider(StorageType.MINIO)).isSameAs(minio);
    }

    @Test
    void fallsBackToDefaultWhenTypeMissing() {
        StorageProvider local = stubProvider(StorageType.LOCAL);
        StorageProperties props = new StorageProperties();
        props.setDefaultType("local");

        StorageProviderFactory factory = new StorageProviderFactory(List.of(local), props);

        assertThat(factory.getProvider(StorageType.ALIYUN_OSS)).isSameAs(local);
        assertThat(factory.getDefaultProvider()).isSameAs(local);
    }

    @Test
    void throwsWhenNoProviderAvailable() {
        StorageProperties props = new StorageProperties();
        props.setDefaultType("minio");
        StorageProviderFactory factory = new StorageProviderFactory(List.of(), props);

        assertThatThrownBy(() -> factory.getProvider(StorageType.S3))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void resolvesTypeFromString() {
        StorageProvider local = stubProvider(StorageType.LOCAL);
        StorageProviderFactory factory = new StorageProviderFactory(List.of(local), new StorageProperties());
        assertThat(factory.getProvider("LOCAL")).isSameAs(local);
        assertThat(factory.getProvider(null)).isSameAs(local);
    }

    @Test
    void hasProviderReportsAvailability() {
        StorageProvider local = stubProvider(StorageType.LOCAL);
        StorageProviderFactory factory = new StorageProviderFactory(List.of(local), new StorageProperties());
        assertThat(factory.hasProvider(StorageType.LOCAL)).isTrue();
        assertThat(factory.hasProvider(StorageType.S3)).isFalse();
    }

    private static StorageProvider stubProvider(StorageType type) {
        return new StorageProvider() {
            @Override
            public StorageResult putObject(String bucket, String objectKey, InputStream is, long size, String contentType) {
                return StorageResult.builder().bucket(bucket).objectKey(objectKey).build();
            }

            @Override
            public InputStream getObject(String bucket, String objectKey) {
                return InputStream.nullInputStream();
            }

            @Override
            public void deleteObject(String bucket, String objectKey) {
            }

            @Override
            public boolean exists(String bucket, String objectKey) {
                return true;
            }

            @Override
            public String getPresignedUrl(String bucket, String objectKey, Duration expiry) {
                return "https://example/" + objectKey;
            }

            @Override
            public String getAccessUrl(String bucket, String objectKey) {
                return "https://example/" + objectKey;
            }

            @Override
            public StorageType getType() {
                return type;
            }
        };
    }
}
