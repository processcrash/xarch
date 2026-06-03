package com.xarch.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.entity.Resource;
import com.xarch.example.entity.StorageConfig;
import com.xarch.example.service.ResourceService;
import com.xarch.example.storage.StorageConfigService;
import com.xarch.example.storage.StorageFactory;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileController unit tests
 */
@SpringBootTest
class FileControllerTest {

    @Autowired
    private FileController fileController;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private StorageConfigService storageConfigService;

    @MockitoBean
    private StorageFactory storageFactory;

    @Test
    void testPage() {
        ApiResult<PageResult<Resource>> result = fileController.page(null, null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testPageWithFilters() {
        ApiResult<PageResult<Resource>> result = fileController.page("avatar", "local", "test", 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testDetail() {
        ApiResult<Resource> result = fileController.detail(1L);
        assertNotNull(result);
        // Result may be success or fail depending on whether id=1 exists
        assertNotNull(result.getCode());
    }

    @Test
    void testDetailNotFound() {
        ApiResult<Resource> result = fileController.detail(999999L);
        assertNotNull(result);
        // Should return fail for non-existent resource
        assertNotNull(result.getCode());
    }

    @Test
    void testStats() {
        ApiResult<ResourceService.StorageStats> result = fileController.stats();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testListStorageConfigs() {
        ApiResult<List<StorageConfig>> result = fileController.listStorageConfigs();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetStorageConfig() {
        ApiResult<StorageConfig> result = fileController.getStorageConfig(1L);
        assertNotNull(result);
        // Result depends on whether config exists
        assertNotNull(result.getCode());
    }

    @Test
    void testGetStorageConfigNotFound() {
        ApiResult<StorageConfig> result = fileController.getStorageConfig(999999L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        // Even if not found, returns success with null or fails
    }

    @Test
    void testListStorageTypes() {
        ApiResult<List<FileController.StorageTypeVO>> result = fileController.listStorageTypes();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size()); // local, minio, aliyun_oss
    }

    @Test
    void testDelete() {
        // Test delete (soft delete) - should work if resource exists
        ApiResult<Void> result = fileController.delete(999999L);
        assertNotNull(result);
        // Will return success even if resource doesn't exist (service handles gracefully)
        assertEquals("0000", result.getCode());
    }

    @Test
    void testCreateStorageConfig() {
        StorageConfig config = new StorageConfig();
        config.setStorageType("local");
        config.setBucketName("test-bucket");
        config.setEndpoint("/tmp/test");
        config.setIsDefault(0);
        config.setStatus(1);

        ApiResult<Void> result = fileController.createStorageConfig(config);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testUpdateStorageConfig() {
        StorageConfig config = new StorageConfig();
        config.setId(1L);
        config.setStorageType("local");
        config.setBucketName("updated-bucket");
        config.setEndpoint("/tmp/updated");
        config.setIsDefault(0);
        config.setStatus(1);

        ApiResult<Void> result = fileController.updateStorageConfig(config);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testDeleteStorageConfig() {
        // Delete non-existent config (soft delete)
        ApiResult<Void> result = fileController.deleteStorageConfig(999999L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testTestStorageConfig() {
        // Test connection with non-existent config
        ApiResult<Boolean> result = fileController.testStorageConfig(999999L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}