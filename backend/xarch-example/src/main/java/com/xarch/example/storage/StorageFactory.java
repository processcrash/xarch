package com.xarch.example.storage;

import com.xarch.example.entity.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage factory
 * Creates and manages storage strategy instances
 */
@Component
public class StorageFactory {

    private final Map<StorageType, StorageStrategy> strategies = new HashMap<>();

    @Autowired
    public void setStrategies(
            LocalStorageStrategy localStrategy,
            MinioStorageStrategy minioStrategy,
            AliyunOssStorageStrategy aliyunOssStrategy
    ) {
        strategies.put(StorageType.LOCAL, localStrategy);
        strategies.put(StorageType.MINIO, minioStrategy);
        strategies.put(StorageType.ALIYUN_OSS, aliyunOssStrategy);
    }

    /**
     * Get storage strategy by type
     */
    public StorageStrategy getStrategy(StorageType type) {
        StorageStrategy strategy = strategies.get(type);
        if (strategy == null) {
            // Fallback to local if not found
            return strategies.get(StorageType.LOCAL);
        }
        return strategy;
    }

    /**
     * Get storage strategy from config
     */
    public StorageStrategy getStrategy(StorageConfig config) {
        if (config == null) {
            return strategies.get(StorageType.LOCAL);
        }
        return getStrategy(StorageType.fromCode(config.getStorageType()));
    }

    /**
     * Get default storage strategy
     */
    public StorageStrategy getDefaultStrategy() {
        // TODO: Get from StorageConfigService to find default config
        // For now, return local
        return strategies.get(StorageType.LOCAL);
    }
}