package com.xarch.example.system.controller;

import com.xarch.example.system.entity.Role;
import com.xarch.example.system.service.RoleService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role controller — migrated from {@code com.xarch.example.controller.RoleController}.
 */
@Tag(name = "Role Management")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

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

    @GetMapping("/{id}/menus")
    public ApiResult<List<Long>> getRoleMenus(@PathVariable Long id) {
        return ApiResult.ok(roleService.getMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @XarchLog(value = "Assign role menus", type = "UPDATE")
    public ApiResult<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResult.ok();
    }

    @GetMapping("/{id}/depts")
    public ApiResult<List<Long>> getRoleDepts(@PathVariable Long id) {
        return ApiResult.ok(roleService.getDeptIds(id));
    }

    @PutMapping("/{id}/depts")
    @XarchLog(value = "Assign role departments", type = "UPDATE")
    public ApiResult<Void> assignDepts(@PathVariable Long id, @RequestBody List<Long> deptIds) {
        roleService.assignDepts(id, deptIds);
        return ApiResult.ok();
    }
}