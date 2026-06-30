package com.xarch.starter.storage.local;

import com.xarch.starter.storage.core.StorageProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the local filesystem storage provider.
 * <p>
 * The provider is always registered (it serves as the default backend) and
 * can be disabled explicitly with
 * {@code xarch.storage.configs.local.enabled=false}.
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(LocalStorageProperties.class)
@ConditionalOnProperty(prefix = "xarch.storage.configs.local", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class LocalStorageAutoConfiguration {

    /**
     * Register the {@link LocalStorageProvider} bean.
     *
     * @param properties the local storage configuration
     * @return a new {@link LocalStorageProvider}
     */
    @Bean
    @ConditionalOnMissingBean(name = "localStorageProvider")
    public StorageProvider localStorageProvider(LocalStorageProperties properties) {
        return new LocalStorageProvider(properties);
    }
}
