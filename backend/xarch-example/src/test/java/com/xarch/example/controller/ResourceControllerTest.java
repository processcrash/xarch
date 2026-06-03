package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResourceController unit tests
 */
@SpringBootTest
class ResourceControllerTest {

    @Autowired
    private ResourceController resourceController;

    @Test
    void testPage() {
        var result = resourceController.page(null, null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testOptions() {
        var result = resourceController.options();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}