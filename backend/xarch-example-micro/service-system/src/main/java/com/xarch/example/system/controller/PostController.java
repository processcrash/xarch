package com.xarch.example.system.controller;

import com.xarch.example.system.entity.SysPost;
import com.xarch.example.system.service.ISysPostService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Post controller — migrated from {@code SysPostController}. */
@Tag(name = "Post")
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class PostController {

    private final ISysPostService postService;

    @GetMapping("/list")
    public PageResult<SysPost> list(SysPost post) {
        List<SysPost> list = postService.selectPostList(post);
        return PageResult.of(list, list.size());
    }

    @GetMapping("/all")
    public ApiResult<List<SysPost>> all() {
        return ApiResult.ok(postService.selectPostAll());
    }

    @GetMapping(value = "/{postId}")
    public ApiResult<SysPost> getInfo(@PathVariable("postId") Long postId) {
        return ApiResult.ok(postService.selectPostById(postId));
    }

    @PostMapping
    public ApiResult<Void> add(@RequestBody SysPost post) {
        postService.insertPost(post);
        return ApiResult.ok();
    }

    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysPost post) {
        postService.updatePost(post);
        return ApiResult.ok();
    }

    @DeleteMapping("/{postIds}")
    public ApiResult<Void> remove(@PathVariable Long[] postIds) {
        postService.deletePostByIds(postIds);
        return ApiResult.ok();
    }
}