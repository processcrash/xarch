package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysConfig;
import com.xarch.example.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置操作处理
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigController {

    @Autowired
    private ISysConfigService configService;

    /**
     * 查询参数配置列表
     */
    @GetMapping("/list")
    public PageResult<List<SysConfig>> list(SysConfig config) {
        List<SysConfig> list = configService.selectConfigList(config);
        return PageResult.ok(list);
    }

    /**
     * 获取参数配置详细信息
     */
    @GetMapping(value = "/{configId}")
    public ApiResult<SysConfig> getInfo(@PathVariable("configId") Long configId) {
        return ApiResult.success(configService.selectConfigById(configId));
    }

    /**
     * 根据键名查询参数配置信息
     */
    @GetMapping(value = "/key/{configKey}")
    public ApiResult<String> getConfigKey(@PathVariable("configKey") String configKey) {
        return ApiResult.success(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PostMapping
    public ApiResult<Void> add(@RequestBody SysConfig config) {
        return ApiResult.success(configService.insertConfig(config) > 0);
    }

    /**
     * 修改参数配置
     */
    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysConfig config) {
        return ApiResult.success(configService.updateConfig(config) > 0);
    }

    /**
     * 删除参数配置
     */
    @DeleteMapping("/{configIds}")
    public ApiResult<Void> remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return ApiResult.success(true);
    }

    /**
     * 刷新参数缓存
     */
    @DeleteMapping("/refresh")
    public ApiResult<Void> refresh() {
        configService.resetConfigCache();
        return ApiResult.success(true);
    }
}