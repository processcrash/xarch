package com.xarch.starter.resilience.circuitbreaker;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CircuitBreakerAspect} and
 * {@link CircuitBreakerFallbackHandler}. The aspect is exercised via a
 * dynamic proxy so we can verify fallback routing without Spring.
 */
class CircuitBreakerAspectTest {

    @CircuitBreaker(name = "ok", fallback = "fallbackOptional")
    public Optional<String> happy() {
        return Optional.of("ok");
    }

    @CircuitBreaker(name = "boom", fallback = "fallbackOptional")
    public Optional<String> sad() {
        throw new IllegalStateException("downstream is down");
    }

    @SuppressWarnings("unused")
    private Optional<String> fallbackOptional(Throwable t) {
        return Optional.empty();
    }

    @Test
    void successfulCallDoesNotInvokeFallback() {
        AspectJProxyFactory factory = new AspectJProxyFactory(this);
        factory.addAspect(aspect());
        CircuitBreakerAspectTest proxy = factory.getProxy();

        assertThat(proxy.happy()).contains("ok");
    }

    @Test
    void failingCallInvokesFallback() {
        AspectJProxyFactory factory = new AspectJProxyFactory(this);
        factory.addAspect(aspect());
        CircuitBreakerAspectTest proxy = factory.getProxy();

        assertThat(proxy.sad()).isEmpty();
    }

    @Test
    void fallbackHandlerReturnsEmptyForOptional() {
        CircuitBreakerFallbackHandler handler = new CircuitBreakerFallbackHandler();
        Object result = handler.defaultFallback(Optional.class,
                new IllegalStateException("nope"));
        assertThat(result).isInstanceOf(Optional.class).isEqualTo(Optional.empty());
    }

    private CircuitBreakerAspect aspect() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(2)
                        .minimumNumberOfCalls(2)
                        .failureRateThreshold(50.0f)
                        .build());
        return new CircuitBreakerAspect(registry, new CircuitBreakerFallbackHandler());
    }

    @Test
    void defaultsProperties() {
        ResilienceProperties p = new ResilienceProperties();
        assertThat(p.getCircuitBreaker().getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(p.getCircuitBreaker().getSlidingWindowSize()).isEqualTo(100);
    }
}
