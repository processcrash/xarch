package com.xarch.example.system.service;

import com.xarch.example.system.entity.LoginLog;
import com.xarch.starter.core.result.PageResult;

/** LoginLog service contract. */
public interface LoginLogService {
    PageResult<LoginLog> page(String username, int pageNum, int pageSize);
}