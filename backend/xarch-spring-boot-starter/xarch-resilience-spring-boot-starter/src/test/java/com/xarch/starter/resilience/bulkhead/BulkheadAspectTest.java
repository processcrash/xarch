package com.xarch.starter.resilience.bulkhead;

import com.xarch.starter.resilience.ResilienceProperties;
import com.xarch.starter.resilience.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BulkheadAspect}. Exercises the aspect against
 * the in-process bulkhead registry, triggering the {@code full}
 * condition by submitting more concurrent invocations than the cap.
 */
class BulkheadAspectTest {

    @Bulkhead(name = "test-bulkhead", concurrentCalls = 2, maxWait = 0)
    public String gated() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "ok";
    }

    @Test
    void bulkheadRejectsAfterCapIsReached() {
        BulkheadRegistry registry = BulkheadRegistry.of(
                BulkheadConfig.custom().maxConcurrentCalls(2).maxWaitDuration(java.time.Duration.ZERO).build());
        BulkheadAspect aspect = new BulkheadAspect(registry);

        AspectJProxyFactory factory = new AspectJProxyFactory(this);
        factory.addAspect(aspect);
        BulkheadAspectTest proxy = factory.getProxy();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger full = new AtomicInteger();
        try {
            CompletableFuture<?>[] futures = new CompletableFuture[8];
            for (int i = 0; i < futures.length; i++) {
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        proxy.gated();
                        ok.incrementAndGet();
                    } catch (BulkheadFullException ex) {
                        full.incrementAndGet();
                    }
                }, pool);
            }
            CompletableFuture.allOf(futures).join();
        } finally {
            pool.shutdownNow();
        }

        assertThat(ok.get()).isGreaterThanOrEqualTo(1);
        assertThat(full.get()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void defaultsAreSensible() {
        ResilienceProperties p = new ResilienceProperties();
        assertThat(p.getBulkhead().getConcurrentCalls()).isEqualTo(25);
        assertThat(p.getBulkhead().getMaxWaitDurationMillis()).isZero();
    }

    @Test
    void singleCallAlwaysAllowed() {
        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        BulkheadAspect aspect = new BulkheadAspect(registry);

        AspectJProxyFactory factory = new AspectJProxyFactory(this);
        factory.addAspect(aspect);
        BulkheadAspectTest proxy = factory.getProxy();

        // Configure default bulkhead with high cap
        registry.bulkhead("default",
                BulkheadConfig.custom().maxConcurrentCalls(100).build());

        assertThat(proxy.gated()).isEqualTo("ok");
    }

    @Test
    void annotationValuesAreSensible() {
        Bulkhead ann = BulkheadAspectTest.class.getDeclaredMethods()[0]
                .getAnnotation(Bulkhead.class);
        assertThat(ann.concurrentCalls()).isEqualTo(2);
        assertThat(ann.maxWait()).isZero();
        assertThat(ann.name()).isEqualTo("test-bulkhead");
    }
}
