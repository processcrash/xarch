package com.xarch.example.monitor.controller;

import com.xarch.example.monitor.entity.SysJobLog;
import com.xarch.example.monitor.service.ISysJobLogService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Job-log controller — migrated from {@code SysJobLogController}. */
@Tag(name = "Job Log")
@RestController
@RequestMapping("/monitor/jobLog")
@RequiredArgsConstructor
public class SysJobLogController {

    private final ISysJobLogService jobLogService;

    @GetMapping("/list")
    public PageResult<SysJobLog> list(SysJobLog jobLog) {
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        return PageResult.of(list, list.size());
    }

    @GetMapping(value = "/{jobLogId}")
    public ApiResult<SysJobLog> getInfo(@PathVariable("jobLogId") Long jobLogId) {
        return ApiResult.ok(jobLogService.selectJobLogById(jobLogId));
    }

    @DeleteMapping("/{jobLogIds}")
    public ApiResult<Void> remove(@PathVariable Long[] jobLogIds) {
        jobLogService.deleteJobLogByIds(jobLogIds);
        return ApiResult.ok();
    }

    @DeleteMapping("/clean")
    public ApiResult<Void> clean() {
        jobLogService.cleanJobLog();
        return ApiResult.ok();
    }
}