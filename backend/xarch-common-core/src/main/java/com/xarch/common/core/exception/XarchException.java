package com.xarch.common.core.exception;

/**
 * Base exception for xarch framework
 */
public class XarchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code;

    public XarchException(String message) {
        super(message);
        this.code = "500";
    }

    public XarchException(String code, String message) {
        super(message);
        this.code = code;
    }

    public XarchException(String message, Throwable cause) {
        super(message, cause);
        this.code = "500";
    }

    public XarchException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}