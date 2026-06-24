package com.xarch.starter.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IdUtil}.
 *
 * <p>Exercises UUID generation and Hutool-backed Snowflake ID creation, including
 * uniqueness across many iterations and concurrent access from multiple threads.</p>
 */
@DisplayName("IdUtil Tests")
class IdUtilTest {

    @Nested
    @DisplayName("UUID Generation")
    class UuidGeneration {

        @Test
        @DisplayName("uuid() returns a 32-character hex string with no dashes")
        void uuid_returns32CharHexString() {
            String id = IdUtil.uuid();

            assertThat(id).hasSize(32);
            assertThat(id).matches("[0-9a-f]{32}");
            assertThat(id).doesNotContain("-");
        }

        @Test
        @DisplayName("1000 UUIDs are unique")
        void uuid_uniqueAcross1000Calls() {
            // Arrange
            int count = 1000;
            Set<String> ids = new HashSet<>(count);

            // Act
            for (int i = 0; i < count; i++) {
                ids.add(IdUtil.uuid());
            }

            // Assert
            assertThat(ids).hasSize(count);
        }

        @Test
        @DisplayName("uuid() is safe under concurrent access")
        void uuid_concurrentAccess_producesUniqueIds() throws InterruptedException {
            // Arrange
            int threads = 8;
            int perThread = 500;
            Set<String> ids = ConcurrentHashMap.newKeySet();
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            // Act
            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < perThread; i++) {
                            ids.add(IdUtil.uuid());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Assert
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            assertThat(ids).hasSize(threads * perThread);
        }
    }

    @Nested
    @DisplayName("Snowflake ID Generation")
    class SnowflakeIdGeneration {

        @Test
        @DisplayName("snowflakeId() returns a positive long")
        void snowflakeId_returnsPositiveLong() {
            long id = IdUtil.snowflakeId();

            assertThat(id).isPositive();
        }

        @Test
        @DisplayName("snowflakeIdStr() returns the decimal string representation")
        void snowflakeIdStr_returnsStringRepresentation() {
            long id = IdUtil.snowflakeId();

            assertThat(IdUtil.snowflakeIdStr()).isEqualTo(String.valueOf(id));
            assertThat(IdUtil.snowflakeIdStr()).matches("\\d+");
        }

        @Test
        @DisplayName("1000 Snowflake IDs are unique")
        void snowflakeId_uniqueAcross1000Calls() {
            // Arrange
            int count = 1000;
            Set<Long> ids = new HashSet<>(count);

            // Act
            for (int i = 0; i < count; i++) {
                ids.add(IdUtil.snowflakeId());
            }

            // Assert
            assertThat(ids).hasSize(count);
        }

        @Test
        @DisplayName("Snowflake IDs are monotonic (strictly increasing across calls)")
        void snowflakeId_isMonotonic() {
            // Act
            long previous = IdUtil.snowflakeId();
            long current = IdUtil.snowflakeId();

            // Assert
            assertThat(current).isGreaterThan(previous);
        }

        @Test
        @DisplayName("Snowflake IDs are unique under concurrent generation")
        void snowflakeId_concurrentAccess_producesUniqueIds() throws InterruptedException {
            // Arrange
            int threads = 8;
            int perThread = 250;
            Set<Long> ids = ConcurrentHashMap.newKeySet();
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            // Act
            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    try {
                        IntStream.range(0, perThread).forEach(i -> ids.add(IdUtil.snowflakeId()));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Assert
            assertThat(latch.await(20, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            assertThat(ids).hasSize(threads * perThread);
        }
    }
}