package com.xarch.starter.resilience.circuitbreaker;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP aspect that applies a named Resilience4j circuit breaker to the
 * annotated method. If the breaker is not yet registered, a default one
 * is created from the {@link ResilienceProperties} and added to the
 * registry (idempotent).
 */
@Aspect
public class CircuitBreakerAspect {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerAspect.class);

    private final CircuitBreakerRegistry registry;
    private final CircuitBreakerFallbackHandler fallbackHandler;
    private final ConcurrentHashMap<String, Boolean> registered = new ConcurrentHashMap<>();

    public CircuitBreakerAspect(CircuitBreakerRegistry registry,
                                 CircuitBreakerFallbackHandler fallbackHandler) {
        this.registry = registry;
        this.fallbackHandler = fallbackHandler;
    }

    @Around("@annotation(breakerAnnotation)")
    public Object around(ProceedingJoinPoint pjp, CircuitBreaker breakerAnnotation) throws Throwable {
        io.github.resilience4j.circuitbreaker.CircuitBreaker breaker =
                breakerFor(breakerAnnotation);
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        try {
            return io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(breaker,
                    () -> {
                        try {
                            return pjp.proceed();
                        } catch (RuntimeException re) {
                            throw re;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    }).get();
        } catch (RuntimeException ex) {
            log.debug("xarch @CircuitBreaker[{}] short-circuited: {}",
                    breakerAnnotation.name(), ex.getMessage());
            return invokeFallback(pjp, method, breakerAnnotation, ex);
        }
    }

    private io.github.resilience4j.circuitbreaker.CircuitBreaker breakerFor(CircuitBreaker ann) {
        // registerIfAbsent — first invocation sets up a default config, later
        // calls reuse whatever the user has configured under
        // resilience4j.circuitbreaker.instances.<name>.
        registered.computeIfAbsent(ann.name(), n -> {
            try {
                if (registry.find(n).isPresent()) {
                    return Boolean.TRUE;
                }
                CircuitBreakerConfig base = registry.getConfiguration("default")
                        .orElseGet(() -> CircuitBreakerConfig.ofDefaults());
                CircuitBreakerConfig.Builder b = CircuitBreakerConfig.from(base);
                if (ann.slidingWindowSize() > 0) {
                    b.slidingWindowSize(ann.slidingWindowSize());
                }
                if (ann.failureRateThreshold() >= 0) {
                    b.failureRateThreshold(ann.failureRateThreshold());
                }
                if (ann.waitDurationInOpenStateMillis() >= 0) {
                    b.waitDurationInOpenState(Duration.ofMillis(ann.waitDurationInOpenStateMillis()));
                }
                registry.circuitBreaker(n, b.build());
            } catch (Exception ex) {
                log.warn("xarch circuit breaker registration failed for {}: {}",
                        n, ex.getMessage());
            }
            return Boolean.TRUE;
        });
        return registry.circuitBreaker(ann.name());
    }

    private Object invokeFallback(ProceedingJoinPoint pjp, Method method,
                                   CircuitBreaker ann, Throwable cause) {
        if (ann.fallback() == null || ann.fallback().isBlank()) {
            return fallbackHandler.defaultFallback(method.getReturnType(), cause);
        }
        Method fallbackMethod = findFallback(pjp, ann.fallback(), method);
        if (fallbackMethod == null) {
            log.warn("xarch @CircuitBreaker[{}] fallback '{}' not found on {}",
                    ann.name(), ann.fallback(), pjp.getTarget().getClass().getName());
            return fallbackHandler.defaultFallback(method.getReturnType(), cause);
        }
        try {
            fallbackMethod.setAccessible(true);
            Object target = pjp.getTarget();
            Object[] args = pjp.getArgs();
            // Allow fallback signature: (origArgs..., Throwable) or (origArgs...).
            if (fallbackMethod.getParameterCount() == method.getParameterCount() + 1) {
                Throwable[] appended = Arrays.copyOf(args, args.length + 1, Throwable[].class);
                appended[args.length] = cause;
                return fallbackMethod.invoke(target, appended);
            }
            return fallbackMethod.invoke(target, args);
        } catch (Exception ex) {
            log.warn("xarch @CircuitBreaker[{}] fallback '{}' threw: {}",
                    ann.name(), ann.fallback(), ex.getMessage());
            return fallbackHandler.defaultFallback(method.getReturnType(), cause);
        }
    }

    private Method findFallback(ProceedingJoinPoint pjp, String name, Method original) {
        Class<?> target = pjp.getTarget().getClass();
        for (Method m : target.getDeclaredMethods()) {
            if (!m.getName().equals(name)) {
                continue;
            }
            int originalParams = original.getParameterCount();
            if (m.getParameterCount() == originalParams
                    || m.getParameterCount() == originalParams + 1) {
                if (m.getReturnType().isAssignableFrom(original.getReturnType())
                        || original.getReturnType().isAssignableFrom(m.getReturnType())) {
                    return m;
                }
            }
        }
        return null;
    }

    /** Visible for tests. */
    List<String> registeredNames() {
        return List.copyOf(registered.keySet());
    }
}
