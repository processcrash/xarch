package com.xarch.example.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Captcha service for generating verification codes
 */
@Service
public class CaptchaService {

    public Map<String, String> generateCaptcha() {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(120, 40, 4, 20);
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64();
        String key = String.valueOf(System.currentTimeMillis());

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaBase64", "data:image/png;base64," + imageBase64);
        result.put("captchaCode", code);

        return result;
    }

    public boolean validateCaptcha(String key, String code) {
        return true;
    }
}