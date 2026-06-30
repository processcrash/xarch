package com.xarch.example.controller;

import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.resilience.annotation.Bulkhead;
import com.xarch.starter.resilience.annotation.CircuitBreaker;
import com.xarch.starter.resilience.annotation.RateLimit;
import com.xarch.starter.resilience.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Demo controller showcasing every annotation exposed by the
 * {@code xarch-resilience-spring-boot-starter}.
 *
 * <p>Mounted under {@code /api/demo/resilience} — it does not interact
 * with any business state, so it is safe to keep enabled in every
 * environment. The companion {@code application-resilience.yml} shows
 * how to tune each pattern.
 */
@RestController
@RequestMapping("/api/demo/resilience")
@Tag(name = "Resilience demo", description = "Endpoints exercising the xarch resilience annotations")
public class ResilienceDemoController {

    /**
     * Demonstrate the {@link RateLimit} annotation. Caps the bucket at
     * 10 permits per second per IP — the 11th call within a 1 s window
     * fails with HTTP 429.
     */
    @GetMapping("/rate-limit")
    @RateLimit(permitsPerSecond = 10, scope = RateLimit.Scope.IP)
    @Operation(summary = "Rate-limited ping (10 pps per IP)")
    public ApiResult<String> rateLimited() {
        return ApiResult.ok("ok");
    }

    /**
     * Demonstrate the {@link CircuitBreaker} annotation. Every call
     * has a 50% chance of failing — when the failure rate exceeds 50%
     * the breaker opens and {@code fallbackUnreliable} is invoked.
     */
    @GetMapping("/unreliable")
    @CircuitBreaker(name = "demo-unreliable", fallback = "fallbackUnreliable")
    @Operation(summary = "Random-failure endpoint protected by a circuit breaker")
    public ApiResult<String> unreliable() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new IllegalStateException("simulated downstream failure");
        }
        return ApiResult.ok("lucky");
    }

    /**
     * Fallback for {@link #unreliable()}. Returns a degraded response
     * without propagating the exception.
     */
    @SuppressWarnings("unused")
    private ApiResult<String> fallbackUnreliable(Throwable cause) {
        return ApiResult.fail(503, "downstream is degraded: "
                + (cause == null ? "n/a" : cause.getMessage()));
    }

    /**
     * Demonstrate the {@link Retry} annotation. Always fails on the
     * first 2 attempts and succeeds on the 3rd — the 200 ms exponential
     * backoff gives the simulated downstream time to recover.
     */
    @GetMapping("/flaky")
    @Retry(maxAttempts = 3, delay = 200, multiplier = 2.0)
    @Operation(summary = "Always-succeeds-eventually endpoint protected by retry")
    public ApiResult<Integer> flaky(@RequestParam(defaultValue = "0") int calls) {
        int attempt = calls + 1;
        if (attempt < 3) {
            throw new IllegalStateException("transient " + attempt);
        }
        return ApiResult.ok(attempt);
    }

    /**
     * Demonstrate the {@link Bulkhead} annotation. Allows at most 2
     * concurrent invocations. The third concurrent caller is rejected
     * with {@code BulkheadFullException}.
     */
    @GetMapping("/slow")
    @Bulkhead(name = "demo-slow", concurrentCalls = 2, maxWait = 0)
    @Operation(summary = "Slow endpoint (1s) protected by a 2-slot bulkhead")
    public ApiResult<String> slow() throws InterruptedException {
        Thread.sleep(1_000L);
        return ApiResult.ok("done");
    }

    /**
     * Demonstrate the {@link com.xarch.starter.resilience.annotation.CircuitBreaker}
     * annotation with a per-method read fallback that returns an
     * empty result rather than failing the call.
     */
    @GetMapping("/user")
    @CircuitBreaker(name = "user-query", fallback = "fallbackUser")
    @Operation(summary = "Read a user; returns empty result on open breaker")
    public ApiResult<String> getUser(@RequestParam Long id) {
        if (id < 0) {
            throw new IllegalStateException("downstream timeout");
        }
        return ApiResult.ok("user-" + id);
    }

    /**
     * Fallback for {@link #getUser(Long)} — returns an empty result
     * when the downstream is degraded.
     */
    @SuppressWarnings("unused")
    private ApiResult<String> fallbackUser(Long id, Throwable cause) {
        return ApiResult.ok("user-" + id + "-cached");
    }
}
