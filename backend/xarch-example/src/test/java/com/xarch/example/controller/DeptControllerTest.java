package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeptController unit tests
 */
@SpringBootTest
class DeptControllerTest {

    @Autowired
    private DeptController deptController;

    @Test
    void testPage() {
        ApiResult<PageResult<com.xarch.example.entity.Dept>> result = deptController.page(null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testTree() {
        ApiResult<List<com.xarch.example.entity.Dept>> result = deptController.tree();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testOptions() {
        ApiResult<List<com.xarch.example.entity.Dept>> result = deptController.options();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}