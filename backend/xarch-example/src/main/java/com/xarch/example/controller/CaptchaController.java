package com.xarch.example.controller;

import com.xarch.example.service.CaptchaService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Captcha controller
 */
@RestController
@RequestMapping("/api/common/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping
    @XarchLog(value = "Generate captcha", type = "QUERY")
    public ApiResult<Map<String, String>> generateCaptcha() {
        return ApiResult.ok(captchaService.generateCaptcha());
    }
}