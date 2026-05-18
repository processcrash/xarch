package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientController unit tests
 */
@SpringBootTest
class ClientControllerTest {

    @Autowired
    private ClientController clientController;

    @Test
    void testPage() {
        var result = clientController.page(null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testOptions() {
        var result = clientController.options();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}