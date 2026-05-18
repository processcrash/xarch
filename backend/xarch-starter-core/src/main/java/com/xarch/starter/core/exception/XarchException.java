package com.xarch.starter.core.exception;

import lombok.Getter;

/**
 * Base exception for xarch
 */
@Getter
public class XarchException extends RuntimeException {

    private final String code;

    public XarchException(String code, String message) {
        super(message);
        this.code = code;
    }

    public XarchException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}