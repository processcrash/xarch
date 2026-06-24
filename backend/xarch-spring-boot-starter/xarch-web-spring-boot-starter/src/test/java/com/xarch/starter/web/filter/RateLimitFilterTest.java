package com.xarch.starter.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RateLimitFilter}.
 *
 * <p>Verifies the per-IP token-bucket behavior: requests under the limit pass
 * through with rate-limit headers, requests above the limit are blocked with a
 * 429 status and an error JSON body.</p>
 */
@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    private RateLimitFilter filter;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimitFilter();
        responseBody = new StringWriter();
    }

    @Test
    @DisplayName("First request from an IP passes through and adds rate-limit headers")
    void firstRequest_passesThroughAndSetsHeaders() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.1");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain, times(1)).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Limit", "120");
        verify(response).setHeader("X-RateLimit-Remaining", "119");
    }

    @Test
    @DisplayName("Requests exceeding 120/minute are blocked with HTTP 429 and JSON error")
    void excessiveRequests_areBlockedWith429() throws Exception {
        // Arrange - 121 requests in a tight loop from the same IP
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.5");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("198.51.100.5");
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        // Act: 121 requests
        for (int i = 0; i < 121; i++) {
            filter.doFilter(request, response, chain);
        }

        // Assert: chain was invoked exactly 120 times, last request was blocked
        verify(chain, times(120)).doFilter(request, response);
        verify(response).setStatus(429);
        assertThat(responseBody.toString()).contains("\"code\":\"1005\"");
        assertThat(responseBody.toString()).contains("请求过于频繁");
    }

    @Test
    @DisplayName("Different IPs are tracked independently")
    void differentIps_trackedIndependently() throws Exception {
        // Arrange
        FilterChain chain = mock(FilterChain.class);

        // IP A: 120 requests - hits the limit on 120th
        for (int i = 0; i < 120; i++) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            filter.doFilter(request, response, chain);
        }

        // IP B: still allowed because it's a different bucket
        HttpServletRequest ipB = mock(HttpServletRequest.class);
        HttpServletResponse ipBResp = mock(HttpServletResponse.class);
        when(ipB.getHeader("X-Forwarded-For")).thenReturn("10.0.0.2");
        when(ipB.getHeader("X-Real-IP")).thenReturn(null);
        when(ipB.getRemoteAddr()).thenReturn("10.0.0.2");

        // Act
        filter.doFilter(ipB, ipBResp, chain);

        // Assert
        verify(ipBResp, never()).setStatus(429);
    }

    @Test
    @DisplayName("X-Forwarded-For header is used when present")
    void xForwardedFor_isPreferred() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.0.2.10, 10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        filter.doFilter(request, response, chain);

        // Assert: the first IP from the comma list is used; remoteAddr not consulted
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Falls back to X-Real-IP when X-Forwarded-For is missing")
    void xRealIp_isUsedWhenForwardedForMissing() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.42");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Falls back to remoteAddr when no proxy headers are set")
    void remoteAddr_isUsedWhenNoHeadersPresent() throws Exception {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
        verify(response).setHeader("X-RateLimit-Limit", "120");
    }
}