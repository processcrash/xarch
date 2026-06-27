package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.Role;
import com.xarch.example.system.service.RoleService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub RoleService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    @Override public PageResult<Role> page(String a, String b, int p, int s) { return PageResult.empty(); }
    @Override public Role getById(Long id) { return null; }
    @Override public void create(Role role) { }
    @Override public void update(Role role) { }
    @Override public void delete(Long id) { }
    @Override public List<Role> list() { return List.of(); }
    @Override public List<Long> getMenuIds(Long id) { return List.of(); }
    @Override public void assignMenus(Long id, List<Long> menuIds) { }
    @Override public List<Long> getDeptIds(Long id) { return List.of(); }
    @Override public void assignDepts(Long id, List<Long> deptIds) { }
}