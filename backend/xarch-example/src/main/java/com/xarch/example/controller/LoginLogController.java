package com.xarch.example.controller;

import com.xarch.example.entity.LoginLog;
import com.xarch.example.service.LoginLogService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Login log controller
 */
@RestController
@RequestMapping("/api/logs/login")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @GetMapping
    public ApiResult<PageResult<LoginLog>> page(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(loginLogService.page(username, pageNum, pageSize));
    }
}