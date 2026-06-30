package com.xarch.starter.resilience.retry;

import com.xarch.starter.resilience.annotation.Retry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP aspect that applies a Resilience4j retry policy with exponential
 * backoff to the annotated method.
 *
 * <p>Each {@link Retry#maxAttempts()} / {@link Retry#delay()} /
 * {@link Retry#multiplier()} combination is registered as a single
 * named retry in the registry, so the configuration is shared across
 * all methods that declare the same values.
 */
@Aspect
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    private final RetryRegistry registry;
    private final RetryListener listener;
    private final ConcurrentHashMap<String, Boolean> registered = new ConcurrentHashMap<>();

    public RetryAspect(RetryRegistry registry, RetryListener listener) {
        this.registry = registry;
        this.listener = listener;
    }

    @Around("@annotation(retryAnnotation)")
    public Object around(ProceedingJoinPoint pjp, Retry retryAnnotation) throws Throwable {
        String name = nameFor(pjp, retryAnnotation);
        io.github.resilience4j.retry.Retry retry = ensureRegistered(name, retryAnnotation);

        String methodName = pjp.getSignature().toShortString();
        int[] counter = {0};
        try {
            return io.github.resilience4j.retry.Retry.decorateCheckedSupplier(retry, () -> {
                counter[0]++;
                try {
                    return pjp.proceed();
                } catch (RuntimeException re) {
                    throw re;
                } catch (Throwable t) {
                    throw new java.io.UncheckedIOException(
                            new java.io.IOException(t));
                }
            }).get();
        } catch (RuntimeException ex) {
            Throwable cause = unwrap(ex);
            if (counter[0] > 1) {
                listener.onError(methodName, counter[0], cause);
            }
            if (cause instanceof Exception exc) {
                throw exc;
            }
            throw ex;
        }
    }

    private io.github.resilience4j.retry.Retry ensureRegistered(String name, Retry ann) {
        registered.computeIfAbsent(name, n -> {
            try {
                if (registry.find(n).isPresent()) {
                    return Boolean.TRUE;
                }
                RetryConfig config = RetryConfig.custom()
                        .maxAttempts(Math.max(1, ann.maxAttempts()))
                        .intervalFunction(IntervalFunction.ofExponentialBackoff(
                                Math.max(1, ann.delay()), ann.multiplier(),
                                Math.max(ann.delay(), ann.maxDelay())))
                        .retryOnException(t -> t instanceof RuntimeException)
                        .build();
                registry.retry(n, config);
            } catch (Exception ex) {
                log.warn("xarch retry registration failed for {}: {}", n, ex.getMessage());
            }
            return Boolean.TRUE;
        });
        return registry.retry(name);
    }

    private static String nameFor(ProceedingJoinPoint pjp, Retry ann) {
        return pjp.getSignature().toShortString() + "::"
                + ann.maxAttempts() + ":" + ann.delay() + ":" + ann.multiplier();
    }

    private static Throwable unwrap(Throwable t) {
        Throwable cur = t;
        for (int i = 0; i < 5 && cur != null; i++) {
            if (cur.getCause() == null || cur.getCause() == cur) {
                return cur;
            }
            cur = cur.getCause();
        }
        return t;
    }
}
