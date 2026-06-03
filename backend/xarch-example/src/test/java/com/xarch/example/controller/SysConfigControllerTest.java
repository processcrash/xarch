package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysConfig;
import com.xarch.example.service.ISysConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysConfigController unit tests
 */
@SpringBootTest
class SysConfigControllerTest {

    @Autowired
    private SysConfigController configController;

    @Autowired
    private ISysConfigService configService;

    @Test
    void testList() {
        PageResult<SysConfig> result = configController.list(new SysConfig());
        assertNotNull(result);
    }

    @Test
    void testGetInfo() {
        var result = configController.getInfo(1L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetConfigKey() {
        var result = configController.getConfigKey("sys.index.skinName");
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testAdd() {
        SysConfig config = new SysConfig();
        config.setConfigName("Test Config");
        config.setConfigKey("test.config.key");
        config.setConfigValue("testValue");
        config.setConfigType("N");

        var result = configController.add(config);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}