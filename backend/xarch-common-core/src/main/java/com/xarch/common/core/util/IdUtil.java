package com.xarch.common.core.util;

import cn.hutool.crypto.SecureUtil;

import java.util.UUID;

/**
 * ID generation utilities
 */
public class IdUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String uuid36() {
        return UUID.randomUUID().toString();
    }

    public static String snowflake() {
        return String.valueOf(System.currentTimeMillis()) + String.format("%05d", (int) (Math.random() * 100000));
    }

    public static String md5(String input) {
        return SecureUtil.md5(input);
    }
}