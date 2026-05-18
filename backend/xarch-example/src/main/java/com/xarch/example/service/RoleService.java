package com.xarch.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Role;
import com.xarch.example.mapper.RoleMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Role service - includes role-menu and role-dept operations
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
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), parseMenuIds(role.getMenuIds()));
        }
    }

    public void update(Role role) {
        roleMapper.updateById(role);
        if (role.getMenuIds() != null) {
            roleMapper.deleteRoleMenus(role.getId());
            if (!role.getMenuIds().isEmpty()) {
                assignMenus(role.getId(), parseMenuIds(role.getMenuIds()));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        roleMapper.deleteRoleMenus(id);
        roleMapper.deleteRoleDepts(id);
        roleMapper.deleteById(id);
    }

    /**
     * Get menu IDs for a role
     */
    public List<Long> getMenuIds(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * Get department IDs for a role
     */
    public List<Long> getDeptIds(Long roleId) {
        return roleMapper.selectDeptIdsByRoleId(roleId);
    }

    /**
     * Assign menus to a role
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMapper.deleteRoleMenus(roleId);
        for (Long menuId : menuIds) {
            roleMapper.insertRoleMenu(roleId, menuId);
        }
    }

    /**
     * Assign departments to a role (data permission)
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignDepts(Long roleId, List<Long> deptIds) {
        roleMapper.deleteRoleDepts(roleId);
        for (Long deptId : deptIds) {
            roleMapper.insertRoleDept(roleId, deptId);
        }
    }

    private List<Long> parseMenuIds(String menuIds) {
        return Arrays.stream(menuIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}