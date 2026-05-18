package com.xarch.example.service;

import com.xarch.example.entity.SysNotice;

import java.util.List;

/**
 * 公告 服务层
 */
public interface ISysNoticeService {
    /**
     * 查询公告信息
     */
    SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     */
    List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     */
    int insertNotice(SysNotice notice);

    /**
     * 修改公告
     */
    int updateNotice(SysNotice notice);

    /**
     * 删除公告信息
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     */
    int deleteNoticeByIds(Long[] noticeIds);
}