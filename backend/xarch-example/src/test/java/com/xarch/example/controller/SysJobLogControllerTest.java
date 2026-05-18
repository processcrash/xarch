package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import com.xarch.example.entity.SysJobLog;
import com.xarch.example.service.ISysJobLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysJobLogController unit tests
 */
@SpringBootTest
class SysJobLogControllerTest {

    @Autowired
    private SysJobLogController jobLogController;

    @Autowired
    private ISysJobLogService jobLogService;

    @Test
    void testList() {
        var result = jobLogController.list(new SysJobLog());
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testGetInfo() {
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName("Test Job");
        jobLog.setJobGroup("DEFAULT");
        jobLog.setInvokeTarget("testTask.run");
        jobLog.setJobMessage("Test execution");
        jobLog.setStatus("0");
        jobLogService.addJobLog(jobLog);

        var result = jobLogController.getInfo(jobLog.getJobLogId());
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testClean() {
        var result = jobLogController.clean();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}