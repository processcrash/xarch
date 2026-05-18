package com.xarch.example.service;

import com.xarch.example.entity.SysJob;

import java.util.List;

/**
 * 定时任务调度信息 服务层
 */
public interface ISysJobService {
    /**
     * 获取quartz调度器的计划任务
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * 通过调度任务ID查询调度信息
     */
    SysJob selectJobById(Long jobId);

    /**
     * 暂停任务
     */
    int pauseJob(SysJob job);

    /**
     * 恢复任务
     */
    int resumeJob(SysJob job);

    /**
     * 删除任务
     */
    int deleteJob(SysJob job);

    /**
     * 批量删除调度信息
     */
    void deleteJobByIds(Long[] jobIds);

    /**
     * 任务调度状态修改
     */
    int changeStatus(SysJob job);

    /**
     * 立即运行任务
     */
    boolean run(SysJob job);

    /**
     * 新增任务
     */
    int insertJob(SysJob job);

    /**
     * 更新任务
     */
    int updateJob(SysJob job);

    /**
     * 校验cron表达式是否有效
     */
    boolean checkCronExpressionIsValid(String cronExpression);
}