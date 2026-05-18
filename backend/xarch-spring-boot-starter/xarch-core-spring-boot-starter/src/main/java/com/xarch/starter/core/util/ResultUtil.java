package com.xarch.starter.core.util;

import com.xarch.starter.core.enums.ResponseCode;
import com.xarch.starter.core.exception.BusinessException;
import com.xarch.starter.core.exception.XarchException;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * Result utility
 */
public class ResultUtil {

    public static <T> ApiResult<T> ok() {
        return ApiResult.ok();
    }

    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.ok(data);
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return ApiResult.fail(code, message);
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode) {
        return ApiResult.fail(responseCode.getCode(), responseCode.getMessage());
    }

    public static <T> ApiResult<T> fail(BusinessException e) {
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    public static <T> ApiResult<T> fail(XarchException e) {
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    public static <T> PageResult<T> page(List<T> list, long total) {
        return PageResult.of(list, total);
    }

    public static void throwFail(String code, String message) {
        throw new XarchException(code, message);
    }

    public static void throwBizFail(String code, String message) {
        throw new BusinessException(code, message);
    }
}