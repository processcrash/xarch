package com.xarch.starter.storage.aliyun;

import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AliyunOssStorageProvider}.
 * <p>
 * The OSS client itself is created with {@code new OSSClientBuilder().build(...)}
 * in the provider constructor, so we focus these tests on configuration
 * validation, URL composition, and argument validation rather than mocking
 * the underlying SDK.
 * </p>
 */
class AliyunOssStorageProviderTest {

    private AliyunOssProperties validProperties() {
        AliyunOssProperties p = new AliyunOssProperties();
        p.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        p.setAccessKeyId("ak");
        p.setAccessKeySecret("sk");
        p.setBucketName("xarch");
        p.setAutoCreateBucket(true);
        return p;
    }

    @Test
    void typeIsAliyunOss() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThat(provider.getType()).isEqualTo(StorageType.ALIYUN_OSS);
    }

    @Test
    void missingCredentialsThrow() {
        AliyunOssProperties bad = new AliyunOssProperties();
        assertThatThrownBy(() -> new AliyunOssStorageProvider(bad)).isInstanceOf(StorageException.class);

        bad.setAccessKeyId("ak");
        bad.setAccessKeySecret(null);
        assertThatThrownBy(() -> new AliyunOssStorageProvider(bad)).isInstanceOf(StorageException.class);
    }

    @Test
    void accessUrlBuildsBucketSubdomainUrl() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThat(provider.getAccessUrl("xarch", "k"))
                .isEqualTo("https://xarch.oss-cn-hangzhou.aliyuncs.com/k");
    }

    @Test
    void accessUrlUsesCnameWhenConfigured() {
        AliyunOssProperties p = validProperties();
        p.setCname("https://cdn.example.com/");
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(p);
        assertThat(provider.getAccessUrl("xarch", "k")).isEqualTo("https://cdn.example.com/k");
    }

    @Test
    void accessUrlHandlesHttpEndpoint() {
        AliyunOssProperties p = validProperties();
        p.setEndpoint("http://oss-cn-hangzhou.aliyuncs.com");
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(p);
        assertThat(provider.getAccessUrl("xarch", "k"))
                .isEqualTo("https://xarch.oss-cn-hangzhou.aliyuncs.com/k");
    }

    @Test
    void accessUrlTrimsTrailingSlash() {
        AliyunOssProperties p = validProperties();
        p.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com/");
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(p);
        assertThat(provider.getAccessUrl("xarch", "k"))
                .isEqualTo("https://xarch.oss-cn-hangzhou.aliyuncs.com/k");
    }

    @Test
    void blankArgumentsRejectedForDelete() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.deleteObject(null, "k"))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.deleteObject("b", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void blankArgumentsRejectedForGet() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.getObject(null, "k"))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.getObject("b", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void blankArgumentsRejectedForPut() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.putObject(null, "k",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.putObject("b", "",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void existsReturnsFalseOnException() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThat(provider.exists("xarch", "missing")).isFalse();
    }

    @Test
    void putObjectWrapsExceptionAsStorageException() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        assertThatThrownBy(() -> provider.putObject("xarch", "k",
                new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void durationNullOnPresignedThrows() {
        AliyunOssStorageProvider provider = new AliyunOssStorageProvider(validProperties());
        // Null duration is delegated to Date, which will NPE internally and
        // be wrapped as a StorageException.
        assertThatThrownBy(() -> provider.getPresignedUrl("xarch", "k", Duration.ofMillis(0)))
                .isInstanceOf(StorageException.class);
    }
}
