package com.xarch.example.system.service;

import com.xarch.example.system.entity.SysUserOnline;

/** Online-user service contract. */
public interface ISysUserOnlineService {
    SysUserOnline selectOnlineByInfo(String ipaddr, String userName, Object user);
    SysUserOnline selectOnlineByIpaddr(String ipaddr, String userName, Object user);
    SysUserOnline selectOnlineByUserName(String userName, Object user);
    SysUserOnline loginUserToUserOnline(Object user);
}