package com.xarch.example.integration;

import com.xarch.example.XarchIntegrationTestBase;
import com.xarch.example.entity.LoginLog;
import com.xarch.example.entity.OpLog;
import com.xarch.example.service.LoginLogService;
import com.xarch.example.service.OpLogService;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Log Integration Test
 * Tests log creation and retrieval
 */
@XarchIntegrationTestBase
@DisplayName("Log Integration Test")
class LogIntegrationTest {

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private OpLogService opLogService;

    @Test
    @DisplayName("Create and retrieve login log")
    void testLoginLog() {
        LoginLog log = new LoginLog();
        log.setUsername("integration_test");
        log.setIp("192.168.1.100");
        log.setLocation("Test Location");
        log.setLoginTime(LocalDateTime.now());
        log.setLoginType(1);
        log.setStatus(1);
        log.setMessage("Integration test login");

        loginLogService.save(log);
        assertNotNull(log.getId());

        PageResult<LoginLog> result = loginLogService.page("integration_test", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create and retrieve operation log")
    void testOpLog() {
        OpLog log = new OpLog();
        log.setUsername("integration_test");
        log.setOperation("Test Operation");
        log.setType("TEST");
        log.setMethod("testMethod");
        log.setIp("192.168.1.100");
        log.setStatus(1);
        log.setCreateTime(LocalDateTime.now());

        opLogService.save(log);
        assertNotNull(log.getId());

        PageResult<OpLog> result = opLogService.page("integration_test", 1, 10);
        assertNotNull(result);
    }
}
