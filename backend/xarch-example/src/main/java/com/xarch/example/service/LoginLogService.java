package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.LoginLog;
import com.xarch.example.mapper.LoginLogMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Login log service
 */
@Service
public class LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    public PageResult<LoginLog> page(String username, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_login_log");
        if (StringUtils.hasText(username)) {
            wrapper.where("username LIKE ?", "%" + username + "%");
        }
        wrapper.orderBy("login_time", false);

        Page<LoginLog> page = loginLogMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public void save(LoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }
}