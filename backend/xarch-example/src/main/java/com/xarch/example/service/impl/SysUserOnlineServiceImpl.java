package com.xarch.example.service.impl;

import com.xarch.example.entity.SysUserOnline;
import com.xarch.example.service.ISysUserOnlineService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * 在线用户 服务层处理
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService {

    @Override
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, String userName, Object user) {
        try {
            Field ipaddrField = user.getClass().getDeclaredField("ipaddr");
            ipaddrField.setAccessible(true);
            String userIpaddr = (String) ipaddrField.get(user);
            if (ipaddr.equals(userIpaddr)) {
                return loginUserToUserOnline(user);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    @Override
    public SysUserOnline selectOnlineByUserName(String userName, Object user) {
        try {
            Field usernameField = user.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            String userNameStr = (String) usernameField.get(user);
            if (userName.equals(userNameStr)) {
                return loginUserToUserOnline(user);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    @Override
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, Object user) {
        try {
            Field ipaddrField = user.getClass().getDeclaredField("ipaddr");
            ipaddrField.setAccessible(true);
            String userIpaddr = (String) ipaddrField.get(user);

            Field usernameField = user.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            String userNameStr = (String) usernameField.get(user);

            if (ipaddr.equals(userIpaddr) && userName.equals(userNameStr)) {
                return loginUserToUserOnline(user);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    @Override
    public SysUserOnline loginUserToUserOnline(Object user) {
        if (user == null) {
            return null;
        }
        SysUserOnline sysUserOnline = new SysUserOnline();
        try {
            Field tokenField = user.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            sysUserOnline.setTokenId((String) tokenField.get(user));

            Field usernameField = user.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            sysUserOnline.setUserName((String) usernameField.get(user));

            Field ipaddrField = user.getClass().getDeclaredField("ipaddr");
            ipaddrField.setAccessible(true);
            sysUserOnline.setIpaddr((String) ipaddrField.get(user));

            Field loginLocationField = user.getClass().getDeclaredField("loginLocation");
            loginLocationField.setAccessible(true);
            sysUserOnline.setLoginLocation((String) loginLocationField.get(user));

            Field browserField = user.getClass().getDeclaredField("browser");
            browserField.setAccessible(true);
            sysUserOnline.setBrowser((String) browserField.get(user));

            Field osField = user.getClass().getDeclaredField("os");
            osField.setAccessible(true);
            sysUserOnline.setOs((String) osField.get(user));

            Field loginTimeField = user.getClass().getDeclaredField("loginTime");
            loginTimeField.setAccessible(true);
            Object loginTime = loginTimeField.get(user);
            if (loginTime instanceof Long) {
                sysUserOnline.setLoginTime((Long) loginTime);
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return sysUserOnline;
    }
}