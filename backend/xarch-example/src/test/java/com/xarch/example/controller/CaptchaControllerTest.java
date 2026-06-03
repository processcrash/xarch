package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CaptchaController unit tests
 */
@SpringBootTest
public class CaptchaControllerTest {

    @Autowired
    private CaptchaController captchaController;

    @Test
    void testGenerateCaptcha() {
        ApiResult<Map<String, String>> result = captchaController.generateCaptcha();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().get("captchaKey"));
        assertNotNull(result.getData().get("captchaBase64"));
    }
}