package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.example.entity.SysCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 缓存监控
 */
@RestController
@RequestMapping("/monitor/cache")
public class SysCacheController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final List<SysCache> caches = new ArrayList<>();
    static {
        caches.add(new SysCache("login_tokens", "用户信息"));
        caches.add(new SysCache("sys_config:", "配置信息"));
        caches.add(new SysCache("sys_dict:", "数据字典"));
        caches.add(new SysCache("captcha_codes:", "验证码"));
        caches.add(new SysCache("repeat_submit:", "防重提交"));
        caches.add(new SysCache("rate_limit:", "限流处理"));
        caches.add(new SysCache("pwd_err_cnt:", "密码错误次数"));
    }

    /**
     * 获取缓存信息
     */
    @GetMapping
    public ApiResult<Map<String, Object>> getInfo() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("caches", caches);
        result.put("size", caches.size());
        return ApiResult.success(result);
    }

    /**
     * 获取缓存名称列表
     */
    @GetMapping("/getNames")
    public ApiResult<List<SysCache>> cache() {
        return ApiResult.success(caches);
    }

    /**
     * 获取缓存键名列表
     */
    @GetMapping("/getKeys/{cacheName}")
    public ApiResult<Set<String>> getCacheKeys(@PathVariable String cacheName) {
        Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        return ApiResult.success(new TreeSet<>(cacheKeys));
    }

    /**
     * 获取缓存值
     */
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public ApiResult<SysCache> getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
        return ApiResult.success(sysCache);
    }

    /**
     * 清除缓存名称
     */
    @DeleteMapping("/clearCacheName/{cacheName}")
    public ApiResult<Void> clearCacheName(@PathVariable String cacheName) {
        Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        redisTemplate.delete(cacheKeys);
        return ApiResult.success(null);
    }

    /**
     * 清除缓存键
     */
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public ApiResult<Void> clearCacheKey(@PathVariable String cacheKey) {
        redisTemplate.delete(cacheKey);
        return ApiResult.success(null);
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/clearCacheAll")
    public ApiResult<Void> clearCacheAll() {
        Collection<String> cacheKeys = redisTemplate.keys("*");
        redisTemplate.delete(cacheKeys);
        return ApiResult.success(null);
    }
}