package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;

/**
 * 通知公告表 sys_notice
 */
@Data
@Table("sys_notice")
public class SysNotice {

    /** 公告ID */
    @Id(auto = true)
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1通知 2公告） */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态（0正常 1关闭） */
    private String status;
}