package com.xarch.example.controller;

import com.xarch.example.entity.Menu;
import com.xarch.example.service.MenuService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu controller
 */
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    @XarchLog(value = "Query menu list", type = "QUERY")
    public ApiResult<PageResult<Menu>> page(
            @RequestParam(required = false) String menuName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(menuService.page(menuName, pageNum, pageSize));
    }

    @GetMapping("/tree")
    public ApiResult<List<Menu>> tree() {
        return ApiResult.ok(menuService.tree());
    }

    @GetMapping("/{id}")
    public ApiResult<Menu> detail(@PathVariable Long id) {
        return ApiResult.ok(menuService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create menu", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Menu menu) {
        menuService.create(menu);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update menu", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Menu menu) {
        menu.setId(id);
        menuService.update(menu);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete menu", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<Menu>> options() {
        return ApiResult.ok(menuService.tree());
    }
}