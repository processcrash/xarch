package com.xarch.example.auth.controller;

import com.xarch.example.auth.service.AuthService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth controller — handles captcha, login and logout.
 *
 * <p>Migrated from the monolithic {@code CaptchaController} with
 * additional login/logout endpoints. Package relocated to
 * {@code com.xarch.example.auth.controller}.
 */
@Tag(name = "Authentication", description = "Login, logout and captcha endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Generate a captcha image.
     *
     * @return captcha id + base64 image payload
     */
    @GetMapping("/captcha")
    @XarchLog(value = "Generate captcha", type = "QUERY")
    public ApiResult<Map<String, String>> captcha() {
        return ApiResult.ok(authService.generateCaptcha());
    }

    /**
     * Authenticate by username + password + captcha.
     *
     * @param body request body containing the four fields
     * @return login result with token and user info
     */
    @PostMapping("/login")
    @XarchLog(value = "User login", type = "LOGIN")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String captchaId = body.get("captchaId");
        String captchaCode = body.get("captchaCode");
        return ApiResult.ok(authService.login(username, password, captchaId, captchaCode));
    }

    /**
     * Logout the current session.
     *
     * @param tokenId token id (header or query)
     * @return success
     */
    @PostMapping("/logout")
    @XarchLog(value = "User logout", type = "LOGOUT")
    public ApiResult<Void> logout(@RequestHeader(value = "xarch-token", required = false) String tokenId) {
        authService.logout(tokenId);
        return ApiResult.ok();
    }
}