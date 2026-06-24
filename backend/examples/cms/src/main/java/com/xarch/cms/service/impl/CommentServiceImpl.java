package com.xarch.cms.service.impl;

import com.xarch.cms.dto.CommentDTO;
import com.xarch.cms.entity.Comment;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.CommentMapper;
import com.xarch.cms.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Comment service implementation.
 */
@Service
public class CommentServiceImpl implements CommentService {

    public static final String STATUS_VISIBLE = "VISIBLE";
    public static final String STATUS_HIDDEN = "HIDDEN";
    public static final String STATUS_PENDING = "PENDING";

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> listByArticle(Long articleId) {
        return commentMapper.selectByArticleId(articleId);
    }

    @Override
    public void create(CommentDTO dto, Long userId) {
        Comment comment = Comment.builder()
                .articleId(dto.articleId())
                .parentId(Objects.requireNonNullElse(dto.parentId(), 0L))
                .userId(userId)
                .content(dto.content())
                .status(STATUS_PENDING)
                .build();
        commentMapper.insert(comment);
    }

    @Override
    public void hide(Long id) {
        Comment comment = commentMapper.selectOneById(id);
        if (comment == null) {
            throw new CmsException("Comment not found: " + id);
        }
        comment.setStatus(STATUS_HIDDEN);
        commentMapper.update(comment);
    }

    @Override
    public void delete(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public long countByArticle(Long articleId) {
        return commentMapper.countByArticleId(articleId);
    }
}
