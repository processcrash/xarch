package com.xarch.starter.resilience.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Listener that logs every retry attempt. Wired into the
 * {@link RetryAspect}'s Retry events. Stateless and safe to share
 * across threads.
 */
public class RetryListener {

    private static final Logger log = LoggerFactory.getLogger(RetryListener.class);

    /** Number of retries triggered since process start. */
    private final AtomicLong retriesTriggered = new AtomicLong();
    /** Number of retries that ultimately succeeded. */
    private final AtomicLong retriesSucceeded = new AtomicLong();
    /** Number of retries that ultimately failed. */
    private final AtomicLong retriesExhausted = new AtomicLong();

    public void onRetry(String name, int attempt, Throwable cause) {
        retriesTriggered.incrementAndGet();
        if (log.isDebugEnabled()) {
            log.debug("xarch @Retry[{}] attempt #{} failed: {}", name, attempt,
                    cause == null ? "n/a" : cause.getMessage());
        }
    }

    public void onSuccess(String name, int totalAttempts) {
        if (totalAttempts > 1) {
            retriesSucceeded.incrementAndGet();
        }
        if (log.isDebugEnabled()) {
            log.debug("xarch @Retry[{}] succeeded after {} attempt(s)", name, totalAttempts);
        }
    }

    public void onError(String name, int totalAttempts, Throwable cause) {
        retriesExhausted.incrementAndGet();
        log.warn("xarch @Retry[{}] exhausted after {} attempt(s): {}",
                name, totalAttempts, cause == null ? "n/a" : cause.getMessage());
    }

    public long getRetriesTriggered() {
        return retriesTriggered.get();
    }

    public long getRetriesSucceeded() {
        return retriesSucceeded.get();
    }

    public long getRetriesExhausted() {
        return retriesExhausted.get();
    }
}
