package com.xarch.example.monitor.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

/** 定时任务调度日志表 owned by service-monitor. */
@Data
@Table("xarch_monitor_job_log")
public class SysJobLog {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long jobLogId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String jobMessage;

    private String status;

    private String exceptionInfo;

    private Date startTime;

    private Date endTime;
}