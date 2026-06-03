package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.example.entity.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysServerController unit tests
 */
@SpringBootTest
class SysServerControllerTest {

    @Autowired
    private SysServerController serverController;

    @Test
    void testGetInfo() throws Exception {
        var result = serverController.getInfo();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        Server server = result.getData();
        assertNotNull(server.getCpu());
        assertNotNull(server.getMem());
        assertNotNull(server.getJvm());
        assertNotNull(server.getSys());
    }
}