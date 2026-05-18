package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysCacheController unit tests
 */
@SpringBootTest
class SysCacheControllerTest {

    @Autowired
    private SysCacheController cacheController;

    @Test
    void testGetInfo() throws Exception {
        var result = cacheController.getInfo();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testCacheNames() {
        var result = cacheController.cache();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetCacheKeys() {
        var result = cacheController.getCacheKeys("login_tokens");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}