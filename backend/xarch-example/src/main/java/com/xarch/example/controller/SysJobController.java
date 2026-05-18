package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysJob;
import com.xarch.example.service.ISysJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度任务信息操作处理
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController {

    @Autowired
    private ISysJobService jobService;

    /**
     * 查询定时任务列表
     */
    @GetMapping("/list")
    public PageResult<List<SysJob>> list(SysJob job) {
        List<SysJob> list = jobService.selectJobList(job);
        return PageResult.ok(list);
    }

    /**
     * 获取定时任务详细信息
     */
    @GetMapping(value = "/{jobId}")
    public ApiResult<SysJob> getInfo(@PathVariable("jobId") Long jobId) {
        return ApiResult.success(jobService.selectJobById(jobId));
    }

    /**
     * 新增定时任务
     */
    @PostMapping
    public ApiResult<Void> add(@RequestBody SysJob job) {
        return ApiResult.success(jobService.insertJob(job) > 0);
    }

    /**
     * 修改定时任务
     */
    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysJob job) {
        return ApiResult.success(jobService.updateJob(job) > 0);
    }

    /**
     * 定时任务状态修改
     */
    @PutMapping("/changeStatus")
    public ApiResult<Void> changeStatus(@RequestBody SysJob job) {
        return ApiResult.success(jobService.changeStatus(job) > 0);
    }

    /**
     * 定时任务立即执行一次
     */
    @PutMapping("/run")
    public ApiResult<Void> run(@RequestBody SysJob job) {
        return ApiResult.success(jobService.run(job));
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{jobIds}")
    public ApiResult<Void> remove(@PathVariable Long[] jobIds) {
        jobService.deleteJobByIds(jobIds);
        return ApiResult.success(true);
    }
}