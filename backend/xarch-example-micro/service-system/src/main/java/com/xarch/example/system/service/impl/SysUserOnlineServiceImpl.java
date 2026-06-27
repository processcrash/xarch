package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.SysUserOnline;
import com.xarch.example.system.service.ISysUserOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stub online-user impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserOnlineServiceImpl implements ISysUserOnlineService {
    @Override public SysUserOnline selectOnlineByInfo(String i, String u, Object user) { return null; }
    @Override public SysUserOnline selectOnlineByIpaddr(String i, String u, Object user) { return null; }
    @Override public SysUserOnline selectOnlineByUserName(String u, Object user) { return null; }
    @Override public SysUserOnline loginUserToUserOnline(Object user) { return null; }
}