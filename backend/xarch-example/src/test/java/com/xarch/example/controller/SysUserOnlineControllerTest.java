package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysUserOnline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserOnlineController unit tests
 */
@SpringBootTest
class SysUserOnlineControllerTest {

    @Autowired
    private SysUserOnlineController onlineController;

    @Test
    void testList() {
        var result = onlineController.list(null, null);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testListWithParams() {
        var result = onlineController.list("127.0.0.1", "admin");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testForceLogout() {
        var result = onlineController.forceLogout("nonexistent-token");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}