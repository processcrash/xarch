package com.xarch.cloud.gateway.config;

import com.xarch.cloud.gateway.filter.CircuitBreakerGatewayFilter;
import com.xarch.cloud.gateway.filter.RateLimitGatewayFilter;
import com.xarch.starter.resilience.RedisRateLimiterPlaceholder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway-side resilience wiring.
 *
 * <p>The starter auto-configuration already registers the
 * {@code RedisRateLimiter}, {@code CircuitBreakerRegistry},
 * {@code RetryRegistry} and {@code BulkheadRegistry} beans. This
 * configuration just exposes the gateway filter factory beans which
 * depend on those.
 */
@Configuration
public class ResilienceConfig {

    /**
     * Placeholder annotation that documents the {@link RedisRateLimiter}
     * dependency — the actual bean is registered by
     * {@code XarchResilienceAutoConfiguration}.
     */
    @Bean
    public RateLimitGatewayFilter rateLimitGatewayFilter(
            com.xarch.starter.resilience.ratelimit.RedisRateLimiter rateLimiter,
            com.xarch.starter.resilience.ResilienceProperties properties) {
        return new RateLimitGatewayFilter(rateLimiter, properties);
    }

    @Bean
    public CircuitBreakerGatewayFilter circuitBreakerGatewayFilter(
            CircuitBreakerRegistry registry) {
        return new CircuitBreakerGatewayFilter(registry);
    }

    /**
     * Internal placeholder — never wired as a bean. The annotation import
     * above keeps the {@code RedisRateLimiterPlaceholder} type discoverable
     * to IDEs without polluting the actual bean graph.
     */
    private static final class RedisRateLimiterPlaceholder {
    }
}
