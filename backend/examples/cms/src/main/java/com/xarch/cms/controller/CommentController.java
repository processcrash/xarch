package com.xarch.cms.controller;

import com.xarch.cms.dto.CommentDTO;
import com.xarch.cms.entity.Comment;
import com.xarch.cms.service.CommentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Comment REST endpoints. Replies are created with the same payload as
 * a top-level comment, the difference is the parentId.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * List comments for an article, oldest first.
     */
    @GetMapping("/article/{articleId}")
    public ApiResult<List<Comment>> list(@PathVariable Long articleId) {
        return ApiResult.ok(commentService.listByArticle(articleId));
    }

    /**
     * Create a new comment or reply.
     */
    @PostMapping
    @XarchLog(value = "Create comment", type = "CREATE")
    public ApiResult<Void> create(@RequestBody CommentDTO dto, @RequestParam Long userId) {
        commentService.create(dto, userId);
        return ApiResult.ok();
    }

    /**
     * Hide a comment.
     */
    @PutMapping("/{id}/hide")
    @XarchLog(value = "Hide comment", type = "OPERATION")
    public ApiResult<Void> hide(@PathVariable Long id) {
        commentService.hide(id);
        return ApiResult.ok();
    }

    /**
     * Soft delete a comment.
     */
    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete comment", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Count visible comments for an article.
     */
    @GetMapping("/article/{articleId}/count")
    public ApiResult<Long> count(@PathVariable Long articleId) {
        return ApiResult.ok(commentService.countByArticle(articleId));
    }
}
