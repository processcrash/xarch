package com.xarch.cms.controller;

import com.xarch.cms.dto.ArticleDTO;
import com.xarch.cms.dto.ArticleQuery;
import com.xarch.cms.entity.Article;
import com.xarch.cms.service.ArticleService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Article REST endpoints.
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * Paginated search.
     */
    @GetMapping
    @XarchLog(value = "Query articles", type = "QUERY")
    public ApiResult<PageResult<Article>> page(ArticleQuery query,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(articleService.page(query, pageNum, pageSize));
    }

    /**
     * Get a single article.
     */
    @GetMapping("/{id}")
    public ApiResult<Article> detail(@PathVariable Long id) {
        return ApiResult.ok(articleService.getById(id));
    }

    /**
     * Create a draft article. The author id is currently sourced from the
     * query string; in a real deployment this would come from the security context.
     */
    @PostMapping
    @XarchLog(value = "Create article", type = "CREATE")
    public ApiResult<Void> create(@RequestBody ArticleDTO dto,
                                  @RequestParam Long authorId) {
        articleService.create(dto, authorId);
        return ApiResult.ok();
    }

    /**
     * Update an existing article. Status is not touched.
     */
    @PutMapping("/{id}")
    @XarchLog(value = "Update article", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ArticleDTO dto) {
        articleService.update(id, dto);
        return ApiResult.ok();
    }

    /**
     * Move to PUBLISHED state.
     */
    @PutMapping("/{id}/publish")
    @XarchLog(value = "Publish article", type = "OPERATION")
    public ApiResult<Void> publish(@PathVariable Long id) {
        articleService.publish(id);
        return ApiResult.ok();
    }

    /**
     * Move to ARCHIVED state.
     */
    @PutMapping("/{id}/archive")
    @XarchLog(value = "Archive article", type = "OPERATION")
    public ApiResult<Void> archive(@PathVariable Long id) {
        articleService.archive(id);
        return ApiResult.ok();
    }

    /**
     * Soft delete.
     */
    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete article", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Increment the view counter.
     */
    @PostMapping("/{id}/view")
    public ApiResult<Long> view(@PathVariable Long id) {
        return ApiResult.ok(articleService.view(id));
    }

    /**
     * Increment the like counter.
     */
    @PostMapping("/{id}/like")
    public ApiResult<Long> like(@PathVariable Long id) {
        return ApiResult.ok(articleService.like(id));
    }

    /**
     * List articles by tag.
     */
    @GetMapping("/by-tag/{tagId}")
    public ApiResult<List<Article>> byTag(@PathVariable Long tagId) {
        return ApiResult.ok(articleService.listByTag(tagId));
    }

    /**
     * List articles by category.
     */
    @GetMapping("/by-category/{categoryId}")
    public ApiResult<List<Article>> byCategory(@PathVariable Long categoryId) {
        return ApiResult.ok(articleService.listByCategory(categoryId));
    }
}
