package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link RateLimitAspect}. Weaves the aspect into a small
 * POJO using {@link AspectJProxyFactory} so we exercise the @Around logic
 * without bringing up a full Spring context.
 */
class RateLimitAspectTest {

    @RateLimit(permitsPerSecond = 1, scope = RateLimit.Scope.GLOBAL)
    public String ping() {
        return "pong";
    }

    @Test
    void firstCallSucceedsSecondIsDenied() {
        ResilienceProperties props = new ResilienceProperties();
        props.getRateLimit().setPermitsPerSecond(1);
        props.getRateLimit().setBurstCapacity(1);
        RedisRateLimiter limiter = RedisRateLimiter.inMemory(props);
        RateLimitAspect aspect = new RateLimitAspect(limiter, props);

        AspectJProxyFactory factory = new AspectJProxyFactory(this);
        factory.addAspect(aspect);
        RateLimitAspectTest proxy = factory.getProxy();

        assertThat(proxy.ping()).isEqualTo("pong");
        assertThatThrownBy(proxy::ping).isInstanceOf(RequestNotPermitted.class);
    }
}
