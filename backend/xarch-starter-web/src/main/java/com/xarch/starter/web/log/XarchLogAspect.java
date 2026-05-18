package com.xarch.starter.web.log;

import com.xarch.starter.core.annotation.XarchLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logging aspect for @XarchLog
 */
@Aspect
@Component
public class XarchLogAspect {

    private static final Logger log = LoggerFactory.getLogger(XarchLogAspect.class);

    @Around("@annotation(com.xarch.starter.core.annotation.XarchLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) point.getSignature();
        var method = signature.getMethod();
        XarchLog xarchLog = method.getAnnotation(XarchLog.class);

        String className = point.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String operation = xarchLog.value();
        String type = xarchLog.type();

        log.info("[{}] {}.{} - {}", type, className, methodName, operation);

        Object result = point.proceed();

        long costTime = System.currentTimeMillis() - startTime;
        log.info("[{}] {}.{} completed in {}ms", type, className, methodName, costTime);

        return result;
    }
}