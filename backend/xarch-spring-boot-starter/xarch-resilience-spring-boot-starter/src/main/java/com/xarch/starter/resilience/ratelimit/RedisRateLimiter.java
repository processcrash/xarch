package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token-bucket rate limiter backed by Redis.
 *
 * <p>The bucket is updated through a single Lua script (atomic, single
 * round-trip). Each bucket is keyed {@code <prefix><key>} and stores two
 * fields: the number of available tokens and the timestamp of the last
 * refill. The script:
 * <ol>
 *   <li>Computes how many tokens should be added since {@code lastRefill}
 *       based on {@code permitsPerSecond} and the elapsed time.</li>
 *   <li>Clamps the result to {@code burstCapacity}.</li>
 *   <li>If at least one token is available, decrements it and returns
 *       {@code 1} (allowed). Otherwise returns {@code 0} (denied).</li>
 * </ol>
 *
 * <p>When Redis is not available, the class transparently falls back to
 * an in-process token bucket — useful for local dev and unit tests.
 */
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    /**
     * Atomic token-bucket Lua script. Returns {@code 1} when the request
     * is allowed and {@code 0} when it must be rejected.
     */
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local permits_per_second = tonumber(ARGV[1])
            local burst_capacity = tonumber(ARGV[2])
            local now_millis = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])

            local data = redis.call('HMGET', key, 'tokens', 'lastRefill')
            local tokens = tonumber(data[1])
            local last_refill = tonumber(data[2])

            if tokens == nil then
                tokens = burst_capacity
                last_refill = now_millis
            end

            local elapsed = math.max(0, now_millis - last_refill) / 1000.0
            local refill = elapsed * permits_per_second
            tokens = math.min(burst_capacity, tokens + refill)

            local allowed = 0
            if tokens >= requested then
                tokens = tokens - requested
                allowed = 1
            end

            redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now_millis)
            redis.call('PEXPIRE', key, 60000)
            return allowed
            """;

    private static final RedisScript<Long> SCRIPT = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ResilienceProperties properties;
    private final boolean redisBacked;

    /** Per-key in-memory bucket state used as a fallback. */
    private final ConcurrentHashMap<String, InMemoryBucket> inMemoryBuckets = new ConcurrentHashMap<>();

    private RedisRateLimiter(StringRedisTemplate redisTemplate, ResilienceProperties properties, boolean redisBacked) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.redisBacked = redisBacked;
    }

    /**
     * Create a Redis-backed limiter.
     */
    public static RedisRateLimiter redisBacked(StringRedisTemplate template, ResilienceProperties properties) {
        return new RedisRateLimiter(template, properties, true);
    }

    /**
     * Create an in-process limiter. Used when no Redis is configured.
     */
    public static RedisRateLimiter inMemory(ResilienceProperties properties) {
        return new RedisRateLimiter(null, properties, false);
    }

    /**
     * Check whether a single permit is available for the given key.
     *
     * @return {@code true} when the call may proceed.
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, 1, properties.getRateLimit().getPermitsPerSecond(),
                properties.getRateLimit().getBurstCapacity());
    }

    /**
     * Check whether {@code permits} permits are available for the given key.
     *
     * @param key       bucket key (without prefix).
     * @param permits   number of permits requested.
     * @param pps       permits per second limit.
     * @param burst     maximum tokens in the bucket.
     * @return {@code true} when all {@code permits} were deducted.
     */
    public boolean tryAcquire(String key, int permits, int pps, int burst) {
        if (!redisBacked) {
            return inMemoryTryAcquire(key, permits, pps, burst);
        }
        try {
            String redisKey = properties.getRateLimit().getRedisKeyPrefix() + key;
            Long result = redisTemplate.execute(SCRIPT, List.of(redisKey),
                    String.valueOf(pps),
                    String.valueOf(burst),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(permits));
            return result != null && result == 1L;
        } catch (Exception ex) {
            // Fail-open: never block traffic because the limiter is broken.
            log.warn("xarch rate-limit script failed, allowing request: {}", ex.getMessage());
            return true;
        }
    }

    private boolean inMemoryTryAcquire(String key, int permits, int pps, int burst) {
        InMemoryBucket bucket = inMemoryBuckets.computeIfAbsent(key, k -> new InMemoryBucket(burst));
        return bucket.tryAcquire(permits, pps, burst);
    }

    public boolean isRedisBacked() {
        return redisBacked;
    }

    /**
     * Tiny in-process token bucket used as a fallback. Not thread-safe
     * across nodes — only used for local dev and tests.
     */
    private static final class InMemoryBucket {
        private double tokens;
        private long lastRefillMillis;
        private final Object lock = new Object();

        InMemoryBucket(int initial) {
            this.tokens = initial;
            this.lastRefillMillis = System.currentTimeMillis();
        }

        boolean tryAcquire(int permits, int pps, int burst) {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                double elapsed = Math.max(0, now - lastRefillMillis) / 1000.0;
                tokens = Math.min(burst, tokens + elapsed * pps);
                lastRefillMillis = now;
                if (tokens >= permits) {
                    tokens -= permits;
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Counter used by tests for diagnostics. Always present, currently
     * unused by the production path.
     */
    static final AtomicLong SCRIPT_INVOCATIONS = new AtomicLong();
}
