package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageController unit tests
 */
@SpringBootTest
class MessageControllerTest {

    @Autowired
    private MessageController messageController;

    @Test
    void testCountMyMessage() {
        ApiResult<Map<String, Object>> result = messageController.countMyMessage();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testListTodo() {
        var result = messageController.listTodo();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testListMsg() {
        var result = messageController.listMsg();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testPage() {
        var result = messageController.page(null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}