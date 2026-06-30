package com.xarch.starter.storage.service;

import com.xarch.starter.storage.StorageProperties;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageProvider;
import com.xarch.starter.storage.core.StorageProviderFactory;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the high-level {@link FileStorageService}.
 */
class FileStorageServiceTest {

    private RecordingProvider localProvider;
    private RecordingProvider minioProvider;
    private StorageProviderFactory factory;
    private FileStorageService service;
    private StorageProperties properties;

    @BeforeEach
    void setUp() {
        localProvider = new RecordingProvider(StorageType.LOCAL);
        minioProvider = new RecordingProvider(StorageType.MINIO);
        properties = new StorageProperties();
        properties.setDefaultType("local");
        factory = new StorageProviderFactory(List.of(localProvider, minioProvider), properties);
        service = new FileStorageService(factory, properties);
    }

    @Test
    void uploadPicksDefaultProvider() {
        MultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hi".getBytes(StandardCharsets.UTF_8));
        StorageResult result = service.upload(file);
        assertThat(result.bucket()).isEqualTo("xarch");
        assertThat(result.objectKey()).endsWith(".txt");
        assertThat(result.size()).isEqualTo(2L);
        assertThat(localProvider.putCount).isEqualTo(1);
        assertThat(minioProvider.putCount).isZero();
    }

    @Test
    void uploadHonoursStorageTypeOverride() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf",
                "%PDF-1.4".getBytes(StandardCharsets.UTF_8));
        StorageResult result = service.upload(file,
                UploadOptions.builder().storageType(StorageType.MINIO).build());
        assertThat(result.bucket()).isEqualTo("xarch");
        assertThat(minioProvider.putCount).isEqualTo(1);
        assertThat(localProvider.putCount).isZero();
    }

    @Test
    void uploadAppliesObjectKeyPrefix() {
        MultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});
        service.upload(file, UploadOptions.builder().objectKeyPrefix("avatars/2026").build());
        assertThat(localProvider.lastKey).startsWith("avatars/2026/");
        assertThat(localProvider.lastKey).endsWith(".png");
    }

    @Test
    void uploadFromBytes() {
        StorageResult result = service.upload("payload".getBytes(StandardCharsets.UTF_8),
                "note.txt", "text/plain");
        assertThat(result.size()).isEqualTo(7L);
        assertThat(localProvider.lastBucket).isEqualTo("xarch");
    }

    @Test
    void uploadRejectsEmptyMultipart() {
        MultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThatThrownBy(() -> service.upload(empty)).isInstanceOf(StorageException.class);
    }

    @Test
    void downloadDelegatesToProvider() {
        localProvider.stream = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        try (InputStream in = service.download(StorageType.LOCAL, "xarch", "k")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("data");
        }
    }

    @Test
    void deleteAndPresignedUrlDelegate() {
        service.delete(StorageType.LOCAL, "xarch", "k");
        assertThat(localProvider.deleteCount).isEqualTo(1);

        String url = service.presignedUrl(StorageType.LOCAL, "xarch", "k", Duration.ofMinutes(1));
        assertThat(url).isEqualTo("presigned/xarch/k");

        String access = service.accessUrl(StorageType.LOCAL, "xarch", "k");
        assertThat(access).isEqualTo("public/xarch/k");
    }

    @Test
    void explicitBucketTakesPrecedenceOverDefault() {
        service.upload("x".getBytes(StandardCharsets.UTF_8), "k", null,
                UploadOptions.builder().bucket("custom-bucket").build());
        assertThat(localProvider.lastBucket).isEqualTo("custom-bucket");
    }

    @Test
    void disabledUniqueKeyUsesOriginalFilename() {
        service.upload("x".getBytes(StandardCharsets.UTF_8), "report.docx", null,
                UploadOptions.builder().generateUniqueKey(false).build());
        assertThat(localProvider.lastKey).isEqualTo("report.docx");
    }

    @Test
    void storageTypeFromConfigIsUsed() {
        properties.setDefaultType("minio");
        MultipartFile file = new MockMultipartFile("file", "a.bin", null, new byte[]{1});
        service.upload(file);
        assertThat(minioProvider.putCount).isEqualTo(1);
        assertThat(localProvider.putCount).isZero();
    }

    /** Test-only recording provider. */
    static class RecordingProvider implements StorageProvider {
        final StorageType type;
        int putCount;
        int deleteCount;
        String lastBucket;
        String lastKey;
        long lastSize;
        InputStream stream;

        RecordingProvider(StorageType type) {
            this.type = type;
        }

        @Override
        public StorageResult putObject(String bucket, String objectKey, InputStream is,
                                       long size, String contentType) {
            putCount++;
            lastBucket = bucket;
            lastKey = objectKey;
            lastSize = size;
            return StorageResult.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .accessUrl("public/" + bucket + "/" + objectKey)
                    .size(size)
                    .contentType(contentType)
                    .build();
        }

        @Override
        public InputStream getObject(String bucket, String objectKey) {
            return stream != null ? stream : new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void deleteObject(String bucket, String objectKey) {
            deleteCount++;
        }

        @Override
        public boolean exists(String bucket, String objectKey) {
            return false;
        }

        @Override
        public String getPresignedUrl(String bucket, String objectKey, Duration expiry) {
            return "presigned/" + bucket + "/" + objectKey;
        }

        @Override
        public String getAccessUrl(String bucket, String objectKey) {
            return "public/" + bucket + "/" + objectKey;
        }

        @Override
        public StorageType getType() {
            return type;
        }
    }
}
