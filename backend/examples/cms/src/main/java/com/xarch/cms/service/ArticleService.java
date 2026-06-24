package com.xarch.cms.service;

import com.xarch.cms.dto.ArticleDTO;
import com.xarch.cms.dto.ArticleQuery;
import com.xarch.cms.entity.Article;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * Article business interface.
 */
public interface ArticleService {

    /**
     * Paginated search. Any field in {@code query} can be null; nulls are ignored.
     */
    PageResult<Article> page(ArticleQuery query, int pageNum, int pageSize);

    /**
     * Get an article by id. Returns null if not found.
     */
    Article getById(Long id);

    /**
     * Create a new article. Always starts in {@code DRAFT} state.
     */
    void create(ArticleDTO dto, Long authorId);

    /**
     * Update title / content / summary / category. Does not touch status.
     */
    void update(Long id, ArticleDTO dto);

    /**
     * Move an article to {@code PUBLISHED} and stamp {@code publishedTime}.
     */
    void publish(Long id);

    /**
     * Move an article to {@code ARCHIVED}.
     */
    void archive(Long id);

    /**
     * Soft delete an article.
     */
    void delete(Long id);

    /**
     * Atomically increment the view counter and return the new value.
     */
    long view(Long id);

    /**
     * Atomically increment the like counter and return the new value.
     */
    long like(Long id);

    /**
     * Return all articles in the given tag, newest first.
     */
    List<Article> listByTag(Long tagId);

    /**
     * Return all articles in the given category, newest first.
     */
    List<Article> listByCategory(Long categoryId);
}
