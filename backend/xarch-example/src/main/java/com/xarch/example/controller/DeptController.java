package com.xarch.example.controller;

import com.xarch.example.entity.Dept;
import com.xarch.example.service.DeptService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Department controller
 */
@RestController
@RequestMapping("/api/depts")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping
    @XarchLog(value = "Query department list", type = "QUERY")
    public ApiResult<PageResult<Dept>> page(
            @RequestParam(required = false) String deptName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(deptService.page(deptName, pageNum, pageSize));
    }

    @GetMapping("/tree")
    public ApiResult<List<Dept>> tree() {
        return ApiResult.ok(deptService.tree());
    }

    @GetMapping("/{id}")
    public ApiResult<Dept> detail(@PathVariable Long id) {
        return ApiResult.ok(deptService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create department", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Dept dept) {
        deptService.create(dept);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update department", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Dept dept) {
        dept.setId(id);
        deptService.update(dept);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete department", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<Dept>> options() {
        return ApiResult.ok(deptService.tree());
    }
}