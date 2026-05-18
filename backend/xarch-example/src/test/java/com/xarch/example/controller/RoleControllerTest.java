package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleController unit tests
 */
@SpringBootTest
class RoleControllerTest {

    @Autowired
    private RoleController roleController;

    @Test
    void testPage() {
        ApiResult<PageResult<com.xarch.example.entity.Role>> result = roleController.page(null, null, 1, 10);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testOptions() {
        ApiResult<List<com.xarch.example.entity.Role>> result = roleController.options();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetRoleMenus() {
        ApiResult<List<Long>> result = roleController.getRoleMenus(1L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testAssignMenus() {
        List<Long> menuIds = List.of(1L, 100L);
        ApiResult<Void> result = roleController.assignMenus(1L, menuIds);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetRoleDepts() {
        ApiResult<List<Long>> result = roleController.getRoleDepts(1L);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testAssignDepts() {
        List<Long> deptIds = List.of(100L, 101L);
        ApiResult<Void> result = roleController.assignDepts(1L, deptIds);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}