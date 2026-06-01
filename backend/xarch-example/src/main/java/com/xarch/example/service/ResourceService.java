package com.xarch.example.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Resource;
import com.xarch.example.entity.StorageConfig;
import com.xarch.example.mapper.ResourceMapper;
import com.xarch.example.storage.StorageConfigService;
import com.xarch.example.storage.StorageFactory;
import com.xarch.example.storage.StorageStrategy;
import com.xarch.example.storage.StorageType;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resource service - unified file management with storage abstraction
 */
@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private StorageFactory storageFactory;

    @Autowired
    private StorageConfigService storageConfigService;

    /**
     * Page query resources
     */
    public PageResult<Resource> page(String sceneCode, String storageType, String keyword, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_resource")
                .where("del_flag = 0");

        if (sceneCode != null && !sceneCode.isEmpty()) {
            wrapper.and("scene_code = ?", sceneCode);
        }
        if (storageType != null && !storageType.isEmpty()) {
            wrapper.and("storage_type = ?", storageType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and("(resource_name LIKE ? OR object_key LIKE ?)", "%" + keyword + "%", "%" + keyword + "%");
        }
        wrapper.orderBy("create_time", false);

        com.mybatisflex.core.paginate.Page<Resource> page = resourceMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    /**
     * Get resource by ID
     */
    public Resource getById(Long id) {
        return resourceMapper.selectById(id);
    }

    /**
     * List all resources
     */
    public List<Resource> list() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_resource")
                .where("del_flag = 0")
                .orderBy("create_time", false);
        return resourceMapper.selectListByQuery(wrapper);
    }

    /**
     * Upload file with automatic storage strategy selection
     */
    public Resource upload(String sceneCode, String bizKey, String storageTypeCode, MultipartFile file,
                           Long createUserId, String createUserName) throws IOException {
        StorageType storageType = StorageType.fromCode(storageTypeCode);
        StorageConfig config = storageConfigService.getDefaultConfig(storageType);
        StorageStrategy strategy;
        String actualStorageType;

        if (config != null) {
            strategy = storageFactory.getStrategy(storageType);
            actualStorageType = storageTypeCode;
        } else {
            strategy = storageFactory.getStrategy(StorageType.LOCAL);
            actualStorageType = StorageType.LOCAL.getCode();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = generateObjectKey(sceneCode, bizKey, extension);

        InputStream inputStream = file.getInputStream();
        long contentLength = file.getSize();
        String contentType = file.getContentType();

        String accessUrl = strategy.upload(objectKey, inputStream, contentLength, contentType);

        Resource resource = new Resource();
        resource.setResourceName(originalFilename);
        resource.setObjectKey(objectKey);
        resource.setAccessUrl(accessUrl);
        resource.setSceneCode(sceneCode);
        resource.setBizKey(bizKey);
        resource.setFileSize(file.getSize());
        resource.setFileType(contentType);
        resource.setStorageType(actualStorageType);
        resource.setCreateUserId(createUserId);
        resource.setCreateUserName(createUserName);
        resource.setCreateTime(LocalDateTime.now());
        resource.setDelFlag(0);

        resourceMapper.insert(resource);

        return resource;
    }

    /**
     * Download file to output stream
     */
    public boolean download(Long id, OutputStream outputStream) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getDelFlag() == 1) {
            return false;
        }

        StorageType storageType = StorageType.fromCode(resource.getStorageType());
        StorageStrategy strategy = storageFactory.getStrategy(storageType);

        return strategy.download(resource.getObjectKey(), outputStream);
    }

    /**
     * Delete file (soft delete)
     */
    public void delete(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            resource.setDelFlag(1);
            resource.setUpdateTime(LocalDateTime.now());
            resourceMapper.updateById(resource);

            StorageType storageType = StorageType.fromCode(resource.getStorageType());
            StorageStrategy strategy = storageFactory.getStrategy(storageType);
            try {
                strategy.delete(resource.getObjectKey());
            } catch (Exception e) {
                // Log but don't fail
            }
        }
    }

    /**
     * Permanently delete file (hard delete from storage only)
     */
    public void hardDelete(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            StorageType storageType = StorageType.fromCode(resource.getStorageType());
            StorageStrategy strategy = storageFactory.getStrategy(storageType);
            try {
                strategy.delete(resource.getObjectKey());
            } catch (Exception e) {
                // Log but don't fail
            }
            resourceMapper.deleteById(id);
        }
    }

    /**
     * Get file input stream for preview
     */
    public InputStream getFileStream(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getDelFlag() == 1) {
            return null;
        }

        StorageType storageType = StorageType.fromCode(resource.getStorageType());
        StorageStrategy strategy = storageFactory.getStrategy(storageType);

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        boolean success = strategy.download(resource.getObjectKey(), outputStream);

        if (success) {
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
        return null;
    }

    /**
     * Get storage statistics
     */
    public StorageStats getStats() {
        StorageStats stats = new StorageStats();

        QueryWrapper localWrapper = QueryWrapper.create().from("sys_resource")
                .where("del_flag = 0 AND storage_type = ?", StorageType.LOCAL.getCode());
        QueryWrapper minioWrapper = QueryWrapper.create().from("sys_resource")
                .where("del_flag = 0 AND storage_type = ?", StorageType.MINIO.getCode());
        QueryWrapper ossWrapper = QueryWrapper.create().from("sys_resource")
                .where("del_flag = 0 AND storage_type = ?", StorageType.ALIYUN_OSS.getCode());

        stats.setLocalCount(resourceMapper.selectCountByQuery(localWrapper));
        stats.setMinioCount(resourceMapper.selectCountByQuery(minioWrapper));
        stats.setOssCount(resourceMapper.selectCountByQuery(ossWrapper));

        QueryWrapper allWrapper = QueryWrapper.create().from("sys_resource").where("del_flag = 0");
        List<Resource> resources = resourceMapper.selectListByQuery(allWrapper);
        long totalSize = resources.stream().mapToLong(r -> r.getFileSize() != null ? r.getFileSize() : 0).sum();
        stats.setTotalSize(totalSize);

        return stats;
    }

    private String generateObjectKey(String sceneCode, String bizKey, String extension) {
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return (sceneCode != null ? sceneCode + "/" : "") + datePath + "/" + uuid + extension;
    }

    /**
     * Storage statistics
     */
    public static class StorageStats {
        private Long localCount;
        private Long minioCount;
        private Long ossCount;
        private Long totalSize;

        public Long getLocalCount() { return localCount; }
        public void setLocalCount(Long localCount) { this.localCount = localCount; }
        public Long getMinioCount() { return minioCount; }
        public void setMinioCount(Long minioCount) { this.minioCount = minioCount; }
        public Long getOssCount() { return ossCount; }
        public void setOssCount(Long ossCount) { this.ossCount = ossCount; }
        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
    }
}