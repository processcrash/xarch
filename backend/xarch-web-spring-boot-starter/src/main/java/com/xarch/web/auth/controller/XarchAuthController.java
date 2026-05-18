package com.xarch.web.auth.controller;

import com.xarch.common.core.result.ApiResult;
import com.xarch.common.core.util.ResultUtil;
import com.xarch.web.auth.entity.LoginRequest;
import com.xarch.web.auth.entity.LoginResponse;
import com.xarch.web.auth.service.XarchTokenService;
import com.xarch.web.log.annotation.XarchLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller
 */
@Tag(name = "Auth API")
@RestController
@RequestMapping("/api/auth")
public class XarchAuthController {

    private final XarchTokenService tokenService;

    private static final Map<String, String> MOCK_USERS = new HashMap<>();

    static {
        MOCK_USERS.put("admin", "admin123");
        MOCK_USERS.put("user", "user123");
    }

    public XarchAuthController(XarchTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    @XarchLog(value = "User login", type = "AUTH")
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String password = request.getPassword();

        String storedPassword = MOCK_USERS.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            return ResultUtil.fail("401", "Invalid username or password");
        }

        String token = tokenService.createToken(username);
        long expireTime = tokenService.getExpireTime(token);

        LoginResponse response = new LoginResponse(token, expireTime, username, "admin");
        return ResultUtil.ok(response);
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    @XarchLog(value = "User logout", type = "AUTH")
    public ApiResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.removeToken(token);
        }
        return ResultUtil.ok();
    }

    @Operation(summary = "Get current user info")
    @GetMapping("/me")
    public ApiResult<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResultUtil.fail("401", "Unauthorized");
        }

        String token = authHeader.substring(7);
        if (!tokenService.validateToken(token)) {
            return ResultUtil.fail("401", "Token expired or invalid");
        }

        String username = tokenService.getUsernameByToken(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("roles", "admin");
        return ResultUtil.ok(userInfo);
    }
}