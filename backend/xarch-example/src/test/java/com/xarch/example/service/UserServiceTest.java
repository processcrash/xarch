package com.xarch.example.service;

import com.xarch.example.XarchTestBase;
import com.xarch.example.entity.User;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService unit tests
 */
@XarchTestBase
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    private final UserService userService;

    UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    @DisplayName("Page query returns valid result")
    void testPage() {
        PageResult<User> result = userService.page(null, null, 1, 10);
        assertNotNull(result);
        assertNotNull(result.getList());
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("Page query with username filter")
    void testPageWithUsername() {
        PageResult<User> result = userService.page("admin", null, 1, 10);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @DisplayName("Page query with status filter")
    void testPageWithStatus() {
        PageResult<User> result = userService.page(null, "1", 1, 10);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @DisplayName("List all users returns non-null")
    void testList() {
        List<User> users = userService.list();
        assertNotNull(users);
    }

    @Test
    @DisplayName("Get user by ID returns valid or null result")
    void testGetById() {
        User user = userService.getById(1L);
        // Result can be null for non-existent ID, both are valid
        if (user != null) {
            assertNotNull(user.getUsername());
        }
    }

    @Test
    @DisplayName("Get non-existent user returns null")
    void testGetByIdNotFound() {
        User user = userService.getById(999999L);
        assertNull(user);
    }

    @Test
    @DisplayName("Get user role IDs returns list")
    void testGetRoleIds() {
        List<Long> roleIds = userService.getRoleIds(1L);
        assertNotNull(roleIds);
    }
}
