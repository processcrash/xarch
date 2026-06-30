package com.xarch.starter.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrap a method invocation with an exponential-backoff retry policy.
 *
 * <p>Only retryable exceptions trigger a retry. By default every
 * {@link RuntimeException} is retryable. The annotation is intentionally
 * simple — for fine-grained control, register a custom
 * {@code RetryRegistry} with explicit {@code retryOnException} /
 * {@code ignoreExceptions} predicates.
 *
 * <p>Example:
 * <pre>{@code
 * @Retry(maxAttempts = 5, delay = 200, multiplier = 2.0)
 * public RemoteStatus callRemote() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {

    /**
     * Maximum number of attempts including the first call. {@code 1}
     * disables retrying.
     */
    int maxAttempts() default 3;

    /**
     * Initial delay in milliseconds between attempts.
     */
    long delay() default 200L;

    /**
     * Multiplier applied to {@link #delay()} on every subsequent attempt.
     */
    double multiplier() default 2.0;

    /**
     * Hard cap on the delay between attempts, in milliseconds.
     */
    long maxDelay() default 2_000L;
}
