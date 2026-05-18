package com.xarch.example.controller;

import com.xarch.example.entity.Config;
import com.xarch.example.service.ConfigService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * System config controller
 */
@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping
    @XarchLog(value = "Query config list", type = "QUERY")
    public ApiResult<PageResult<Config>> page(
            @RequestParam(required = false) String configKey,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(configService.page(configKey, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Config> detail(@PathVariable Long id) {
        return ApiResult.ok(configService.getById(id));
    }

    @GetMapping("/value/{configKey}")
    public ApiResult<String> getValue(@PathVariable String configKey) {
        return ApiResult.ok(configService.getValue(configKey));
    }

    @PostMapping
    @XarchLog(value = "Create config", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Config config) {
        configService.create(config);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update config", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Config config) {
        config.setId(id);
        configService.update(config);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete config", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResult.ok();
    }
}