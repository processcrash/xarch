package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysJobLog;
import com.xarch.example.service.ISysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 定时任务调度日志操作处理
 */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController {

    @Autowired
    private ISysJobLogService jobLogService;

    /**
     * 查询定时任务日志列表
     */
    @GetMapping("/list")
    public PageResult<List<SysJobLog>> list(SysJobLog jobLog) {
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        return PageResult.ok(list);
    }

    /**
     * 获取定时任务日志详细信息
     */
    @GetMapping(value = "/{jobLogId}")
    public ApiResult<SysJobLog> getInfo(@PathVariable("jobLogId") Long jobLogId) {
        return ApiResult.success(jobLogService.selectJobLogById(jobLogId));
    }

    /**
     * 删除定时任务日志
     */
    @DeleteMapping("/{jobLogIds}")
    public ApiResult<Void> remove(@PathVariable Long[] jobLogIds) {
        return ApiResult.success(jobLogService.deleteJobLogByIds(jobLogIds) > 0);
    }

    /**
     * 清空定时任务日志
     */
    @DeleteMapping("/clean")
    public ApiResult<Void> clean() {
        jobLogService.cleanJobLog();
        return ApiResult.success(true);
    }
}