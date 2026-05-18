package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 调度任务日志信息 数据层
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
    /**
     * 获取quartz调度器日志的计划任务
     */
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 查询所有调度任务日志
     */
    List<SysJobLog> selectJobLogAll();

    /**
     * 通过调度任务日志ID查询调度信息
     */
    SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增任务日志
     */
    int insertJobLog(SysJobLog jobLog);

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