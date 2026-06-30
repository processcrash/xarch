package com.xarch.starter.storage;

import com.xarch.starter.storage.aliyun.AliyunOssAutoConfiguration;
import com.xarch.starter.storage.core.StorageProvider;
import com.xarch.starter.storage.core.StorageProviderFactory;
import com.xarch.starter.storage.local.LocalStorageAutoConfiguration;
import com.xarch.starter.storage.minio.MinioStorageAutoConfiguration;
import com.xarch.starter.storage.s3.S3StorageAutoConfiguration;
import com.xarch.starter.storage.service.FileStorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Top-level auto-configuration that wires together all storage providers.
 * <p>
 * All four sub-configurations are imported unconditionally; each one
 * internally uses class and property based conditions to decide whether
 * to register its bean. This guarantees that:
 * <ul>
 *   <li>Local storage is always available out of the box.</li>
 *   <li>MinIO, Aliyun OSS and S3 are only enabled if their SDKs are on
 *       the classpath and the corresponding properties are configured.</li>
 * </ul>
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(StorageProperties.class)
@Import({
        LocalStorageAutoConfiguration.class,
        MinioStorageAutoConfiguration.class,
        AliyunOssAutoConfiguration.class,
        S3StorageAutoConfiguration.class
})
public class XarchStorageAutoConfiguration {

    /**
     * Register the {@link StorageProviderFactory} that exposes a typed
     * view of all available providers.
     *
     * @param providers all {@link StorageProvider} beans in the context
     * @param properties the storage configuration
     * @return a new factory
     */
    @Bean
    public StorageProviderFactory storageProviderFactory(
            List<StorageProvider> providers,
            StorageProperties properties) {
        return new StorageProviderFactory(providers, properties);
    }

    /**
     * Register the high-level {@link FileStorageService} facade.
     *
     * @param providerFactory the provider factory
     * @param properties      the storage configuration
     * @return a new {@link FileStorageService}
     */
    @Bean
    public FileStorageService fileStorageService(
            StorageProviderFactory providerFactory,
            StorageProperties properties) {
        return new FileStorageService(providerFactory, properties);
    }
}
