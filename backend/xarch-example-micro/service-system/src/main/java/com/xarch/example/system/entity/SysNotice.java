package com.xarch.example.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 通知公告表 owned by {@code service-system}.
 */
@Data
@Table("xarch_system_notice")
public class SysNotice {

    /** 公告ID */
    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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