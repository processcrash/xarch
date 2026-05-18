package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysPost;
import com.xarch.example.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位信息操作处理
 */
@RestController
@RequestMapping("/system/post")
public class SysPostController {

    @Autowired
    private ISysPostService postService;

    /**
     * 查询岗位列表
     */
    @GetMapping("/list")
    public PageResult<List<SysPost>> list(SysPost post) {
        List<SysPost> list = postService.selectPostList(post);
        return PageResult.ok(list);
    }

    /**
     * 查询所有岗位
     */
    @GetMapping("/all")
    public ApiResult<List<SysPost>> all() {
        return ApiResult.success(postService.selectPostAll());
    }

    /**
     * 获取岗位详细信息
     */
    @GetMapping(value = "/{postId}")
    public ApiResult<SysPost> getInfo(@PathVariable("postId") Long postId) {
        return ApiResult.success(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     */
    @PostMapping
    public ApiResult<Void> add(@RequestBody SysPost post) {
        return ApiResult.success(postService.insertPost(post) > 0);
    }

    /**
     * 修改岗位
     */
    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysPost post) {
        return ApiResult.success(postService.updatePost(post) > 0);
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{postIds}")
    public ApiResult<Void> remove(@PathVariable Long[] postIds) {
        return ApiResult.success(postService.deletePostByIds(postIds) > 0);
    }
}