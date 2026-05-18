package com.xarch.web.autoconfigure;

import com.xarch.common.core.exception.BusinessException;
import com.xarch.common.core.exception.XarchException;
import com.xarch.common.core.result.ApiResult;
import com.xarch.common.core.result.ResultCode;
import com.xarch.common.core.util.ResultUtil;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler
 */
@AutoConfiguration
@RestControllerAdvice
public class XarchExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(XarchExceptionHandler.class);

    @ExceptionHandler(XarchException.class)
    public ApiResult<Void> handleXarchException(XarchException e) {
        log.error("XarchException: {}", e.getMessage(), e);
        return ResultUtil.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);
        return ResultUtil.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("ConstraintViolationException: {}", e.getMessage(), e);
        return ResultUtil.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResultUtil.fail(ResultCode.INTERNAL_ERROR);
    }
}