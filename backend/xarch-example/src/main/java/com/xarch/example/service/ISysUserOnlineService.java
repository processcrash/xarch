package com.xarch.example.service;

import com.xarch.example.entity.SysUserOnline;

/**
 * 在线用户 服务层
 */
public interface ISysUserOnlineService {
    /**
     * 通过登录地址查询信息
     */
    SysUserOnline selectOnlineByIpaddr(String ipaddr, String userName, Object user);

    /**
     * 通过用户名称查询信息
     */
    SysUserOnline selectOnlineByUserName(String userName, Object user);

    /**
     * 通过登录地址/用户名称查询信息
     */
    SysUserOnline selectOnlineByInfo(String ipaddr, String userName, Object user);

    /**
     * 设置在线用户信息
     */
    SysUserOnline loginUserToUserOnline(Object user);
}