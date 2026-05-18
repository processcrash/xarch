package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.LoginLog;
import com.xarch.example.mapper.LoginLogMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Login log service
 */
@Service
public class LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    public PageResult<LoginLog> page(String username, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LoginLog>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(LoginLog::getUsername, username);
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        Page<LoginLog> result = loginLogMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public void save(LoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }
}