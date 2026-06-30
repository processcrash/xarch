package com.xarch.starter.resilience.ratelimit;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP aspect that processes the {@link RateLimit} annotation. Falls back
 * to the Resilience4j {@link RateLimiterRegistry} when the request needs
 * to wait longer than {@code timeoutMillis}, otherwise uses the Redis
 * token bucket for a single round-trip check.
 */
@Aspect
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedisRateLimiter redisRateLimiter;
    private final ResilienceProperties properties;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramNames = new DefaultParameterNameDiscoverer();
    private final ConcurrentHashMap<String, io.github.resilience4j.ratelimiter.RateLimiter> waitLimiters = new ConcurrentHashMap<>();

    public RateLimitAspect(RedisRateLimiter redisRateLimiter, ResilienceProperties properties) {
        this(redisRateLimiter, properties, RateLimiterRegistry.ofDefaults());
    }

    public RateLimitAspect(RedisRateLimiter redisRateLimiter,
                            ResilienceProperties properties,
                            RateLimiterRegistry rateLimiterRegistry) {
        this.redisRateLimiter = redisRateLimiter;
        this.properties = properties;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Around("@annotation(rateLimitAnnotation)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimitAnnotation) throws Throwable {
        String bucketKey = resolveBucketKey(pjp, rateLimitAnnotation);
        int permits = Math.max(1, rateLimitAnnotation.permitsPerSecond());
        int burst = Math.max(permits, rateLimitAnnotation.burstCapacity());
        long timeout = rateLimitAnnotation.timeoutMillis() >= 0
                ? rateLimitAnnotation.timeoutMillis()
                : properties.getRateLimit().getTimeoutMillis();

        boolean allowed;
        if (timeout > 0) {
            // Use a Resilience4j waitable limiter — falls back to Redis for
            // the cluster-wide limit but blocks locally up to `timeout`.
            io.github.resilience4j.ratelimiter.RateLimiter limiter = waitLimiters.computeIfAbsent(bucketKey, k ->
                    io.github.resilience4j.ratelimiter.RateLimiter.of(k,
                            io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                                    .limitForPeriod(permits)
                                    .limitRefreshPeriod(java.time.Duration.ofSeconds(1))
                                    .timeoutDuration(java.time.Duration.ofMillis(timeout))
                                    .build()));
            try {
                io.github.resilience4j.ratelimiter.RateLimiter.decorateSupplier(limiter,
                        () -> redisRateLimiter.tryAcquire(bucketKey, 1, permits, burst)).get();
                allowed = true;
            } catch (RequestNotPermitted ex) {
                allowed = false;
            }
        } else {
            allowed = redisRateLimiter.tryAcquire(bucketKey, 1, permits, burst);
        }

        if (!allowed) {
            log.debug("xarch @RateLimit denied for key={} method={}", bucketKey, pjp.getSignature());
            throw RequestNotPermitted.createRequestNotPermitted(
                    io.github.resilience4j.ratelimiter.RateLimiter.of(bucketKey,
                            io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                                    .limitForPeriod(permits)
                                    .build()));
        }
        return pjp.proceed();
    }

    private String resolveBucketKey(ProceedingJoinPoint pjp, RateLimit ann) {
        String key = ann.key();
        if (!key.isBlank()) {
            return evaluateKey(pjp, key);
        }
        return switch (ann.scope()) {
            case IP -> "ip:" + currentRequestIp();
            case USER -> "user:" + currentRequestHeader("X-User-Id", "anonymous");
            case GLOBAL -> "global";
        };
    }

    private String evaluateKey(ProceedingJoinPoint pjp, String expression) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Object[] args = pjp.getArgs();
        EvaluationContext context = new StandardEvaluationContext();
        String[] names = paramNames.getParameterNames(method);
        if (names != null) {
            for (int i = 0; i < names.length && i < args.length; i++) {
                context.setVariable(names[i], args[i]);
            }
        }
        Object value = parser.parseExpression(expression).getValue(context);
        return value == null ? "anon" : value.toString();
    }

    private static String currentRequestIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "internal";
        }
        String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return attrs.getRequest().getRemoteAddr();
    }

    private static String currentRequestHeader(String name, String fallback) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return fallback;
        }
        String value = attrs.getRequest().getHeader(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
