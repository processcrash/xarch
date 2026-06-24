package com.xarch.cms.mapper;

import com.xarch.cms.entity.ArticleTag;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Article-Tag join mapper. Exists so the article service can re-link tags
 * to an article without dragging a many-to-many through the parent.
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * List all tag ids for an article.
     */
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

    /**
     * List all article ids for a tag.
     */
    List<Long> selectArticleIdsByTagId(@Param("tagId") Long tagId);
}
