package com.xarch.example.controller;

import com.xarch.example.service.UserService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserController unit tests
 */
@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Test
    void testPage() {
        ApiResult<PageResult<com.xarch.example.entity.User>> result = userController.page(null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testDetail() {
        ApiResult<com.xarch.example.entity.User> result = userController.detail(1L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testOptions() {
        ApiResult<List<com.xarch.example.entity.User>> result = userController.options();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testPageWithParams() {
        ApiResult<PageResult<com.xarch.example.entity.User>> result = userController.page("admin", "1", 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}