package com.xarch.starter.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for the xarch resilience starter.
 *
 * <p>All settings live under the {@code xarch.resilience} prefix in
 * {@code application.yml}. Default values are intentionally conservative
 * so the starter is safe to enable globally.
 */
@ConfigurationProperties(prefix = "xarch.resilience")
public class ResilienceProperties {

    /**
     * Master switch. When {@code false} the starter registers no beans and
     * the annotations become no-ops.
     */
    private boolean enabled = true;

    /**
     * Rate limit settings — applies to both the global servlet filter and
     * the {@code @RateLimit} annotation.
     */
    @NestedConfigurationProperty
    private RateLimit rateLimit = new RateLimit();

    /**
     * Circuit breaker settings — bound to the named breakers created via
     * the {@code @CircuitBreaker} annotation.
     */
    @NestedConfigurationProperty
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * Retry settings — applied to every {@code @Retry} annotation that does
     * not override the values explicitly.
     */
    @NestedConfigurationProperty
    private Retry retry = new Retry();

    /**
     * Bulkhead settings — concurrency cap for {@code @Bulkhead}.
     */
    @NestedConfigurationProperty
    private Bulkhead bulkhead = new Bulkhead();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }

    /**
     * Rate limit settings. Mirrors the structure exposed by
     * Resilience4j's own {@code RateLimiterConfig}.
     */
    public static class RateLimit {
        /**
         * Whether the global servlet filter is registered. Per-method
         * {@code @RateLimit} annotations always work.
         */
        private boolean servletEnabled = true;

        /**
         * Default permits per second granted to each scoped bucket. Can be
         * overridden on the {@code @RateLimit} annotation itself.
         */
        private int permitsPerSecond = 100;

        /**
         * Maximum permits a bucket can hold in idle bursts. Equivalent to
         * the {@code limitForPeriod} Resilience4j field.
         */
        private int burstCapacity = 100;

        /**
         * Default scope for the global filter. {@code IP} is the safest
         * default and what most APIs want.
         */
        private Scope defaultScope = Scope.IP;

        /**
         * Maximum wait time when a downstream call is rate-limited. Calls
         * exceeding this wait get a 429 response.
         */
        private long timeoutMillis = 0L;

        /**
         * Redis key prefix used by the bucket. Keep it short — it appears
         * on every request.
         */
        private String redisKeyPrefix = "xarch:rl:";

        public boolean isServletEnabled() {
            return servletEnabled;
        }

        public void setServletEnabled(boolean servletEnabled) {
            this.servletEnabled = servletEnabled;
        }

        public int getPermitsPerSecond() {
            return permitsPerSecond;
        }

        public void setPermitsPerSecond(int permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public Scope getDefaultScope() {
            return defaultScope;
        }

        public void setDefaultScope(Scope defaultScope) {
            this.defaultScope = defaultScope;
        }

        public long getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        public String getRedisKeyPrefix() {
            return redisKeyPrefix;
        }

        public void setRedisKeyPrefix(String redisKeyPrefix) {
            this.redisKeyPrefix = redisKeyPrefix;
        }
    }

    /**
     * Circuit breaker settings.
     */
    public static class CircuitBreaker {
        /**
         * Failure rate (percentage) above which the breaker opens.
         */
        private float failureRateThreshold = 50.0f;

        /**
         * Number of calls in the sliding window.
         */
        private int slidingWindowSize = 100;

        /**
         * Minimum number of calls before the breaker can trip.
         */
        private int minimumNumberOfCalls = 20;

        /**
         * How long the breaker stays open before transitioning to
         * half-open.
         */
        private long waitDurationInOpenStateMillis = 10_000L;

        /**
         * Number of permitted calls in the half-open state.
         */
        private int permittedNumberOfCallsInHalfOpenState = 5;

        /**
         * Whether failure of every call — or just exceptional calls —
         * should be counted by the breaker.
         */
        private boolean recordExceptions = true;

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public long getWaitDurationInOpenStateMillis() {
            return waitDurationInOpenStateMillis;
        }

        public void setWaitDurationInOpenStateMillis(long waitDurationInOpenStateMillis) {
            this.waitDurationInOpenStateMillis = waitDurationInOpenStateMillis;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }

        public boolean isRecordExceptions() {
            return recordExceptions;
        }

        public void setRecordExceptions(boolean recordExceptions) {
            this.recordExceptions = recordExceptions;
        }
    }

    /**
     * Retry settings.
     */
    public static class Retry {
        /**
         * Default maximum number of attempts (initial + retries).
         */
        private int maxAttempts = 3;

        /**
         * Initial delay before the first retry, in milliseconds.
         */
        private long delayMillis = 200L;

        /**
         * Multiplier applied to {@code delayMillis} on every subsequent
         * retry. {@code 2.0} means exponential backoff with factor 2.
         */
        private double multiplier = 2.0;

        /**
         * Hard cap on the delay between retries, in milliseconds.
         */
        private long maxDelayMillis = 2_000L;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getDelayMillis() {
            return delayMillis;
        }

        public void setDelayMillis(long delayMillis) {
            this.delayMillis = delayMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public long getMaxDelayMillis() {
            return maxDelayMillis;
        }

        public void setMaxDelayMillis(long maxDelayMillis) {
            this.maxDelayMillis = maxDelayMillis;
        }
    }

    /**
     * Bulkhead settings.
     */
    public static class Bulkhead {
        /**
         * Maximum concurrent calls allowed for each bulkhead.
         */
        private int concurrentCalls = 25;

        /**
         * Maximum wait time, in milliseconds, when the bulkhead is full.
         */
        private long maxWaitDurationMillis = 0L;

        public int getConcurrentCalls() {
            return concurrentCalls;
        }

        public void setConcurrentCalls(int concurrentCalls) {
            this.concurrentCalls = concurrentCalls;
        }

        public long getMaxWaitDurationMillis() {
            return maxWaitDurationMillis;
        }

        public void setMaxWaitDurationMillis(long maxWaitDurationMillis) {
            this.maxWaitDurationMillis = maxWaitDurationMillis;
        }
    }

    /**
     * Scope used to differentiate rate-limit buckets.
     */
    public enum Scope {
        /** Bucket key is the client IP. */
        IP,
        /** Bucket key is the authenticated user id. */
        USER,
        /** Single global bucket. */
        GLOBAL
    }
}
