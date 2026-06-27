package com.xarch.example.auth.service;

import java.util.Map;

/**
 * Authentication service contract for {@code service-auth}.
 *
 * <p>Handles login, logout and captcha generation. The implementations
 * are stubs at this stage.
 */
public interface AuthService {

    /**
     * Generate a fresh captcha for the front-end.
     *
     * @return map with captcha key ({@code captchaId}) and base64 image ({@code image})
     */
    Map<String, String> generateCaptcha();

    /**
     * Authenticate by username/password.
     *
     * @param username    submitted username
     * @param password    submitted password
     * @param captchaId   captcha key
     * @param captchaCode submitted captcha text
     * @return authentication result (token / user info)
     */
    Map<String, Object> login(String username, String password, String captchaId, String captchaCode);

    /**
     * Invalidate the current token.
     *
     * @param tokenId token to revoke
     */
    void logout(String tokenId);
}