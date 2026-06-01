package com.xarch.example.integration;

import com.xarch.example.XarchIntegrationTestBase;
import com.xarch.example.entity.StorageConfig;
import com.xarch.example.storage.StorageConfigService;
import com.xarch.example.storage.StorageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Storage Integration Test
 * Tests storage configuration management
 */
@XarchIntegrationTestBase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Storage Integration Test")
class StorageIntegrationTest {

    @Autowired
    private StorageConfigService storageConfigService;

    private static Long testConfigId;

    @Test
    @Order(1)
    @DisplayName("Create storage config")
    void testCreateConfig() {
        StorageConfig config = new StorageConfig();
        config.setStorageType(StorageType.LOCAL.getCode());
        config.setConfigName("Test Local Storage");
        config.setEndpoint("/tmp/test");
        config.setBucketName("test-bucket");
        config.setIsDefault(0);
        config.setStatus(1);

        storageConfigService.create(config);
        assertNotNull(config.getId());
        testConfigId = config.getId();
    }

    @Test
    @Order(2)
    @DisplayName("List enabled storage configs")
    void testListEnabled() {
        List<StorageConfig> configs = storageConfigService.listEnabled();
        assertNotNull(configs);
        assertTrue(configs.size() > 0);
    }

    @Test
    @Order(3)
    @DisplayName("Get storage config by ID")
    void testGetById() {
        StorageConfig config = storageConfigService.getById(testConfigId);
        assertNotNull(config);
        assertEquals("Test Local Storage", config.getConfigName());
    }

    @Test
    @Order(4)
    @DisplayName("Update storage config")
    void testUpdate() {
        StorageConfig config = storageConfigService.getById(testConfigId);
        config.setConfigName("Updated Test Storage");
        storageConfigService.update(config);

        StorageConfig updated = storageConfigService.getById(testConfigId);
        assertEquals("Updated Test Storage", updated.getConfigName());
    }

    @Test
    @Order(5)
    @DisplayName("Test connection (should not throw)")
    void testTestConnection() {
        StorageConfig config = storageConfigService.getById(testConfigId);
        assertDoesNotThrow(() -> storageConfigService.testConnection(config));
    }

    @Test
    @Order(6)
    @DisplayName("Delete storage config (soft delete)")
    void testDelete() {
        storageConfigService.delete(testConfigId);
        // Soft delete: record still exists but delFlag=1
        StorageConfig deleted = storageConfigService.getById(testConfigId);
        assertNotNull(deleted);
        assertEquals(1, deleted.getDelFlag());
    }
}
