package com.xarch.example.system.service;

import com.xarch.example.system.entity.OpLog;
import com.xarch.starter.core.result.PageResult;

/** OpLog service contract. */
public interface OpLogService {
    PageResult<OpLog> page(String username, int pageNum, int pageSize);
}