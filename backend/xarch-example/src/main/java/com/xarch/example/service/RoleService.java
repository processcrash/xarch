package com.xarch.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Role;
import com.xarch.example.mapper.RoleMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Role service
 */
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public PageResult<Role> page(String roleName, String roleCode, int pageNum, int pageSize) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(Role::getRoleName, roleName);
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.eq(Role::getRoleCode, roleCode);
        }
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> page = new Page<>(pageNum, pageSize);
        Page<Role> result = roleMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    public List<Role> list() {
        return roleMapper.selectList(null);
    }

    public void create(Role role) {
        roleMapper.insert(role);
    }

    public void update(Role role) {
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
    }
}