package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Resource;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResourceService unit tests
 */
@XarchTestBase
@DisplayName("ResourceService Unit Tests")
class ResourceServiceTest {

    private final ResourceService resourceService;

    ResourceServiceTest(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Resource> result = resourceService.page(null, null, null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with scene code filter")
    void testPageWithSceneCode() {
        PageResult<Resource> result = resourceService.page("avatar", null, null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with storage type filter")
    void testPageWithStorageType() {
        PageResult<Resource> result = resourceService.page(null, "local", null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with keyword filter")
    void testPageWithKeyword() {
        PageResult<Resource> result = resourceService.page(null, null, "test", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List all resources")
    void testList() {
        List<Resource> resources = resourceService.list();
        assertNotNull(resources);
    }

    @Test
    @DisplayName("Get by ID")
    void testGetById() {
        Resource resource = resourceService.getById(1L);
        if (resource != null) {
            assertNotNull(resource.getResourceName());
        }
    }

    @Test
    @DisplayName("Get storage statistics")
    void testGetStats() {
        ResourceService.StorageStats stats = resourceService.getStats();
        assertNotNull(stats);
        assertNotNull(stats.getLocalCount());
    }
}
