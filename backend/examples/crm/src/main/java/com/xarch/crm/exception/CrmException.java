package com.xarch.crm.exception;

import com.xarch.starter.core.exception.BusinessException;

/**
 * Domain exception for the CRM example.
 */
public class CrmException extends BusinessException {

    private static final String DEFAULT_CODE = "4001";

    public CrmException(String message) {
        super(DEFAULT_CODE, message);
    }

    public CrmException(String code, String message) {
        super(code, message);
    }
}
