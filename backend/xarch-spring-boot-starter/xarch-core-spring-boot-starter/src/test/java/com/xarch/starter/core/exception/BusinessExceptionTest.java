package com.xarch.starter.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BusinessException}.
 *
 * <p>Validates the code/message behavior and confirms that
 * {@link BusinessException} is a {@link RuntimeException} suitable for use
 * with Spring's exception handler chain.</p>
 */
@DisplayName("BusinessException Tests")
class BusinessExceptionTest {

    @Nested
    @DisplayName("Code And Message")
    class CodeAndMessage {

        @Test
        @DisplayName("Two-arg constructor preserves code and message")
        void twoArgConstructor_preservesCodeAndMessage() {
            BusinessException exception = new BusinessException("4100", "Conflict");

            assertThat(exception.getCode()).isEqualTo("4100");
            assertThat(exception.getMessage()).isEqualTo("Conflict");
        }

        @Test
        @DisplayName("Three-arg constructor preserves cause chain")
        void threeArgConstructor_preservesCause() {
            Throwable cause = new IllegalArgumentException("root");
            BusinessException exception = new BusinessException("4200", "validation", cause);

            assertThat(exception.getCode()).isEqualTo("4200");
            assertThat(exception.getMessage()).isEqualTo("validation");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Runtime Exception Contract")
    class RuntimeContract {

        @Test
        @DisplayName("BusinessException is a RuntimeException")
        void extendsRuntimeException() {
            assertThat(new BusinessException("0000", "ok")).isInstanceOf(RuntimeException.class);
        }
    }
}