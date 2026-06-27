package com.xarch.example.system.controller;

import com.xarch.example.system.entity.LoginLog;
import com.xarch.example.system.entity.OpLog;
import com.xarch.example.system.service.LoginLogService;
import com.xarch.example.system.service.OpLogService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Aggregated log controller — combines the original {@code OpLogController}
 * and {@code LoginLogController} from the monolith into a single class for
 * clearer API grouping.
 */
@Tag(name = "System Logs", description = "Operation and login audit logs")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OpLogService opLogService;
    private final LoginLogService loginLogService;

    /** Operation-log page. */
    @GetMapping("/op")
    public ApiResult<PageResult<OpLog>> opLogPage(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(opLogService.page(username, pageNum, pageSize));
    }

    /** Login-log page. */
    @GetMapping("/login")
    public ApiResult<PageResult<LoginLog>> loginLogPage(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(loginLogService.page(username, pageNum, pageSize));
    }
}