package com.xarch.cms.controller;

import com.xarch.cms.entity.Tag;
import com.xarch.cms.service.TagService;
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
 * Tag REST endpoints. The {@code /options} endpoint is used to populate
 * selects / transfer components in the admin UI.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ApiResult<List<Tag>> list() {
        return ApiResult.ok(tagService.list());
    }

    @GetMapping("/options")
    public ApiResult<List<Tag>> options() {
        return ApiResult.ok(tagService.list());
    }

    @GetMapping("/{id}")
    public ApiResult<Tag> detail(@PathVariable Long id) {
        return ApiResult.ok(tagService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create tag", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Tag tag) {
        tagService.create(tag);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update tag", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagService.update(tag);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete tag", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ApiResult.ok();
    }
}
