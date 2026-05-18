package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigController unit tests
 */
@SpringBootTest
class ConfigControllerTest {

    @Autowired
    private ConfigController configController;

    @Test
    void testPage() {
        ApiResult<PageResult<com.xarch.example.entity.Config>> result = configController.page(null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetValue() {
        ApiResult<String> result = configController.getValue("sys.index.title");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}