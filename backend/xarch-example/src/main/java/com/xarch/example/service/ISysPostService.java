package com.xarch.example.service;

import com.xarch.example.entity.SysPost;

import java.util.List;

/**
 * 岗位信息 服务层
 */
public interface ISysPostService {
    /**
     * 查询岗位信息集合
     */
    List<SysPost> selectPostList(SysPost post);

    /**
     * 查询所有岗位
     */
    List<SysPost> selectPostAll();

    /**
     * 通过岗位ID查询岗位信息
     */
    SysPost selectPostById(Long postId);

    /**
     * 根据用户ID获取岗位选择框列表
     */
    List<Long> selectPostListByUserId(Long userId);

    /**
     * 校验岗位名称
     */
    boolean checkPostNameUnique(SysPost post);

    /**
     * 校验岗位编码
     */
    boolean checkPostCodeUnique(SysPost post);

    /**
     * 通过岗位ID查询岗位使用数量
     */
    int countUserPostById(Long postId);

    /**
     * 删除岗位信息
     */
    int deletePostById(Long postId);

    /**
     * 批量删除岗位信息
     */
    int deletePostByIds(Long[] postIds);

    /**
     * 新增保存岗位信息
     */
    int insertPost(SysPost post);

    /**
     * 修改保存岗位信息
     */
    int updatePost(SysPost post);
}