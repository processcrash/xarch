package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TempFileController unit tests
 */
@SpringBootTest
class TempFileControllerTest {

    @Autowired
    private TempFileController tempFileController;

    @Test
    void testPage() {
        var result = tempFileController.page(null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}