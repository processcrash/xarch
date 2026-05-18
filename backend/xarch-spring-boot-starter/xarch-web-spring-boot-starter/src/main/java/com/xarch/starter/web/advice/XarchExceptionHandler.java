package com.xarch.starter.web.advice;

import com.xarch.starter.core.exception.BusinessException;
import com.xarch.starter.core.exception.XarchException;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.util.ResultUtil;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler
 */
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
        log.error("ConstraintViolationException: {}", e.getMessage());
        return ResultUtil.fail("4000", "Validation failed: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
            ? e.getBindingResult().getFieldError().getDefaultMessage()
            : "Validation failed";
        log.error("MethodArgumentNotValidException: {}", message);
        return ResultUtil.fail("4000", message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResultUtil.fail("5000", "Internal server error");
    }
}