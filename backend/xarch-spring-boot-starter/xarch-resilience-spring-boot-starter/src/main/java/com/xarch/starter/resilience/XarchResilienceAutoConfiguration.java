package com.xarch.starter.resilience;

import com.xarch.starter.resilience.bulkhead.BulkheadAspect;
import com.xarch.starter.resilience.circuitbreaker.CircuitBreakerAspect;
import com.xarch.starter.resilience.circuitbreaker.CircuitBreakerFallbackHandler;
import com.xarch.starter.resilience.ratelimit.RateLimitAspect;
import com.xarch.starter.resilience.ratelimit.RateLimitFilter;
import com.xarch.starter.resilience.ratelimit.RedisRateLimiter;
import com.xarch.starter.resilience.retry.RetryAspect;
import com.xarch.starter.resilience.retry.RetryListener;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.EnumSet;

/**
 * Entry point for the xarch resilience starter.
 *
 * <p>Aggregates four resilience patterns on top of Resilience4j 2.2.0:
 * <ul>
 *   <li><b>Rate limiter</b> — global servlet {@link RateLimitFilter} plus
 *       method-level {@link RateLimitAspect} backed by a Redis Lua token
 *       bucket (single round-trip, atomic).</li>
 *   <li><b>Circuit breaker</b> — {@link CircuitBreakerAspect} wrapping
 *       invocations with a configured Resilience4j breaker and a
 *       {@link CircuitBreakerFallbackHandler} producing degraded
 *       {@code ApiResult}s.</li>
 *   <li><b>Retry</b> — {@link RetryAspect} with exponential backoff and
 *       {@link RetryListener} for attempt logging.</li>
 *   <li><b>Bulkhead</b> — {@link BulkheadAspect} enforcing a hard
 *       concurrency cap per method.</li>
 * </ul>
 *
 * <p>Master switch: {@code xarch.resilience.enabled} (default {@code true}).
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(ResilienceProperties.class)
@ConditionalOnProperty(prefix = "xarch.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "io.github.resilience4j.springboot3.resilience4j.SpringCircuitBreakerConfiguration")
public class XarchResilienceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(XarchResilienceAutoConfiguration.class);

    /**
     * Redis-backed rate limiter using a Lua token bucket. Falls back to a
     * simple in-memory implementation when no {@link StringRedisTemplate}
     * is available (e.g. unit tests or services that do not need cluster
     * wide limits).
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiter redisRateLimiter(
            org.springframework.beans.factory.ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            ResilienceProperties properties) {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template == null) {
            log.info("xarch resilience: no StringRedisTemplate found, "
                    + "falling back to in-memory rate limiter");
            return RedisRateLimiter.inMemory(properties);
        }
        return RedisRateLimiter.redisBacked(template, properties);
    }

    /**
     * Servlet filter that enforces the IP-scoped global rate limit on every
     * incoming request. Skipped when {@code xarch.resilience.rate-limit.servlet-enabled}
     * is explicitly set to {@code false}.
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "xarch.resilience.rate-limit", name = "servlet-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RedisRateLimiter rateLimiter,
                                                                              ResilienceProperties properties) {
        RateLimitFilter filter = new RateLimitFilter(rateLimiter, properties);
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("xarchRateLimitFilter");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE + 200);
        return registration;
    }

    /**
     * Aspect that handles the custom {@code @RateLimit} annotation using
     * the Redis bucket.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RedisRateLimiter rateLimiter,
                                           ResilienceProperties properties) {
        return new RateLimitAspect(rateLimiter, properties);
    }

    /**
     * Aspect that handles the custom {@code @CircuitBreaker} annotation.
     * Falls back to the global Resilience4j registry when the named breaker
     * is not configured.
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerAspect circuitBreakerAspect(CircuitBreakerRegistry registry,
                                                     CircuitBreakerFallbackHandler fallbackHandler) {
        return new CircuitBreakerAspect(registry, fallbackHandler);
    }

    /**
     * Default fallback handler producing {@code ApiResult.fail} responses.
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerFallbackHandler circuitBreakerFallbackHandler() {
        return new CircuitBreakerFallbackHandler();
    }

    /**
     * Aspect that handles the custom {@code @Retry} annotation with
     * exponential backoff.
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryAspect retryAspect(RetryRegistry registry, RetryListener listener) {
        return new RetryAspect(registry, listener);
    }

    /**
     * Listener that logs every retry attempt.
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryListener retryListener() {
        return new RetryListener();
    }

    /**
     * Aspect that handles the custom {@code @Bulkhead} annotation enforcing
     * a hard concurrency cap.
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadAspect bulkheadAspect(BulkheadRegistry registry) {
        return new BulkheadAspect(registry);
    }

    /**
     * Log a single info line at startup to confirm the starter is active.
     */
    @Bean
    @ConditionalOnMissingBean
    public ResilienceStartupLogger resilienceStartupLogger(ResilienceProperties properties,
                                                            RateLimiterRegistry rateLimiterRegistry,
                                                            CircuitBreakerRegistry circuitBreakerRegistry,
                                                            RetryRegistry retryRegistry,
                                                            BulkheadRegistry bulkheadRegistry) {
        return new ResilienceStartupLogger(properties, rateLimiterRegistry,
                circuitBreakerRegistry, retryRegistry, bulkheadRegistry);
    }

    /**
     * Simple no-op bean that logs configuration at startup.
     */
    public static class ResilienceStartupLogger {
        public ResilienceStartupLogger(ResilienceProperties properties,
                                        RateLimiterRegistry rateLimiterRegistry,
                                        CircuitBreakerRegistry circuitBreakerRegistry,
                                        RetryRegistry retryRegistry,
                                        BulkheadRegistry bulkheadRegistry) {
            log.info("xarch resilience initialised: rate-limit pps={}, breaker threshold={}%, "
                            + "retry max-attempts={}, bulkhead concurrent-calls={}, "
                            + "rate-limiters={}, circuit-breakers={}, retries={}, bulkheads={}",
                    properties.getRateLimit().getPermitsPerSecond(),
                    properties.getCircuitBreaker().getFailureRateThreshold(),
                    properties.getRetry().getMaxAttempts(),
                    properties.getBulkhead().getConcurrentCalls(),
                    rateLimiterRegistry.getAllRateLimiters().size(),
                    circuitBreakerRegistry.getAllCircuitBreakers().size(),
                    retryRegistry.getAllRetries().size(),
                    bulkheadRegistry.getAllBulkheads().size());
        }
    }
}
