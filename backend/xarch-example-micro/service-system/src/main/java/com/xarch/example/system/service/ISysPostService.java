package com.xarch.example.system.service;

import com.xarch.example.system.entity.SysPost;

import java.util.List;

/** Post service contract. */
public interface ISysPostService {
    List<SysPost> selectPostList(SysPost post);
    List<SysPost> selectPostAll();
    SysPost selectPostById(Long postId);
    void insertPost(SysPost post);
    void updatePost(SysPost post);
    void deletePostByIds(Long[] postIds);
}