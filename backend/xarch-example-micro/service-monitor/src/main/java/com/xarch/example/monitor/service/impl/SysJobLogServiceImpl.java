package com.xarch.example.monitor.service.impl;

import com.xarch.example.monitor.entity.SysJobLog;
import com.xarch.example.monitor.service.ISysJobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub job-log impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl implements ISysJobLogService {
    @Override public List<SysJobLog> selectJobLogList(SysJobLog j) { return List.of(); }
    @Override public SysJobLog selectJobLogById(Long id) { return null; }
    @Override public void deleteJobLogByIds(Long[] ids) { }
    @Override public void cleanJobLog() { }
}