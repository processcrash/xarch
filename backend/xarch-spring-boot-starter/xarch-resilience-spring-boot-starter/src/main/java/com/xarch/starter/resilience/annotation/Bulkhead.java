package com.xarch.starter.resilience.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Concurrency cap for a method. Acts as a thread-pool style bulkhead —
 * protects the calling thread by bounding how many parallel invocations
 * can be in-flight at any one time.
 *
 * <p>Example:
 * <pre>{@code
 * @Bulkhead(concurrentCalls = 10, maxWait = 500)
 * public ApiResult<BigReport> generateReport() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bulkhead {

    /**
     * Maximum concurrent invocations. {@code 0} falls back to the global
     * {@code xarch.resilience.bulkhead.concurrent-calls} property.
     */
    int concurrentCalls() default 0;

    /**
     * Maximum wait time for a permit, in milliseconds. {@code -1} means
     * fail immediately when the bulkhead is full.
     */
    long maxWait() default -1L;

    /**
     * Logical bulkhead name. Defaults to the fully qualified method name.
     */
    String name() default "";
}
