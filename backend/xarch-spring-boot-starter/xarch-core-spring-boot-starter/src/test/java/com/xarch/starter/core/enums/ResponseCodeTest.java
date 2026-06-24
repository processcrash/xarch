package com.xarch.starter.core.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ResponseCode}.
 *
 * <p>Verifies the code/message mapping for each enum constant.</p>
 */
@DisplayName("ResponseCode Tests")
class ResponseCodeTest {

    @Nested
    @DisplayName("Enum Constants")
    class EnumConstants {

        @Test
        @DisplayName("SUCCESS maps to '0000' / 'SUCCESS'")
        void success_hasExpectedValues() {
            assertThat(ResponseCode.SUCCESS.getCode()).isEqualTo("0000");
            assertThat(ResponseCode.SUCCESS.getMessage()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("BAD_REQUEST maps to '4000' / 'Bad Request'")
        void badRequest_hasExpectedValues() {
            assertThat(ResponseCode.BAD_REQUEST.getCode()).isEqualTo("4000");
            assertThat(ResponseCode.BAD_REQUEST.getMessage()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("UNAUTHORIZED maps to '4010' / 'Unauthorized'")
        void unauthorized_hasExpectedValues() {
            assertThat(ResponseCode.UNAUTHORIZED.getCode()).isEqualTo("4010");
            assertThat(ResponseCode.UNAUTHORIZED.getMessage()).isEqualTo("Unauthorized");
        }

        @Test
        @DisplayName("FORBIDDEN maps to '4030' / 'Forbidden'")
        void forbidden_hasExpectedValues() {
            assertThat(ResponseCode.FORBIDDEN.getCode()).isEqualTo("4030");
            assertThat(ResponseCode.FORBIDDEN.getMessage()).isEqualTo("Forbidden");
        }

        @Test
        @DisplayName("NOT_FOUND maps to '4040' / 'Not Found'")
        void notFound_hasExpectedValues() {
            assertThat(ResponseCode.NOT_FOUND.getCode()).isEqualTo("4040");
            assertThat(ResponseCode.NOT_FOUND.getMessage()).isEqualTo("Not Found");
        }

        @Test
        @DisplayName("INTERNAL_ERROR maps to '5000' / 'Internal Server Error'")
        void internalError_hasExpectedValues() {
            assertThat(ResponseCode.INTERNAL_ERROR.getCode()).isEqualTo("5000");
            assertThat(ResponseCode.INTERNAL_ERROR.getMessage()).isEqualTo("Internal Server Error");
        }
    }

    @Nested
    @DisplayName("Enum Identity")
    class EnumIdentity {

        @Test
        @DisplayName("All six constants are present")
        void allConstants_arePresent() {
            assertThat(ResponseCode.values()).hasSize(6);
        }

        @Test
        @DisplayName("valueOf returns the expected constant")
        void valueOf_returnsExpectedConstant() {
            assertThat(ResponseCode.valueOf("SUCCESS")).isEqualTo(ResponseCode.SUCCESS);
            assertThat(ResponseCode.valueOf("INTERNAL_ERROR")).isEqualTo(ResponseCode.INTERNAL_ERROR);
        }
    }
}