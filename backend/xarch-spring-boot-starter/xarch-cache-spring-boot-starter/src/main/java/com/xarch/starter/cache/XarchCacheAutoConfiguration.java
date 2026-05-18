package com.xarch.starter.cache;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache auto-configuration placeholder
 * Real implementation would configure RedissonClient
 */
@Configuration
public class XarchCacheAutoConfiguration {

    @Bean
    public String cachePlaceholder() {
        return "xarch-cache-placeholder";
    }
}