package com.xarch.example.file.service;

import com.xarch.example.file.entity.Resource;
import com.xarch.starter.core.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.List;

/** Resource service contract. */
public interface ResourceService {
    PageResult<Resource> page(String sceneCode, String storageType, String keyword, int pageNum, int pageSize);
    Resource getById(Long id);
    Resource upload(String sceneCode, String bizKey, String storageType, MultipartFile file, Long userId, String userName) throws IOException;
    boolean download(Long id, OutputStream outputStream);
    InputStream getFileStream(Long id);
    void delete(Long id);
    List<Resource> list();

    /** Aggregate storage stats for the dashboard. */
    final class StorageStats {
        public long total;
        public long totalSize;
    }
    StorageStats getStats();
}