package com.xarch.example.storage;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.StorageConfig;
import com.xarch.example.mapper.StorageConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Storage configuration service
 */
@Service
public class StorageConfigService {

    @Autowired
    private StorageConfigMapper storageConfigMapper;

    /**
     * Get all enabled storage configs
     */
    public List<StorageConfig> listEnabled() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_storage_config")
                .where("status = 1 AND del_flag = 0")
                .orderBy("is_default", false);
        return storageConfigMapper.selectListByQuery(wrapper);
    }

    /**
     * Get global default storage config (regardless of type)
     */
    public StorageConfig getGlobalDefaultConfig() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_storage_config")
                .where("is_default = 1 AND status = 1 AND del_flag = 0")
                .limit(1);
        return storageConfigMapper.selectOneByQuery(wrapper);
    }

    /**
     * Get default storage config
     */
    public StorageConfig getDefaultConfig(StorageType type) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_storage_config")
                .where("storage_type = ? AND is_default = 1 AND status = 1 AND del_flag = 0", type.getCode())
                .limit(1);
        return storageConfigMapper.selectOneByQuery(wrapper);
    }

    /**
     * Get config by ID
     */
    public StorageConfig getById(Long id) {
        return storageConfigMapper.selectById(id);
    }

    /**
     * Create new storage config
     */
    public void create(StorageConfig config) {
        // If set as default, clear other defaults
        if (config.getIsDefault() != null && config.getIsDefault() == 1) {
            clearDefault(config.getStorageType());
        }
        storageConfigMapper.insert(config);
    }

    /**
     * Update storage config
     */
    public void update(StorageConfig config) {
        // If set as default, clear other defaults
        if (config.getIsDefault() != null && config.getIsDefault() == 1) {
            clearDefault(config.getStorageType());
        }
        storageConfigMapper.updateById(config);
    }

    /**
     * Delete storage config (soft delete)
     */
    public void delete(Long id) {
        StorageConfig config = storageConfigMapper.selectById(id);
        if (config != null) {
            config.setDelFlag(1);
            storageConfigMapper.updateById(config);
        }
    }

    /**
     * Test storage connection
     */
    public boolean testConnection(StorageConfig config) {
        try {
            StorageStrategy strategy = getStrategyForConfig(config);
            return strategy != null;
        } catch (Exception e) {
            return false;
        }
    }

    private StorageStrategy getStrategyForConfig(StorageConfig config) {
        StorageType type = StorageType.fromCode(config.getStorageType());
        switch (type) {
            case MINIO:
                return new MinioStorageStrategyTest(config);
            case ALIYUN_OSS:
                return new AliyunOssStorageStrategyTest(config);
            default:
                return new LocalStorageStrategyTest(config);
        }
    }

    private void clearDefault(String storageType) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_storage_config")
                .where("storage_type = ? AND is_default = 1", storageType);
        List<StorageConfig> configs = storageConfigMapper.selectListByQuery(wrapper);
        for (StorageConfig config : configs) {
            config.setIsDefault(0);
            storageConfigMapper.updateById(config);
        }
    }

    // Test implementations that use provided config directly
    private static class LocalStorageStrategyTest implements StorageStrategy {
        private final StorageConfig config;

        LocalStorageStrategyTest(StorageConfig config) {
            this.config = config;
        }

        @Override
        public String upload(String objectKey, java.io.InputStream inputStream, long contentLength, String contentType) {
            return "/files/" + objectKey;
        }

        @Override
        public boolean download(String objectKey, java.io.OutputStream outputStream) {
            return true;
        }

        @Override
        public boolean delete(String objectKey) {
            return true;
        }

        @Override
        public boolean exists(String objectKey) {
            return true;
        }

        @Override
        public String getAccessUrl(String objectKey) {
            return "/files/" + objectKey;
        }

        @Override
        public String getObjectKeyFromUrl(String url) {
            return url;
        }

        @Override
        public StorageType getStorageType() {
            return StorageType.LOCAL;
        }
    }

    private static class MinioStorageStrategyTest implements StorageStrategy {
        private final StorageConfig config;

        MinioStorageStrategyTest(StorageConfig config) {
            this.config = config;
        }

        @Override
        public String upload(String objectKey, java.io.InputStream inputStream, long contentLength, String contentType) {
            return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
        }

        @Override
        public boolean download(String objectKey, java.io.OutputStream outputStream) {
            return true;
        }

        @Override
        public boolean delete(String objectKey) {
            return true;
        }

        @Override
        public boolean exists(String objectKey) {
            return true;
        }

        @Override
        public String getAccessUrl(String objectKey) {
            return config.getEndpoint() + "/" + config.getBucketName() + "/" + objectKey;
        }

        @Override
        public String getObjectKeyFromUrl(String url) {
            return url;
        }

        @Override
        public StorageType getStorageType() {
            return StorageType.MINIO;
        }
    }

    private static class AliyunOssStorageStrategyTest implements StorageStrategy {
        private final StorageConfig config;

        AliyunOssStorageStrategyTest(StorageConfig config) {
            this.config = config;
        }

        @Override
        public String upload(String objectKey, java.io.InputStream inputStream, long contentLength, String contentType) {
            return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + objectKey;
        }

        @Override
        public boolean download(String objectKey, java.io.OutputStream outputStream) {
            return true;
        }

        @Override
        public boolean delete(String objectKey) {
            return true;
        }

        @Override
        public boolean exists(String objectKey) {
            return true;
        }

        @Override
        public String getAccessUrl(String objectKey) {
            return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + objectKey;
        }

        @Override
        public String getObjectKeyFromUrl(String url) {
            return url;
        }

        @Override
        public StorageType getStorageType() {
            return StorageType.ALIYUN_OSS;
        }
    }
}