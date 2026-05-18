package com.xarch.example.service;

import com.xarch.example.entity.SysJobLog;

import java.util.List;

/**
 * 定时任务调度日志信息 服务层
 */
public interface ISysJobLogService {
    /**
     * 获取quartz调度器日志的计划任务
     */
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 通过调度任务日志ID查询调度信息
     */
    SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增任务日志
     */
    void addJobLog(SysJobLog jobLog);

    /**
     * 批量删除调度日志信息
     */
    int deleteJobLogByIds(Long[] logIds);

    /**
     * 删除任务日志
     */
    int deleteJobLogById(Long jobId);

    /**
     * 清空任务日志
     */
    void cleanJobLog();
}