package com.xarch.example.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        LambdaQueryWrapper<StorageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageConfig::getStatus, 1)
                .eq(StorageConfig::getDelFlag, 0)
                .orderByDesc(StorageConfig::getIsDefault);
        return storageConfigMapper.selectList(wrapper);
    }

    /**
     * Get global default storage config (regardless of type)
     */
    public StorageConfig getGlobalDefaultConfig() {
        LambdaQueryWrapper<StorageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageConfig::getIsDefault, 1)
                .eq(StorageConfig::getStatus, 1)
                .eq(StorageConfig::getDelFlag, 0)
                .last("LIMIT 1");
        return storageConfigMapper.selectOne(wrapper);
    }

    /**
     * Get default storage config
     */
    public StorageConfig getDefaultConfig(StorageType type) {
        LambdaQueryWrapper<StorageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageConfig::getStorageType, type.getCode())
                .eq(StorageConfig::getIsDefault, 1)
                .eq(StorageConfig::getStatus, 1)
                .eq(StorageConfig::getDelFlag, 0)
                .last("LIMIT 1");
        return storageConfigMapper.selectOne(wrapper);
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
        LambdaQueryWrapper<StorageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageConfig::getStorageType, storageType)
                .eq(StorageConfig::getIsDefault, 1);
        List<StorageConfig> configs = storageConfigMapper.selectList(wrapper);
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