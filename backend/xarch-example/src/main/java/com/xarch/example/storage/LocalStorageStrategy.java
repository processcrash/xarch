package com.xarch.example.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local storage strategy
 * Stores files on local filesystem
 */
@Component
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${xarch.storage.local.path:/tmp/xarch-files}")
    private String basePath;

    @Override
    public String upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            Path path = Paths.get(basePath, objectKey);
            Files.createDirectories(path.getParent());

            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
                byte[] buffer = new byte[8192];
                long remaining = contentLength;
                int read;
                while (remaining > 0 && (read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    outputStream.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            return "/files/" + objectKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public boolean download(String objectKey, OutputStream outputStream) {
        try {
            Path path = Paths.get(basePath, objectKey);
            if (!Files.exists(path)) {
                return false;
            }
            Files.copy(path, outputStream);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public boolean delete(String objectKey) {
        try {
            Path path = Paths.get(basePath, objectKey);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        Path path = Paths.get(basePath, objectKey);
        return Files.exists(path);
    }

    @Override
    public String getAccessUrl(String objectKey) {
        return "/files/" + objectKey;
    }

    @Override
    public String getObjectKeyFromUrl(String url) {
        if (url != null && url.startsWith("/files/")) {
            return url.substring(7);
        }
        return url;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }
}