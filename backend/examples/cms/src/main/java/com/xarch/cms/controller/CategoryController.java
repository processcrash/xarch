package com.xarch.cms.controller;

import com.xarch.cms.entity.Category;
import com.xarch.cms.service.CategoryService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Category REST endpoints. Exposes both the flat list and a nested tree.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ApiResult<List<Category>> list() {
        return ApiResult.ok(categoryService.list());
    }

    @GetMapping("/tree")
    public ApiResult<List<Category>> tree() {
        return ApiResult.ok(categoryService.tree());
    }

    @GetMapping("/{id}")
    public ApiResult<Category> detail(@PathVariable Long id) {
        return ApiResult.ok(categoryService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create category", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Category category) {
        categoryService.create(category);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update category", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.update(category);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete category", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResult.ok();
    }
}
