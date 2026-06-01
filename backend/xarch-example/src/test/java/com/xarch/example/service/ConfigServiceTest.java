package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Config;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigService unit tests
 */
@XarchTestBase
@DisplayName("ConfigService Unit Tests")
class ConfigServiceTest {

    private final ConfigService configService;

    ConfigServiceTest(ConfigService configService) {
        this.configService = configService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Config> result = configService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with key filter")
    void testPageWithKey() {
        PageResult<Config> result = configService.page("sys", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Config config = configService.getById(1L);
        if (config != null) {
            assertNotNull(config.getConfigKey());
        }
    }

    @Test
    @DisplayName("Get value returns null for non-existent key")
    void testGetValueNotFound() {
        String value = configService.getValue("non_existent_key");
        assertNull(value);
    }
}
