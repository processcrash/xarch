package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.Role;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleService unit tests
 */
@XarchTestBase
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    private final RoleService roleService;

    RoleServiceTest(RoleService roleService) {
        this.roleService = roleService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<Role> result = roleService.page(null, null, 1, 10);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @DisplayName("Page query with role name filter")
    void testPageWithRoleName() {
        PageResult<Role> result = roleService.page("admin", null, 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Page query with role code filter")
    void testPageWithRoleCode() {
        PageResult<Role> result = roleService.page(null, "ADMIN", 1, 10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("List returns non-null")
    void testList() {
        List<Role> roles = roleService.list();
        assertNotNull(roles);
    }

    @Test
    @DisplayName("Get by ID returns role or null")
    void testGetById() {
        Role role = roleService.getById(1L);
        if (role != null) {
            assertNotNull(role.getRoleName());
        }
    }

    @Test
    @DisplayName("Get menu IDs for role")
    void testGetMenuIds() {
        List<Long> menuIds = roleService.getMenuIds(1L);
        assertNotNull(menuIds);
    }

    @Test
    @DisplayName("Get dept IDs for role")
    void testGetDeptIds() {
        List<Long> deptIds = roleService.getDeptIds(1L);
        assertNotNull(deptIds);
    }
}
