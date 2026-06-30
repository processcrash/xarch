package com.xarch.starter.storage.minio;

import com.xarch.starter.storage.core.StorageProvider;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the MinIO storage provider.
 * <p>
 * Activated when:
 * <ul>
 *   <li>The MinIO class is on the classpath.</li>
 *   <li>{@code xarch.storage.configs.minio.enabled=true} (the default).</li>
 *   <li>Endpoint and credentials are configured.</li>
 * </ul>
 * </p>
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "xarch.storage.configs.minio", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class MinioStorageAutoConfiguration {

    /**
     * Register the {@link MinioStorageProvider} bean.
     *
     * @param properties the MinIO configuration
     * @return a new {@link MinioStorageProvider}
     */
    @Bean
    @ConditionalOnMissingBean(name = "minioStorageProvider")
    public StorageProvider minioStorageProvider(MinioProperties properties) {
        return new MinioStorageProvider(properties);
    }
}
