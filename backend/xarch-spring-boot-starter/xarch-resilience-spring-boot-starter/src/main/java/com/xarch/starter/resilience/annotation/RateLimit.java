package com.xarch.starter.resilience.annotation;

import com.xarch.starter.resilience.ResilienceProperties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as rate-limited.
 *
 * <p>By default the rate-limit bucket is keyed by the caller IP. Use
 * {@link #scope()} to switch to {@code USER} (requires a security
 * context) or {@code GLOBAL} (single bucket for the whole service).
 *
 * <p>Example:
 * <pre>{@code
 * @RateLimit(permitsPerSecond = 10, scope = RateLimit.Scope.USER)
 * public ApiResult<UserDTO> login(LoginRequest req) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Permits per second granted to the bucket. Values below 1 are
     * rejected at runtime.
     */
    int permitsPerSecond() default 100;

    /**
     * Maximum number of permits the bucket can hold during an idle burst.
     */
    int burstCapacity() default 100;

    /**
     * Scope of the rate-limit bucket.
     */
    ResilienceProperties.Scope scope() default ResilienceProperties.Scope.IP;

    /**
     * Maximum time the caller is willing to wait for a permit, in
     * milliseconds. {@code 0} means fail fast.
     */
    long timeoutMillis() default 0L;

    /**
     * Optional explicit key — useful when the same method should share a
     * bucket across callers (e.g. per-tenant). Ignored when empty.
     */
    String key() default "";
}
