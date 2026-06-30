package com.xarch.cloud.gateway.filter;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Gateway filter that applies a per-route rate limit using the shared
 * {@link RedisRateLimiter} bean.
 *
 * <p>Configurable per route in {@code application.yml}:
 * <pre>{@code
 * filters:
 *   - name: RateLimit
 *     args:
 *       permitsPerSecond: 50
 *       burstCapacity: 50
 *       scope: IP
 * }</pre>
 */
@Component
public class RateLimitGatewayFilter extends AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config> {

    private final RedisRateLimiter rateLimiter;
    private final ResilienceProperties properties;

    public RateLimitGatewayFilter(RedisRateLimiter rateLimiter, ResilienceProperties properties) {
        super(Config.class);
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = resolveKey(exchange, config);
            int pps = config.permitsPerSecond > 0
                    ? config.permitsPerSecond
                    : properties.getRateLimit().getPermitsPerSecond();
            int burst = config.burstCapacity > 0
                    ? config.burstCapacity
                    : properties.getRateLimit().getBurstCapacity();
            boolean allowed = rateLimiter.tryAcquire(key, 1, pps, burst);
            if (!allowed) {
                var response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return response.writeWith(reactor.core.publisher.Mono.just(
                        response.bufferFactory().wrap(
                                "{\"code\":429,\"message\":\"rate limit exceeded\"}"
                                        .getBytes())));
            }
            return chain.filter(exchange);
        };
    }

    private String resolveKey(org.springframework.web.server.ServerWebExchange exchange, Config config) {
        var request = exchange.getRequest();
        ResilienceProperties.Scope scope = config.scope != null
                ? config.scope
                : properties.getRateLimit().getDefaultScope();
        return switch (scope) {
            case IP -> "ip:" + clientIp(request);
            case USER -> {
                String userId = request.getHeaders().getFirst("X-User-Id");
                yield "user:" + (userId == null || userId.isBlank() ? clientIp(request) : userId);
            }
            case GLOBAL -> "global";
        };
    }

    private static String clientIp(org.springframework.http.server.reactive.ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String real = request.getHeaders().getFirst("X-Real-IP");
        if (real != null && !real.isBlank()) {
            return real.trim();
        }
        var remote = request.getRemoteAddress();
        return remote == null ? "unknown" : remote.getAddress().getHostAddress();
    }

    /**
     * Filter configuration bound from {@code args:} in the route definition.
     */
    public static class Config {
        private int permitsPerSecond;
        private int burstCapacity;
        private ResilienceProperties.Scope scope;

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

        public ResilienceProperties.Scope getScope() {
            return scope;
        }

        public void setScope(ResilienceProperties.Scope scope) {
            this.scope = scope;
        }
    }
}
