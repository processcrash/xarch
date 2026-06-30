package com.xarch.starter.resilience.bulkhead;

import com.xarch.starter.resilience.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP aspect that enforces a hard concurrency cap on the annotated
 * method. Uses a Resilience4j semaphore bulkhead; when the cap is
 * reached, the call fails fast with {@link BulkheadFullException}.
 */
@Aspect
public class BulkheadAspect {

    private static final Logger log = LoggerFactory.getLogger(BulkheadAspect.class);

    private final BulkheadRegistry registry;
    private final ConcurrentHashMap<String, Boolean> registered = new ConcurrentHashMap<>();

    public BulkheadAspect(BulkheadRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(bulkheadAnnotation)")
    public Object around(ProceedingJoinPoint pjp, Bulkhead bulkheadAnnotation) throws Throwable {
        String name = bulkheadAnnotation.name().isBlank()
                ? pjp.getSignature().toShortString()
                : bulkheadAnnotation.name();
        io.github.resilience4j.bulkhead.Bulkhead bulkhead = ensureRegistered(name, bulkheadAnnotation);

        if (!bulkhead.tryAcquirePermission()) {
            log.debug("xarch @Bulkhead[{}] full, rejecting {}", name, pjp.getSignature());
            throw BulkheadFullException.createBulkheadFullException(bulkhead);
        }
        try {
            return pjp.proceed();
        } finally {
            bulkhead.releasePermission();
        }
    }

    private io.github.resilience4j.bulkhead.Bulkhead ensureRegistered(String name, Bulkhead ann) {
        registered.computeIfAbsent(name, n -> {
            try {
                if (registry.find(n).isPresent()) {
                    return Boolean.TRUE;
                }
                int concurrent = ann.concurrentCalls() > 0
                        ? ann.concurrentCalls()
                        : 25;
                long wait = ann.maxWait() >= 0 ? ann.maxWait() : 0L;
                BulkheadConfig cfg = BulkheadConfig.custom()
                        .maxConcurrentCalls(concurrent)
                        .maxWaitDuration(Duration.ofMillis(wait))
                        .build();
                registry.bulkhead(n, cfg);
            } catch (Exception ex) {
                log.warn("xarch bulkhead registration failed for {}: {}", n, ex.getMessage());
            }
            return Boolean.TRUE;
        });
        return registry.bulkhead(name);
    }
}
