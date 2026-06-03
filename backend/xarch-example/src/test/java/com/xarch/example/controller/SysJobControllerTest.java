package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysJob;
import com.xarch.example.service.ISysJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysJobController unit tests
 */
@SpringBootTest
class SysJobControllerTest {

    @Autowired
    private SysJobController jobController;

    @Autowired
    private ISysJobService jobService;

    @Test
    void testList() {
        PageResult<SysJob> result = jobController.list(new SysJob());
        assertNotNull(result);
    }

    @Test
    void testGetInfo() {
        SysJob job = new SysJob();
        job.setJobName("Test Job");
        job.setJobGroup("DEFAULT");
        job.setInvokeTarget("testTask.run");
        job.setCronExpression("0/30 * * * * ?");
        job.setStatus("0");
        jobService.insertJob(job);

        var result = jobController.getInfo(job.getJobId());
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testAdd() {
        SysJob job = new SysJob();
        job.setJobName("New Test Job");
        job.setJobGroup("DEFAULT");
        job.setInvokeTarget("newTask.run");
        job.setCronExpression("0/15 * * * * ?");
        job.setStatus("1");
        job.setConcurrent("1");

        var result = jobController.add(job);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testChangeStatus() {
        SysJob job = new SysJob();
        job.setJobName("Status Test Job");
        job.setJobGroup("DEFAULT");
        job.setInvokeTarget("statusTask.run");
        job.setCronExpression("0/20 * * * * ?");
        job.setStatus("0");
        jobService.insertJob(job);

        job.setStatus("1");
        var result = jobController.changeStatus(job);
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}