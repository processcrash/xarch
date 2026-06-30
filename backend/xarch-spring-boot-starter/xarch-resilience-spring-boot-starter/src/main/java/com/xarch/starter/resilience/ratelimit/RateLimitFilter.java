package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Global rate-limit filter applied to every incoming HTTP request.
 *
 * <p>Scoping rules:
 * <ul>
 *   <li>{@code IP} — bucket key is the first non-empty value among
 *       {@code X-Forwarded-For}, {@code X-Real-IP} and
 *       {@code request.getRemoteAddr()}.</li>
 *   <li>{@code USER} — bucket key is the {@code X-User-Id} header (set
 *       by your security filter). Falls back to IP when absent.</li>
 *   <li>{@code GLOBAL} — single bucket per service instance.</li>
 * </ul>
 *
 * <p>When the bucket is empty the filter returns {@code 429 Too Many
 * Requests} with a small JSON body. The downstream controller is not
 * invoked.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RedisRateLimiter rateLimiter;
    private final ResilienceProperties properties;

    public RateLimitFilter(RedisRateLimiter rateLimiter, ResilienceProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String key = resolveKey(request);
        ResilienceProperties.RateLimit cfg = properties.getRateLimit();

        boolean allowed = rateLimiter.tryAcquire(key, 1,
                cfg.getPermitsPerSecond(), cfg.getBurstCapacity());

        if (!allowed) {
            log.debug("xarch rate-limit denied for key={} on {}", key, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", "1");
            response.getWriter().write(
                    "{\"code\":429,\"message\":\"Too many requests, please retry later\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Build the bucket key for the given request based on the configured
     * scope.
     */
    String resolveKey(HttpServletRequest request) {
        return switch (properties.getRateLimit().getDefaultScope()) {
            case IP -> "ip:" + clientIp(request);
            case USER -> {
                String userId = request.getHeader("X-User-Id");
                yield "user:" + (userId == null || userId.isBlank() ? clientIp(request) : userId);
            }
            case GLOBAL -> "global";
        };
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }
}
