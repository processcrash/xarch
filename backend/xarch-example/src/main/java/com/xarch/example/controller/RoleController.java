package com.xarch.example.controller;

import com.xarch.example.entity.Role;
import com.xarch.example.service.RoleService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role controller
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @XarchLog(value = "Query role list", type = "QUERY")
    public ApiResult<PageResult<Role>> page(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(roleService.page(roleName, roleCode, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Role> detail(@PathVariable Long id) {
        return ApiResult.ok(roleService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create role", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update role", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.update(role);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete role", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<Role>> options() {
        return ApiResult.ok(roleService.list());
    }

    /**
     * Get menus assigned to a role (for menu transfer component)
     */
    @GetMapping("/{id}/menus")
    public ApiResult<List<Long>> getRoleMenus(@PathVariable Long id) {
        return ApiResult.ok(roleService.getMenuIds(id));
    }

    /**
     * Assign menus to a role (for menu transfer component)
     */
    @PutMapping("/{id}/menus")
    @XarchLog(value = "Assign role menus", type = "UPDATE")
    public ApiResult<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResult.ok();
    }

    /**
     * Get departments assigned to a role (for data permission)
     */
    @GetMapping("/{id}/depts")
    public ApiResult<List<Long>> getRoleDepts(@PathVariable Long id) {
        return ApiResult.ok(roleService.getDeptIds(id));
    }

    /**
     * Assign departments to a role (for data permission)
     */
    @PutMapping("/{id}/depts")
    @XarchLog(value = "Assign role departments", type = "UPDATE")
    public ApiResult<Void> assignDepts(@PathVariable Long id, @RequestBody List<Long> deptIds) {
        roleService.assignDepts(id, deptIds);
        return ApiResult.ok();
    }
}