package com.xarch.example.controller;

import com.xarch.example.entity.OpLog;
import com.xarch.example.service.OpLogService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Operation log controller
 */
@RestController
@RequestMapping("/api/logs/op")
public class OpLogController {

    @Autowired
    private OpLogService opLogService;

    @GetMapping
    public ApiResult<PageResult<OpLog>> page(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(opLogService.page(username, pageNum, pageSize));
    }
}