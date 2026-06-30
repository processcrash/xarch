package com.xarch.starter.storage.aliyun;

import com.aliyun.oss.OSS;
import com.xarch.starter.storage.core.StorageProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Aliyun OSS storage provider.
 */
@AutoConfiguration
@ConditionalOnClass(OSS.class)
@EnableConfigurationProperties(AliyunOssProperties.class)
@ConditionalOnProperty(prefix = "xarch.storage.configs.aliyun-oss", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class AliyunOssAutoConfiguration {

    /**
     * Register the {@link AliyunOssStorageProvider} bean.
     *
     * @param properties the Aliyun OSS configuration
     * @return a new {@link AliyunOssStorageProvider}
     */
    @Bean
    @ConditionalOnMissingBean(name = "aliyunOssStorageProvider")
    public StorageProvider aliyunOssStorageProvider(AliyunOssProperties properties) {
        return new AliyunOssStorageProvider(properties);
    }
}
