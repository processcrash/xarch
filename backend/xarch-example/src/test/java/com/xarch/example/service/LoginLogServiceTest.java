package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.LoginLog;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginLogService unit tests
 */
@XarchTestBase
@DisplayName("LoginLogService Unit Tests")
class LoginLogServiceTest {

    private final LoginLogService loginLogService;

    LoginLogServiceTest(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<LoginLog> result = loginLogService.page(null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with username filter")
    void testPageWithUsername() {
        PageResult<LoginLog> result = loginLogService.page("admin", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Save login log")
    void testSave() {
        LoginLog log = new LoginLog();
        log.setUsername("testuser");
        log.setIp("127.0.0.1");
        log.setStatus(1);

        assertDoesNotThrow(() -> loginLogService.save(log));
    }
}
