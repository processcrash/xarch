package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位信息 数据层
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    /**
     * 查询岗位数据集合
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
     * 查询用户所属岗位组
     */
    List<SysPost> selectPostsByUserName(String userName);

    /**
     * 删除岗位信息
     */
    int deletePostById(Long postId);

    /**
     * 批量删除岗位信息
     */
    int deletePostByIds(Long[] postIds);

    /**
     * 修改岗位信息
     */
    int updatePost(SysPost post);

    /**
     * 新增岗位信息
     */
    int insertPost(SysPost post);

    /**
     * 校验岗位名称
     */
    SysPost checkPostNameUnique(String postName);

    /**
     * 校验岗位编码
     */
    SysPost checkPostCodeUnique(String postCode);
}