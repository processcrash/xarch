package com.xarch.example.auth.service.impl;

import com.xarch.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of {@link AuthService}.
 *
 * <p>Concrete login/captcha logic will be migrated from the monolith.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public Map<String, String> generateCaptcha() {
        log.debug("AuthService.generateCaptcha stub called");
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", "stub-id");
        result.put("image", "");
        return result;
    }

    @Override
    public Map<String, Object> login(String username, String password, String captchaId, String captchaCode) {
        log.debug("AuthService.login stub called for user={}", username);
        Map<String, Object> result = new HashMap<>();
        result.put("token", "stub-token");
        return result;
    }

    @Override
    public void logout(String tokenId) {
        log.debug("AuthService.logout stub called");
    }
}