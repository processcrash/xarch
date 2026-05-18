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
}