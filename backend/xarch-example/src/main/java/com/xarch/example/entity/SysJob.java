package com.xarch.example.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.util.Date;

/**
 * 定时任务调度表 sys_job
 */
@Data
@Table("sys_job")
public class SysJob {

    /** 任务ID */
    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 任务组名 */
    private String jobGroup;

    /** 调用目标字符串 */
    private String invokeTarget;

    /** cron执行表达式 */
    private String cronExpression;

    /** cron计划策略 */
    private String misfirePolicy = "0";

    /** 是否并发执行（0允许 1禁止） */
    private String concurrent = "1";

    /** 任务状态（0正常 1暂停） */
    private String status;

    public Date getNextValidTime() {
        return null;
    }
}