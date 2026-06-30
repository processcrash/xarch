package com.xarch.starter.resilience.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RetryListener} counters.
 */
class RetryListenerTest {

    @Test
    void countersStartAtZero() {
        RetryListener listener = new RetryListener();
        assertThat(listener.getRetriesTriggered()).isZero();
        assertThat(listener.getRetriesSucceeded()).isZero();
        assertThat(listener.getRetriesExhausted()).isZero();
    }

    @Test
    void countersUpdate() {
        RetryListener listener = new RetryListener();
        listener.onRetry("m1", 1, new RuntimeException("boom"));
        listener.onRetry("m1", 2, new RuntimeException("boom"));
        listener.onSuccess("m1", 3);
        listener.onError("m1", 3, new RuntimeException("boom"));

        assertThat(listener.getRetriesTriggered()).isEqualTo(2);
        assertThat(listener.getRetriesSucceeded()).isEqualTo(1);
        assertThat(listener.getRetriesExhausted()).isEqualTo(1);
    }
}
