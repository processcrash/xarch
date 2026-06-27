package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.SysNotice;
import com.xarch.example.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub notice impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements ISysNoticeService {
    @Override public List<SysNotice> selectNoticeList(SysNotice n) { return List.of(); }
    @Override public SysNotice selectNoticeById(Long id) { return null; }
    @Override public void insertNotice(SysNotice n) { }
    @Override public void updateNotice(SysNotice n) { }
    @Override public void deleteNoticeByIds(Long[] ids) { }
}