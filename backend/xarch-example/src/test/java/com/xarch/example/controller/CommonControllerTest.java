package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonController unit tests
 */
@SpringBootTest
class CommonControllerTest {

    @Autowired
    private CommonController commonController;

    @Test
    void testQuerySelector() {
        ApiResult<Map<String, Object>> result = commonController.querySelector("user", null);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testChallenge() {
        ApiResult<Map<String, Object>> result = commonController.challenge();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetOssPrivateUrl() {
        ApiResult<String> result = commonController.getOssPrivateUrl(null, "https://example.com/file.png");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}