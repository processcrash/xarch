package com.xarch.starter.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrap a method invocation with a Resilience4j circuit breaker.
 *
 * <p>The {@link #name()} is mandatory — it identifies the breaker in the
 * registry, the metrics and the actuator endpoint. The {@link #fallback()}
 * attribute references a sibling method that returns a degraded result
 * when the breaker is open.
 *
 * <p>Example:
 * <pre>{@code
 * @CircuitBreaker(name = "user-query", fallback = "fallbackUser")
 * public UserDTO getUser(Long id) { ... }
 *
 * @SuppressWarnings("unused")
 * private UserDTO fallbackUser(Long id, Throwable ex) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    /**
     * Breaker name. Must be unique within the registry.
     */
    String name();

    /**
     * Name of the fallback method. The method must live in the same
     * class, have the same return type and accept the original arguments
     * (plus an optional trailing {@link Throwable} for the cause).
     */
    String fallback() default "";

    /**
     * Override the sliding window size for this breaker. {@code 0} means
     * use the value from {@code xarch.resilience.circuit-breaker.sliding-window-size}.
     */
    int slidingWindowSize() default 0;

    /**
     * Override the failure rate threshold (percentage). {@code -1} means
     * use the value from the global config.
     */
    float failureRateThreshold() default -1.0f;

    /**
     * Override the wait time in the open state. {@code -1} means use the
     * value from the global config.
     */
    long waitDurationInOpenStateMillis() default -1L;
}
