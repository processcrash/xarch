package com.xarch.cloud.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Gateway filter that wraps the downstream chain in a Resilience4j
 * circuit breaker. When the breaker is open the gateway short-circuits
 * with {@code 503 Service Unavailable} and a small JSON body.
 *
 * <p>Sample route config:
 * <pre>{@code
 * filters:
 *   - name: CircuitBreakerGW
 *     args:
 *       name: xarch-example
 *       failureRateThreshold: 50
 *       slidingWindowSize: 20
 *       waitDurationInOpenStateMillis: 10000
 * }</pre>
 */
@Component
public class CircuitBreakerGatewayFilter
        extends AbstractGatewayFilterFactory<CircuitBreakerGatewayFilter.Config> {

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerGatewayFilter(CircuitBreakerRegistry registry) {
        super(Config.class);
        this.registry = registry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        CircuitBreaker breaker = breakerFor(config);

        return (exchange, chain) -> chain.filter(exchange)
                .transformDeferred(CircuitBreakerOperator.of(breaker))
                .onErrorResume(ex -> writeDegraded(exchange, breaker, ex));
    }

    private CircuitBreaker breakerFor(Config config) {
        return registry.find(config.name).orElseGet(() -> {
            CircuitBreakerConfig base = registry.getConfiguration("default")
                    .orElseGet(CircuitBreakerConfig::ofDefaults);
            CircuitBreakerConfig.Builder b = CircuitBreakerConfig.from(base);
            if (config.failureRateThreshold > 0) {
                b.failureRateThreshold(config.failureRateThreshold);
            }
            if (config.slidingWindowSize > 0) {
                b.slidingWindowSize(config.slidingWindowSize);
            }
            if (config.minimumNumberOfCalls > 0) {
                b.minimumNumberOfCalls(config.minimumNumberOfCalls);
            }
            if (config.waitDurationInOpenStateMillis > 0) {
                b.waitDurationInOpenState(Duration.ofMillis(config.waitDurationInOpenStateMillis));
            }
            return registry.circuitBreaker(config.name, b.build());
        });
    }

    private Mono<Void> writeDegraded(org.springframework.web.server.ServerWebExchange exchange,
                                       CircuitBreaker breaker, Throwable ex) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"code\":503,\"message\":\"upstream degraded\",\"breaker\":\""
                + breaker.getName() + "\"}").getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    /**
     * Filter configuration.
     */
    public static class Config {
        private String name = "default";
        private float failureRateThreshold = 50.0f;
        private int slidingWindowSize = 20;
        private int minimumNumberOfCalls = 10;
        private long waitDurationInOpenStateMillis = 10_000L;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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
    }
}
