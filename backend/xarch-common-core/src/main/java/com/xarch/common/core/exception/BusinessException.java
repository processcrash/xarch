package com.xarch.common.core.exception;

/**
 * Exception for business logic errors
 */
public class BusinessException extends XarchException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super("400", message);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }

    public BusinessException(String message, Throwable cause) {
        super("400", message, cause);
    }
}