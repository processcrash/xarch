package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.OpLog;
import com.xarch.example.mapper.OpLogMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Operation log service
 */
@Service
public class OpLogService {

    @Autowired
    private OpLogMapper opLogMapper;

    public PageResult<OpLog> page(String username, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OpLog>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(OpLog::getUsername, username);
        }
        wrapper.orderByDesc(OpLog::getCreateTime);

        Page<OpLog> page = new Page<>(pageNum, pageSize);
        Page<OpLog> result = opLogMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public void save(OpLog opLog) {
        opLogMapper.insert(opLog);
    }
}