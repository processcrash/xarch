package com.xarch.cms.mapper;

import com.xarch.cms.entity.Comment;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Comment mapper. Adds helpers for the reply tree.
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * List visible comments for an article, ordered oldest first so that
     * a client can build a conversation thread top-down.
     */
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    /**
     * Count comments for an article, used to display counts in the UI.
     */
    long countByArticleId(@Param("articleId") Long articleId);
}
