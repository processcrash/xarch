package com.xarch.cms.service;

import com.xarch.cms.dto.CommentDTO;
import com.xarch.cms.entity.Comment;

import java.util.List;

/**
 * Comment business interface.
 */
public interface CommentService {

    /** List visible comments for an article, oldest first. */
    List<Comment> listByArticle(Long articleId);

    /** Create a new comment or reply. */
    void create(CommentDTO dto, Long userId);

    /** Hide a comment (soft transition to HIDDEN). */
    void hide(Long id);

    /** Soft delete a comment. */
    void delete(Long id);

    /** Count visible comments for an article. */
    long countByArticle(Long articleId);
}
