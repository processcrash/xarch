package com.xarch.oa.exception;

import com.xarch.starter.core.exception.BusinessException;

/**
 * Domain exception for the OA example. Carries a fixed OA-specific
 * error code to keep client error handling straightforward.
 */
public class OaException extends BusinessException {

    private static final String DEFAULT_CODE = "3001";

    public OaException(String message) {
        super(DEFAULT_CODE, message);
    }

    public OaException(String code, String message) {
        super(code, message);
    }
}
