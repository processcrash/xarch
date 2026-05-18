package com.xarch.starter.core.util;

import cn.hutool.core.util.IdUtil;

import java.util.UUID;

/**
 * ID utility
 */
public class IdUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static long snowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    public static String snowflakeIdStr() {
        return String.valueOf(snowflakeId());
    }
}