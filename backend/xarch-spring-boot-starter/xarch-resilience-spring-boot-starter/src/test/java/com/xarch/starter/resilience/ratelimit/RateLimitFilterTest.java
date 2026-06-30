package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RateLimitFilter}. The filter is exercised
 * against the in-memory bucket so no Redis is required.
 */
class RateLimitFilterTest {

    private ResilienceProperties properties;
    private RedisRateLimiter limiter;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        properties = new ResilienceProperties();
        properties.getRateLimit().setPermitsPerSecond(1);
        properties.getRateLimit().setBurstCapacity(1);
        limiter = RedisRateLimiter.inMemory(properties);
        filter = new RateLimitFilter(limiter, properties);
    }

    @Test
    void allowedRequestsPassThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void overTheLimitReturns429() throws Exception {
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/test");
        req1.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, mock(FilterChain.class));

        // Second request from the same IP is denied.
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/test");
        req2.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(req2, res2, chain);

        assertThat(res2.getStatus()).isEqualTo(429);
        assertThat(res2.getContentAsString()).contains("Too many requests");
        assertThat(res2.getHeader("Retry-After")).isEqualTo("1");
        verify(chain, never()).doFilter(req2, res2);
    }

    @Test
    void ipKeyPrefersXForwardedFor() {
        HttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        req.setRemoteAddr("10.0.0.1");

        assertThat(filter.resolveKey(req)).isEqualTo("ip:203.0.113.5");
    }

    @Test
    void userScopeUsesXUserIdHeader() {
        properties.getRateLimit().setDefaultScope(ResilienceProperties.Scope.USER);
        filter = new RateLimitFilter(limiter, properties);

        HttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        req.addHeader("X-User-Id", "alice");

        assertThat(filter.resolveKey(req)).isEqualTo("user:alice");
    }

    @Test
    void globalScopeUsesSingleBucket() {
        properties.getRateLimit().setDefaultScope(ResilienceProperties.Scope.GLOBAL);
        filter = new RateLimitFilter(limiter, properties);

        HttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/test");
        req1.setRemoteAddr("1.1.1.1");
        HttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/test");
        req2.setRemoteAddr("2.2.2.2");

        assertThat(filter.resolveKey(req1)).isEqualTo("global");
        assertThat(filter.resolveKey(req2)).isEqualTo("global");
    }
}
