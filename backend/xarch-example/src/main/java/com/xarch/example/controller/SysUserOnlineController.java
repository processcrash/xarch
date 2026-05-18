package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysUserOnline;
import com.xarch.example.service.ISysUserOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 在线用户监控
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController {

    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 查询在线用户列表
     */
    @GetMapping("/list")
    public PageResult<List<SysUserOnline>> list(String ipaddr, String userName) {
        Collection<String> keys = redisTemplate.keys(LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<>();
        for (String key : keys) {
            Object user = redisTemplate.opsForValue().get(key);
            if (user != null) {
                SysUserOnline online = null;
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

    /**
     * 强退用户
     */
    @DeleteMapping("/{tokenId}")
    public ApiResult<Void> forceLogout(@PathVariable String tokenId) {
        redisTemplate.delete(LOGIN_TOKEN_KEY + tokenId);
        return ApiResult.success(null);
    }
}