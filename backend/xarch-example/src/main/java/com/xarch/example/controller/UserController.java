package com.xarch.example.controller;

import com.xarch.common.core.result.ApiResult;
import com.xarch.common.core.result.ResultCode;
import com.xarch.common.core.util.ResultUtil;
import com.xarch.example.entity.User;
import com.xarch.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller
 */
@Tag(name = "User API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ApiResult<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? ResultUtil.ok(user) : ResultUtil.fail(ResultCode.NOT_FOUND);
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/username/{username}")
    public ApiResult<User> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return user != null ? ResultUtil.ok(user) : ResultUtil.fail(ResultCode.NOT_FOUND);
    }

    @Operation(summary = "List all users")
    @GetMapping
    public ApiResult<List<User>> listAll() {
        return ResultUtil.ok(userService.listAll());
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody User user) {
        return ResultUtil.ok(userService.save(user));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ResultUtil.ok(userService.update(user));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        return ResultUtil.ok(userService.delete(id));
    }
}