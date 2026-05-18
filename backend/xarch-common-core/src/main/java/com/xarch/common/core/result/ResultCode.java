package com.xarch.common.core.result;

/**
 * Common result codes
 */
public enum ResultCode {

    SUCCESS("200", "success"),
    BAD_REQUEST("400", "bad request"),
    UNAUTHORIZED("401", "unauthorized"),
    FORBIDDEN("403", "forbidden"),
    NOT_FOUND("404", "not found"),
    INTERNAL_ERROR("500", "internal server error"),
    SERVICE_UNAVAILABLE("503", "service unavailable");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}