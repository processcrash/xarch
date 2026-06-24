package com.xarch.starter.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link XarchException}.
 *
 * <p>Verifies that the code is preserved across constructors and the message
 * is exposed through the standard {@link Throwable} contract.</p>
 */
@DisplayName("XarchException Tests")
class XarchExceptionTest {

    @Nested
    @DisplayName("Code And Message")
    class CodeAndMessage {

        @Test
        @DisplayName("Two-arg constructor preserves code and message")
        void twoArgConstructor_preservesCodeAndMessage() {
            XarchException exception = new XarchException("9999", "kaboom");

            assertThat(exception.getCode()).isEqualTo("9999");
            assertThat(exception.getMessage()).isEqualTo("kaboom");
        }

        @Test
        @DisplayName("Three-arg constructor preserves cause chain")
        void threeArgConstructor_preservesCause() {
            Throwable cause = new IllegalStateException("root");
            XarchException exception = new XarchException("9001", "wrapped", cause);

            assertThat(exception.getCode()).isEqualTo("9001");
            assertThat(exception.getMessage()).isEqualTo("wrapped");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Runtime Exception Contract")
    class RuntimeContract {

        @Test
        @DisplayName("XarchException is a RuntimeException")
        void extendsRuntimeException() {
            assertThat(new XarchException("0000", "ok")).isInstanceOf(RuntimeException.class);
        }
    }
}