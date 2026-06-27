package com.xarch.example.monitor.controller;

import com.xarch.example.monitor.entity.SysJob;
import com.xarch.example.monitor.service.ISysJobService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Scheduled job controller — migrated from {@code SysJobController}. */
@Tag(name = "Scheduled Jobs")
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class JobController {

    private final ISysJobService jobService;

    @GetMapping("/list")
    public PageResult<SysJob> list(SysJob job) {
        List<SysJob> list = jobService.selectJobList(job);
        return PageResult.of(list, list.size());
    }

    @GetMapping(value = "/{jobId}")
    public ApiResult<SysJob> getInfo(@PathVariable("jobId") Long jobId) {
        return ApiResult.ok(jobService.selectJobById(jobId));
    }

    @PostMapping
    public ApiResult<Void> add(@RequestBody SysJob job) {
        jobService.insertJob(job);
        return ApiResult.ok();
    }

    @PutMapping
    public ApiResult<Void> edit(@RequestBody SysJob job) {
        jobService.updateJob(job);
        return ApiResult.ok();
    }

    @PutMapping("/changeStatus")
    public ApiResult<Void> changeStatus(@RequestBody SysJob job) {
        jobService.changeStatus(job);
        return ApiResult.ok();
    }

    @PutMapping("/run")
    public ApiResult<Void> run(@RequestBody SysJob job) {
        jobService.run(job);
        return ApiResult.ok();
    }

    @DeleteMapping("/{jobIds}")
    public ApiResult<Void> remove(@PathVariable Long[] jobIds) {
        jobService.deleteJobByIds(jobIds);
        return ApiResult.ok();
    }
}