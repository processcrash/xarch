package com.xarch.cms.exception;

import com.xarch.starter.core.exception.BusinessException;

/**
 * Domain-specific exception for CMS errors. Wraps the framework's
 * {@link BusinessException} with a sensible default response code.
 */
public class CmsException extends BusinessException {

    private static final String DEFAULT_CODE = "2001";

    public CmsException(String message) {
        super(DEFAULT_CODE, message);
    }

    public CmsException(String code, String message) {
        super(code, message);
    }
}
