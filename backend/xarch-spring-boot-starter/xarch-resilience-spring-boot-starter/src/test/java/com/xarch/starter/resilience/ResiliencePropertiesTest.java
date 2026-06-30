package com.xarch.starter.resilience;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ResilienceProperties} defaults and simple setters.
 */
class ResiliencePropertiesTest {

    @Test
    void defaultsAreSensible() {
        ResilienceProperties properties = new ResilienceProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getRateLimit().getPermitsPerSecond()).isEqualTo(100);
        assertThat(properties.getRateLimit().getBurstCapacity()).isEqualTo(100);
        assertThat(properties.getRateLimit().getDefaultScope())
                .isEqualTo(ResilienceProperties.Scope.IP);
        assertThat(properties.getRateLimit().isServletEnabled()).isTrue();
        assertThat(properties.getRateLimit().getTimeoutMillis()).isZero();
        assertThat(properties.getRateLimit().getRedisKeyPrefix()).isEqualTo("xarch:rl:");

        assertThat(properties.getCircuitBreaker().getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(properties.getCircuitBreaker().getSlidingWindowSize()).isEqualTo(100);
        assertThat(properties.getCircuitBreaker().getMinimumNumberOfCalls()).isEqualTo(20);
        assertThat(properties.getCircuitBreaker().getWaitDurationInOpenStateMillis())
                .isEqualTo(10_000L);

        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(3);
        assertThat(properties.getRetry().getDelayMillis()).isEqualTo(200L);
        assertThat(properties.getRetry().getMultiplier()).isEqualTo(2.0);
        assertThat(properties.getRetry().getMaxDelayMillis()).isEqualTo(2_000L);

        assertThat(properties.getBulkhead().getConcurrentCalls()).isEqualTo(25);
        assertThat(properties.getBulkhead().getMaxWaitDurationMillis()).isZero();
    }

    @Test
    void scopeEnumRoundTrips() {
        for (ResilienceProperties.Scope scope : ResilienceProperties.Scope.values()) {
            ResilienceProperties properties = new ResilienceProperties();
            properties.getRateLimit().setDefaultScope(scope);
            assertThat(properties.getRateLimit().getDefaultScope()).isEqualTo(scope);
        }
    }

    @Test
    void settersRoundTrip() {
        ResilienceProperties properties = new ResilienceProperties();
        properties.setEnabled(false);
        properties.getRateLimit().setPermitsPerSecond(50);
        properties.getCircuitBreaker().setFailureRateThreshold(75.0f);
        properties.getRetry().setMaxAttempts(5);
        properties.getBulkhead().setConcurrentCalls(100);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getPermitsPerSecond()).isEqualTo(50);
        assertThat(properties.getCircuitBreaker().getFailureRateThreshold()).isEqualTo(75.0f);
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
        assertThat(properties.getBulkhead().getConcurrentCalls()).isEqualTo(100);
    }
}
