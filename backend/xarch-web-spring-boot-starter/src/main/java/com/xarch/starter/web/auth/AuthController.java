package com.xarch.starter.web.auth;

import cn.dev33.satoken.context SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.entity.LoginUser;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.util.ResultUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Auth controller for login/logout/captcha
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Mock users: username -> password
    private static final Map<String, String> MOCK_USERS = new HashMap<>();
    private static final Map<String, LoginUser> MOCK_USER_INFO = new HashMap<>();

    static {
        MOCK_USERS.put("admin", "admin123");
        MOCK_USERS.put("user", "user123");

        LoginUser admin = new LoginUser();
        admin.setUserId(1L);
        admin.setUsername("admin");
        admin.setNickname("Administrator");
        admin.setUserType(1);
        admin.setRoleIds("1");
        admin.setRoleNames("Super Admin");
        admin.setPermissions("*:*:*");
        MOCK_USER_INFO.put("admin", admin);

        LoginUser user = new LoginUser();
        user.setUserId(2L);
        user.setUsername("user");
        user.setNickname("Normal User");
        user.setUserType(2);
        user.setRoleIds("2");
        user.setRoleNames("User");
        user.setPermissions("user:read");
        MOCK_USER_INFO.put("user", user);
    }

    @PostMapping("/login")
    @XarchLog(value = "User login", type = "AUTH")
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        String storedPassword = MOCK_USERS.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            return ResultUtil.fail("4010", "Invalid username or password");
        }

        LoginUser userInfo = MOCK_USER_INFO.get(username);
        StpUtil.login(username);
        String token = StpUtil.getTokenValue();

        LoginResponse response = new LoginResponse(
            token,
            System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
            username,
            userInfo.getRoleNames(),
            userInfo.getNickname()
        );

        SaHolder.getSession().set("userInfo", userInfo);

        return ResultUtil.ok(response);
    }

    @PostMapping("/logout")
    @XarchLog(value = "User logout", type = "AUTH")
    public ApiResult<Void> logout() {
        StpUtil.logout();
        return ResultUtil.ok();
    }

    @GetMapping("/me")
    public ApiResult<LoginUser> getCurrentUser() {
        Object loginId = StpUtil.getLoginId();
        if (loginId == null) {
            return ResultUtil.fail("4010", "Unauthorized");
        }
        LoginUser userInfo = (LoginUser) SaHolder.getSession().get("userInfo");
        return ResultUtil.ok(userInfo);
    }

    @GetMapping("/captcha")
    public ApiResult<CaptchaResponse> getCaptcha() {
        String key = String.valueOf(System.currentTimeMillis());
        String captchaBase64 = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAiIGhlaWdodD0iNDAiPgo8Y2lyY2xlIGN4PSI2MCIgY3k9IjIwIiByPSIyNSIgZmlsbD0iIzAwNjYiLz4KPGNpcmNsZSBjeD0iNjAiIGN5PSIyMCIgcj0iMTYiIGZpbGw9IiNmZmYiLz4KPC9zdmc+";
        return ResultUtil.ok(new CaptchaResponse(captchaBase64, key));
    }
}