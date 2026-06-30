package com.xarch.starter.storage.s3;

import com.xarch.starter.storage.core.StorageProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Auto-configuration for the generic AWS S3 storage provider.
 */
@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(prefix = "xarch.storage.configs.s3", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class S3StorageAutoConfiguration {

    /**
     * Register the {@link S3StorageProvider} bean.
     *
     * @param properties the S3 configuration
     * @return a new {@link S3StorageProvider}
     */
    @Bean
    @ConditionalOnMissingBean(name = "s3StorageProvider")
    public StorageProvider s3StorageProvider(S3Properties properties) {
        return new S3StorageProvider(properties);
    }
}
