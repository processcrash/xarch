package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.OpLog;
import com.xarch.example.mapper.OpLogMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Operation log service
 */
@Service
public class OpLogService {

    @Autowired
    private OpLogMapper opLogMapper;

    public PageResult<OpLog> page(String username, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_op_log");
        if (StringUtils.hasText(username)) {
            wrapper.where("username LIKE ?", "%" + username + "%");
        }
        wrapper.orderBy("create_time", false);

        Page<OpLog> page = opLogMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public void save(OpLog opLog) {
        opLogMapper.insert(opLog);
    }
}