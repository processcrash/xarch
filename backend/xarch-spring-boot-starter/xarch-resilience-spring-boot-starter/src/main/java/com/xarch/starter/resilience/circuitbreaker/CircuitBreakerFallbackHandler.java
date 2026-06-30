package com.xarch.starter.resilience.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Default fallback handler invoked when a circuit breaker is open or its
 * invocation throws.
 *
 * <p>Returns a degraded response in the same shape the caller expects
 * (typically {@code ApiResult.fail}) — using reflection so the starter
 * does not need a hard dependency on the xarch web module.
 */
public class CircuitBreakerFallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFallbackHandler.class);

    /**
     * Build a fallback value for the given return type. Supports
     * {@code ApiResult} (reflectively) and {@code Optional}. For any
     * other type, returns {@code null}.
     */
    public Object defaultFallback(Class<?> returnType, Throwable cause) {
        if (returnType == null) {
            return null;
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        try {
            // Reflectively call ApiResult.fail(code, message) when the
            // xarch-core ApiResult class is on the classpath.
            Class<?> apiResultClass = Class.forName("com.xarch.starter.core.api.ApiResult");
            Object instance = apiResultClass.getMethod("fail", Integer.TYPE, String.class)
                    .invoke(null, 503, degradedMessage(cause));
            if (returnType.isAssignableFrom(instance.getClass())) {
                return instance;
            }
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // ApiResult not on classpath — return null and let the caller
            // decide. log at debug to avoid noise on production.
            log.debug("xarch fallback: ApiResult not on classpath, returning null");
        } catch (Exception ex) {
            log.warn("xarch fallback: ApiResult.fail() failed: {}", ex.getMessage());
        }
        return null;
    }

    private static String degradedMessage(Throwable cause) {
        if (cause == null || cause.getMessage() == null) {
            return "Service degraded, please retry";
        }
        return "Service degraded: " + cause.getMessage();
    }
}
