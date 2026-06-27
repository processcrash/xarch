package com.xarch.example.monitor.controller;

import com.xarch.example.monitor.entity.Server;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Server monitor controller. */
@Tag(name = "Server Monitor")
@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class SysServerController {

    /**
     * Get server metrics (CPU / memory / JVM / system).
     */
    @GetMapping
    public ApiResult<Server> getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return ApiResult.success(server);
    }
}