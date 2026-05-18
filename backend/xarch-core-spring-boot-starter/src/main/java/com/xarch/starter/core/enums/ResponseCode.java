package com.xarch.starter.core.enums;

import lombok.Getter;

/**
 * Response code enum
 */
@Getter
public enum ResponseCode {

    SUCCESS("0000", "SUCCESS"),
    BAD_REQUEST("4000", "Bad Request"),
    UNAUTHORIZED("4010", "Unauthorized"),
    FORBIDDEN("4030", "Forbidden"),
    NOT_FOUND("4040", "Not Found"),
    INTERNAL_ERROR("5000", "Internal Server Error");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}