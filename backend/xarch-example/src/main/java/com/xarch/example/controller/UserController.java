package com.xarch.example.controller;

import com.xarch.example.entity.User;
import com.xarch.example.service.UserService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.entity.PageQuery;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @XarchLog(value = "Query user list", type = "QUERY")
    public ApiResult<PageResult<User>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(userService.page(username, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<User> detail(@PathVariable Long id) {
        return ApiResult.ok(userService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create user", type = "CREATE")
    public ApiResult<Void> create(@RequestBody User user) {
        userService.create(user);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update user", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete user", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<User>> options() {
        return ApiResult.ok(userService.list());
    }

    /**
     * Get roles assigned to a user (for transfer component)
     */
    @GetMapping("/{id}/roles")
    public ApiResult<List<Long>> getUserRoles(@PathVariable Long id) {
        return ApiResult.ok(userService.getRoleIds(id));
    }

    /**
     * Assign roles to a user (for transfer component)
     */
    @PutMapping("/{id}/roles")
    @XarchLog(value = "Assign user roles", type = "UPDATE")
    public ApiResult<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResult.ok();
    }
}