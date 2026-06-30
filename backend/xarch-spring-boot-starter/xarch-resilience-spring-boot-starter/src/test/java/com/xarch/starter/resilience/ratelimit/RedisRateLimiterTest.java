package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RedisRateLimiter} using the in-memory fallback
 * implementation. The in-memory implementation is a pure-Java token
 * bucket with the same algorithm as the Redis Lua script, so the
 * behaviour exercised here reflects what production sees.
 */
class RedisRateLimiterTest {

    @Test
    void firstRequestIsAllowed() {
        RedisRateLimiter limiter = RedisRateLimiter.inMemory(new ResilienceProperties());
        assertThat(limiter.tryAcquire("user:1")).isTrue();
        assertThat(limiter.isRedisBacked()).isFalse();
    }

    @Test
    void burstIsBounded() {
        ResilienceProperties props = new ResilienceProperties();
        props.getRateLimit().setPermitsPerSecond(2);
        props.getRateLimit().setBurstCapacity(2);
        RedisRateLimiter limiter = RedisRateLimiter.inMemory(props);

        // Burst of 2
        assertThat(limiter.tryAcquire("k1", 1, 2, 2)).isTrue();
        assertThat(limiter.tryAcquire("k1", 1, 2, 2)).isTrue();
        // Third call is denied.
        assertThat(limiter.tryAcquire("k1", 1, 2, 2)).isFalse();
    }

    @Test
    void differentKeysDoNotShareBucket() {
        ResilienceProperties props = new ResilienceProperties();
        props.getRateLimit().setPermitsPerSecond(1);
        props.getRateLimit().setBurstCapacity(1);
        RedisRateLimiter limiter = RedisRateLimiter.inMemory(props);

        assertThat(limiter.tryAcquire("ip:1.1.1.1", 1, 1, 1)).isTrue();
        assertThat(limiter.tryAcquire("ip:2.2.2.2", 1, 1, 1)).isTrue();
        // Same key denied.
        assertThat(limiter.tryAcquire("ip:1.1.1.1", 1, 1, 1)).isFalse();
    }

    @Test
    void tokensRefillOverTime() throws InterruptedException {
        ResilienceProperties props = new ResilienceProperties();
        props.getRateLimit().setPermitsPerSecond(20);
        props.getRateLimit().setBurstCapacity(2);
        RedisRateLimiter limiter = RedisRateLimiter.inMemory(props);

        // Drain the burst.
        assertThat(limiter.tryAcquire("k", 1, 20, 2)).isTrue();
        assertThat(limiter.tryAcquire("k", 1, 20, 2)).isTrue();
        assertThat(limiter.tryAcquire("k", 1, 20, 2)).isFalse();

        // 250ms at 20 pps ≈ 5 tokens refilled. Should be allowed again.
        Thread.sleep(250);
        assertThat(limiter.tryAcquire("k", 1, 20, 2)).isTrue();
    }
}
