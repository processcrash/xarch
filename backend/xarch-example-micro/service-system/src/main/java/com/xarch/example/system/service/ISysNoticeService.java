package com.xarch.example.system.service;

import com.xarch.example.system.entity.SysNotice;

import java.util.List;

/** Notice service contract — Chinese-style methods from monolith. */
public interface ISysNoticeService {
    List<SysNotice> selectNoticeList(SysNotice notice);
    SysNotice selectNoticeById(Long noticeId);
    void insertNotice(SysNotice notice);
    void updateNotice(SysNotice notice);
    void deleteNoticeByIds(Long[] noticeIds);
}