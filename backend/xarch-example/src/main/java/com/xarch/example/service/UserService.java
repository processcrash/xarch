package com.xarch.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.User;
import com.xarch.example.mapper.UserMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * User service
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public PageResult<User> page(String username, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public void create(User user) {
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public List<User> list() {
        return userMapper.selectList(null);
    }
}