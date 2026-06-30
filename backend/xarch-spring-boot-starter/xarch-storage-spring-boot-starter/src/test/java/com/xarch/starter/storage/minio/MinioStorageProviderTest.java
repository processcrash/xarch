package com.xarch.starter.storage.minio;

import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link MinioStorageProvider}.
 * <p>
 * These tests focus on the configuration validation, type identification and
 * access-URL composition. The actual SDK interactions are exercised against a
 * real MinIO instance via testcontainers; the wire-level behaviour is
 * out-of-scope for the unit-test layer.
 * </p>
 */
class MinioStorageProviderTest {

    private MinioProperties validProperties() {
        MinioProperties p = new MinioProperties();
        p.setEndpoint("http://localhost:9000");
        p.setAccessKey("ak");
        p.setSecretKey("sk");
        p.setDefaultBucket("xarch");
        p.setAutoCreateBucket(true);
        return p;
    }

    @Test
    void typeIsMinio() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThat(provider.getType()).isEqualTo(StorageType.MINIO);
    }

    @Test
    void missingCredentialsThrow() {
        MinioProperties bad = new MinioProperties();
        bad.setAccessKey(null);
        bad.setSecretKey(null);
        assertThatThrownBy(() -> new MinioStorageProvider(bad)).isInstanceOf(StorageException.class);

        bad.setAccessKey("ak");
        bad.setSecretKey(null);
        assertThatThrownBy(() -> new MinioStorageProvider(bad)).isInstanceOf(StorageException.class);
    }

    @Test
    void accessUrlUsesCnameWhenConfigured() {
        MinioProperties p = validProperties();
        p.setCname("https://cdn.example.com/");
        MinioStorageProvider provider = new MinioStorageProvider(p);
        assertThat(provider.getAccessUrl("xarch", "k")).isEqualTo("https://cdn.example.com/xarch/k");
    }

    @Test
    void accessUrlFallsBackToEndpoint() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThat(provider.getAccessUrl("xarch", "k"))
                .isEqualTo("http://localhost:9000/xarch/k");
    }

    @Test
    void accessUrlTrimsTrailingSlashOnCname() {
        MinioProperties p = validProperties();
        p.setCname("https://cdn.example.com");
        MinioStorageProvider provider = new MinioStorageProvider(p);
        assertThat(provider.getAccessUrl("xarch", "k")).isEqualTo("https://cdn.example.com/xarch/k");
    }

    @Test
    void blankArgumentsRejectedForPresign() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.getPresignedUrl(null, "k", Duration.ofMinutes(1)))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.getPresignedUrl("b", "", Duration.ofMinutes(1)))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void blankArgumentsRejectedForDelete() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.deleteObject(null, "k"))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.deleteObject("b", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void blankArgumentsRejectedForGet() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.getObject(null, "k"))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.getObject("b", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void blankArgumentsRejectedForPut() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.putObject(null, "k",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.putObject("b", "",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void existsReturnsFalseWhenExceptionThrown() {
        // We can only assert the contract indirectly here; the SDK throws
        // because the endpoint is unreachable. The provider should swallow
        // the failure and return false rather than propagate.
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        assertThat(provider.exists("xarch", "missing")).isFalse();
    }

    @Test
    void putObjectWrapsExceptionAsStorageException() {
        MinioStorageProvider provider = new MinioStorageProvider(validProperties());
        // Use an invalid endpoint to force an SDK failure.
        assertThatThrownBy(() -> provider.putObject("xarch", "k",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
    }
}
