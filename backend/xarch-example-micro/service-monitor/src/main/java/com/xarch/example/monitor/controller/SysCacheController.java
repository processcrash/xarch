package com.xarch.example.monitor.controller;

import com.xarch.example.monitor.entity.SysCache;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** Cache monitor controller. */
@Tag(name = "Cache Monitor")
@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor
public class SysCacheController {

    private final RedisTemplate<String, String> redisTemplate;

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

    @GetMapping
    public ApiResult<Map<String, Object>> getInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("caches", caches);
        result.put("size", caches.size());
        return ApiResult.success(result);
    }

    @GetMapping("/getNames")
    public ApiResult<List<SysCache>> cache() {
        return ApiResult.success(caches);
    }

    @GetMapping("/getKeys/{cacheName}")
    public ApiResult<Set<String>> getCacheKeys(@PathVariable String cacheName) {
        Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        return ApiResult.success(new TreeSet<>(cacheKeys));
    }

    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public ApiResult<SysCache> getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
        return ApiResult.success(sysCache);
    }

    @DeleteMapping("/clearCacheName/{cacheName}")
    public ApiResult<Void> clearCacheName(@PathVariable String cacheName) {
        Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        redisTemplate.delete(cacheKeys);
        return ApiResult.success(null);
    }

    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public ApiResult<Void> clearCacheKey(@PathVariable String cacheKey) {
        redisTemplate.delete(cacheKey);
        return ApiResult.success(null);
    }

    @DeleteMapping("/clearCacheAll")
    public ApiResult<Void> clearCacheAll() {
        Collection<String> cacheKeys = redisTemplate.keys("*");
        redisTemplate.delete(cacheKeys);
        return ApiResult.success(null);
    }
}