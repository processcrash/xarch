package com.xarch.common.core.result;

import java.io.Serializable;

/**
 * Unified API response wrapper
 *
 * @author xarch
 * @since 1.0.0
 */
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private T data;
    private long timestamp;

    public ApiResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResult(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>("200", "success", null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>("200", "success", data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>("200", message, data);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>("500", message, null);
    }

    public static <T> ApiResult<T> error(String code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public static <T> ApiResult<T> of(String code, String message, T data) {
        return new ApiResult<>(code, message, data);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}