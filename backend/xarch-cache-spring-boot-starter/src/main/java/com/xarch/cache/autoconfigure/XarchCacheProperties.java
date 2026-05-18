package com.xarch.cache.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Cache starter properties
 */
@EnableConfigurationProperties
public class XarchCacheProperties {

    private boolean enabled = true;
    private int defaultExpireSeconds = 3600;
    private String redisHost = "localhost";
    private int redisPort = 6379;
    private String redisPassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultExpireSeconds() {
        return defaultExpireSeconds;
    }

    public void setDefaultExpireSeconds(int defaultExpireSeconds) {
        this.defaultExpireSeconds = defaultExpireSeconds;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }
}