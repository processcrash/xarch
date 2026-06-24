package com.xarch.cms.dto;

/**
 * Article search parameters.
 */
public record ArticleQuery(
        String title,
        Long categoryId,
        Long tagId,
        String status,
        Long authorId
) {
}
