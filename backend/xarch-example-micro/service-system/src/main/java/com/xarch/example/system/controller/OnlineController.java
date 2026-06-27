package com.xarch.example.system.controller;

import com.xarch.example.system.entity.SysUserOnline;
import com.xarch.example.system.service.ISysUserOnlineService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Online user controller — migrated from {@code SysUserOnlineController}. */
@Tag(name = "Online User")
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class OnlineController {

    private final ISysUserOnlineService userOnlineService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOGIN_TOKEN_KEY = "login_tokens:";

    @GetMapping("/list")
    public PageResult<SysUserOnline> list(String ipaddr, String userName) {
        Collection<String> keys = redisTemplate.keys(LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<>();
        for (String key : keys) {
            Object user = redisTemplate.opsForValue().get(key);
            if (user != null) {
                SysUserOnline online;
                if (ipaddr != null && userName != null) {
                    online = userOnlineService.selectOnlineByInfo(ipaddr, userName, user);
                } else if (ipaddr != null) {
                    online = userOnlineService.selectOnlineByIpaddr(ipaddr, userName, user);
                } else if (userName != null) {
                    online = userOnlineService.selectOnlineByUserName(userName, user);
                } else {
                    online = userOnlineService.loginUserToUserOnline(user);
                }
                if (online != null) {
                    userOnlineList.add(online);
                }
            }
        }
        Collections.reverse(userOnlineList);
        return PageResult.ok(userOnlineList);
    }

    @DeleteMapping("/{tokenId}")
    public ApiResult<Void> forceLogout(@PathVariable String tokenId) {
        redisTemplate.delete(LOGIN_TOKEN_KEY + tokenId);
        return ApiResult.success(null);
    }
}