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
    public PageResult<SysJob> list(SysJob job) {
        List<SysJob> list = jobService.selectJobList(job);
        return PageResult.of(list, list.size());
    }

    /**
     * 获取定时任务详细信息
     */
    @GetMapping(value = "/{jobId}")
    public ApiResult<SysJob> getInfo(@PathVariable("jobId") Long jobId) {
        return ApiResult.ok(jobService.selectJobById(jobId));
    }

    /**
     * 新增定时任务
     */
    @PostMapping
    public ApiResult<Void> add(@RequestBody SysJob job) {
        jobService.insertJob(job);
        return ApiResult.ok();
    }

    /**
     * 修改定时任务
     */
    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysJob job) {
        jobService.updateJob(job);
        return ApiResult.ok();
    }

    /**
     * 定时任务状态修改
     */
    @PutMapping("/changeStatus")
    public ApiResult<Void> changeStatus(@RequestBody SysJob job) {
        jobService.changeStatus(job);
        return ApiResult.ok();
    }

    /**
     * 定时任务立即执行一次
     */
    @PutMapping("/run")
    public ApiResult<Void> run(@RequestBody SysJob job) {
        jobService.run(job);
        return ApiResult.ok();
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{jobIds}")
    public ApiResult<Void> remove(@PathVariable Long[] jobIds) {
        jobService.deleteJobByIds(jobIds);
        return ApiResult.ok();
    }
}