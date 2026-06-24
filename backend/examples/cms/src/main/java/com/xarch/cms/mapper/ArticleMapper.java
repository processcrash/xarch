package com.xarch.cms.mapper;

import com.xarch.cms.entity.Article;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Article mapper. Most CRUD is covered by {@link BaseMapper}; only
 * complex joins live here.
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * Find articles by tag id.
     */
    List<Article> selectByTagId(@Param("tagId") Long tagId);

    /**
     * Find articles by category id, newest first.
     */
    List<Article> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Increment the view counter atomically.
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * Increment the like counter atomically.
     */
    int incrementLikeCount(@Param("id") Long id);
}
