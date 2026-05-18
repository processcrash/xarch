package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.example.entity.Server;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 */
@RestController
@RequestMapping("/monitor/server")
public class SysServerController {

    /**
     * 获取服务器信息
     */
    @GetMapping
    public ApiResult<Server> getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return ApiResult.success(server);
    }
}