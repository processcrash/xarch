package com.xarch.starter.storage.local;

import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link LocalStorageProvider}.
 */
class LocalStorageProviderTest {

    @Test
    void putAndGetRoundTrips(@TempDir Path tempDir) throws IOException {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        byte[] data = "hello xarch".getBytes(StandardCharsets.UTF_8);

        StorageResult result = provider.putObject("xarch", "docs/a.txt",
                new ByteArrayInputStream(data), data.length, "text/plain");

        assertThat(result.bucket()).isEqualTo("xarch");
        assertThat(result.objectKey()).isEqualTo("docs/a.txt");
        assertThat(result.size()).isEqualTo(data.length);
        assertThat(result.etag()).hasSize(32);
        assertThat(result.accessUrl()).isEqualTo("/files/xarch/docs/a.txt");

        try (InputStream in = provider.getObject("xarch", "docs/a.txt")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello xarch");
        }
    }

    @Test
    void existsReturnsTrueForStoredObject(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        provider.putObject("xarch", "a/b.bin",
                new ByteArrayInputStream(new byte[]{1, 2, 3}), 3, "application/octet-stream");
        assertThat(provider.exists("xarch", "a/b.bin")).isTrue();
        assertThat(provider.exists("xarch", "missing")).isFalse();
    }

    @Test
    void deleteRemovesObject(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        provider.putObject("xarch", "k.txt",
                new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), 3, "text/plain");
        assertThat(provider.exists("xarch", "k.txt")).isTrue();
        provider.deleteObject("xarch", "k.txt");
        assertThat(provider.exists("xarch", "k.txt")).isFalse();
    }

    @Test
    void accessUrlRespectsPublicBaseUrl(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "https://cdn/");
        assertThat(provider.getAccessUrl("b", "k")).isEqualTo("https://cdn/b/k");
    }

    @Test
    void presignedUrlFallsBackToPublicAccessUrl(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        assertThat(provider.getPresignedUrl("b", "k", Duration.ofMinutes(5)))
                .isEqualTo("/files/b/k");
    }

    @Test
    void rejectsPathTraversal(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        assertThatThrownBy(() -> provider.putObject("xarch", "../escape.txt",
                new ByteArrayInputStream(new byte[0]), 0, "text/plain"))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void getObjectFailsForMissingKey(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        assertThatThrownBy(() -> provider.getObject("xarch", "missing"))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void rejectsBlankArguments(@TempDir Path tempDir) {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        assertThatThrownBy(() -> provider.putObject(null, "k", new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> provider.putObject("b", "", new ByteArrayInputStream(new byte[0]), 0, null))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void getTypeReturnsLocal() {
        LocalStorageProvider provider = new LocalStorageProvider("./tmp", "/files");
        assertThat(provider.getType()).isEqualTo(StorageType.LOCAL);
    }

    @Test
    void sizeOfReturnsFileSize(@TempDir Path tempDir) throws IOException {
        LocalStorageProvider provider = new LocalStorageProvider(tempDir.toString(), "/files");
        provider.putObject("xarch", "x.bin", new ByteArrayInputStream(new byte[10]), 10, null);
        assertThat(provider.sizeOf("xarch", "x.bin")).isEqualTo(10L);
        assertThat(provider.sizeOf("xarch", "missing")).isEqualTo(-1L);
        // Sanity: the file was created on disk
        assertThat(Files.exists(tempDir.resolve("xarch").resolve("x.bin"))).isTrue();
    }
}
