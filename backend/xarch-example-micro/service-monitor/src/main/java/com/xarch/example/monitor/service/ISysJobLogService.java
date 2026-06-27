package com.xarch.example.monitor.service;

import com.xarch.example.monitor.entity.SysJobLog;

import java.util.List;

/** Job-log service contract. */
public interface ISysJobLogService {
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);
    SysJobLog selectJobLogById(Long jobLogId);
    void deleteJobLogByIds(Long[] jobLogIds);
    void cleanJobLog();
}