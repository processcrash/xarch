package com.xarch.example.auth.controller;

import com.xarch.example.auth.entity.User;
import com.xarch.example.auth.service.UserService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller — migrated verbatim from the monolith.
 *
 * <p>Original package: {@code com.xarch.example.controller.UserController}.
 * Logic is identical; only the package and service references changed.
 */
@Tag(name = "User Management", description = "User CRUD and role assignment")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Page query users.
     */
    @GetMapping
    @XarchLog(value = "Query user list", type = "QUERY")
    public ApiResult<PageResult<User>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(userService.page(username, status, pageNum, pageSize));
    }

    /**
     * Get one user by id.
     */
    @GetMapping("/{id}")
    public ApiResult<User> detail(@PathVariable Long id) {
        return ApiResult.ok(userService.getById(id));
    }

    /**
     * Create a new user.
     */
    @PostMapping
    @XarchLog(value = "Create user", type = "CREATE")
    public ApiResult<Void> create(@RequestBody User user) {
        userService.create(user);
        return ApiResult.ok();
    }

    /**
     * Update an existing user.
     */
    @PutMapping("/{id}")
    @XarchLog(value = "Update user", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return ApiResult.ok();
    }

    /**
     * Delete a user by id.
     */
    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete user", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Lightweight option list (for select / dropdown components).
     */
    @GetMapping("/options")
    public ApiResult<List<User>> options() {
        return ApiResult.ok(userService.list());
    }

    /**
     * Get roles assigned to a user (for transfer component).
     */
    @GetMapping("/{id}/roles")
    public ApiResult<List<Long>> getUserRoles(@PathVariable Long id) {
        return ApiResult.ok(userService.getRoleIds(id));
    }

    /**
     * Replace the role assignment of a user.
     */
    @PutMapping("/{id}/roles")
    @XarchLog(value = "Assign user roles", type = "UPDATE")
    public ApiResult<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResult.ok();
    }
}