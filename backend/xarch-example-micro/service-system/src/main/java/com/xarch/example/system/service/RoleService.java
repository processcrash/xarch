package com.xarch.example.system.service;

import com.xarch.example.system.entity.Role;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Role service contract. */
public interface RoleService {
    PageResult<Role> page(String roleName, String roleCode, int pageNum, int pageSize);
    Role getById(Long id);
    void create(Role role);
    void update(Role role);
    void delete(Long id);
    List<Role> list();
    List<Long> getMenuIds(Long id);
    void assignMenus(Long id, List<Long> menuIds);
    List<Long> getDeptIds(Long id);
    void assignDepts(Long id, List<Long> deptIds);
}