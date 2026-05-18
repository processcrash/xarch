package com.xarch.common.core.util;

import com.xarch.common.core.result.ApiResult;
import com.xarch.common.core.result.ResultCode;

/**
 * Result utilities
 */
public class ResultUtil {

    public static <T> ApiResult<T> ok() {
        return ApiResult.success();
    }

    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.success(data);
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return ApiResult.success(message, data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return ApiResult.error(message);
    }

    public static <T> ApiResult<T> fail(ResultCode resultCode) {
        return ApiResult.error(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return ApiResult.error(code, message);
    }
}