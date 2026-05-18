package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 调度任务信息 数据层
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
    /**
     * 查询调度任务集合
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * 查询所有调度任务
     */
    List<SysJob> selectJobAll();

    /**
     * 通过调度ID查询调度任务信息
     */
    SysJob selectJobById(Long jobId);

    /**
     * 通过调度ID删除调度任务信息
     */
    int deleteJobById(Long jobId);

    /**
     * 批量删除调度任务信息
     */
    int deleteJobByIds(Long[] ids);

    /**
     * 修改调度任务信息
     */
    int updateJob(SysJob job);

    /**
     * 新增调度任务信息
     */
    int insertJob(SysJob job);
}