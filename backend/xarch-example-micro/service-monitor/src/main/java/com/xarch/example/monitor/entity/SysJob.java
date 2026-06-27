package com.xarch.example.monitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

/** 定时任务调度表 owned by service-monitor. */
@Data
@Table("xarch_monitor_job")
public class SysJob {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String cronExpression;

    private String misfirePolicy = "0";

    private String concurrent = "1";

    private String status;

    public Date getNextValidTime() {
        return null;
    }
}