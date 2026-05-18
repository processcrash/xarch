package com.xarch.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.User;
import com.xarch.example.mapper.UserMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User service - includes user-role relation operations
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
        return userMapper.selectUserWithRoles(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(User user) {
        userMapper.insert(user);
        if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), parseRoleIds(user.getRoleIds()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(User user) {
        userMapper.updateById(user);
        if (user.getRoleIds() != null) {
            userMapper.deleteUserRoles(user.getId());
            if (!user.getRoleIds().isEmpty()) {
                assignRoles(user.getId(), parseRoleIds(user.getRoleIds()));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        userMapper.deleteUserRoles(id);
        userMapper.deleteById(id);
    }

    public List<User> list() {
        return userMapper.selectList(null);
    }

    /**
     * Get role IDs for a user
     */
    public List<Long> getRoleIds(Long userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * Assign roles to a user
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRoles(userId);
        for (Long roleId : roleIds) {
            userMapper.insertUserRole(userId, roleId);
        }
    }

    /**
     * Parse comma-separated role IDs to list
     */
    private List<Long> parseRoleIds(String roleIds) {
        return Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}