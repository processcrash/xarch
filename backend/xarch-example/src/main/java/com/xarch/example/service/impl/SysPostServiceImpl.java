package com.xarch.example.service.impl;

import com.xarch.example.entity.SysPost;
import com.xarch.example.mapper.SysPostMapper;
import com.xarch.example.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 岗位信息 服务层处理
 */
@Service
public class SysPostServiceImpl implements ISysPostService {
    @Autowired
    private SysPostMapper postMapper;

    @Override
    public List<SysPost> selectPostList(SysPost post) {
        return postMapper.selectPostList(post);
    }

    @Override
    public List<SysPost> selectPostAll() {
        return postMapper.selectPostAll();
    }

    @Override
    public SysPost selectPostById(Long postId) {
        return postMapper.selectPostById(postId);
    }

    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        return postMapper.selectPostListByUserId(userId);
    }

    @Override
    public boolean checkPostNameUnique(SysPost post) {
        Long postId = StringUtils.isEmpty(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostNameUnique(post.getPostName());
        if (info != null && !info.getPostId().equals(postId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkPostCodeUnique(SysPost post) {
        Long postId = StringUtils.isEmpty(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostCodeUnique(post.getPostCode());
        if (info != null && !info.getPostId().equals(postId)) {
            return false;
        }
        return true;
    }

    @Override
    public int countUserPostById(Long postId) {
        return 0;
    }

    @Override
    public int deletePostById(Long postId) {
        return postMapper.deletePostById(postId);
    }

    @Override
    public int deletePostByIds(Long[] postIds) {
        return postMapper.deletePostByIds(postIds);
    }

    @Override
    public int insertPost(SysPost post) {
        return postMapper.insertPost(post);
    }

    @Override
    public int updatePost(SysPost post) {
        return postMapper.updatePost(post);
    }
}