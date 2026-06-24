package com.xarch.starter.core.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ApiResult}.
 *
 * <p>Verifies factory methods, constructor behavior, and Jackson serialization
 * roundtripping of the unified API response wrapper.</p>
 */
@DisplayName("ApiResult Tests")
class ApiResultTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("ok() returns success code '0000'")
        void ok_returnsSuccessCode() {
            // Arrange & Act
            ApiResult<String> result = ApiResult.ok();

            // Assert
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("ok(data) includes provided data payload")
        void ok_withData_includesPayload() {
            // Arrange & Act
            ApiResult<String> result = ApiResult.ok("payload");

            // Assert
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isEqualTo("payload");
        }

        @Test
        @DisplayName("success() is an alias of ok()")
        void success_isAliasOfOk() {
            ApiResult<Integer> result = ApiResult.success();
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("success(data) is an alias of ok(data)")
        void success_withData_isAliasOfOkWithData() {
            ApiResult<Integer> result = ApiResult.success(42);
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getData()).isEqualTo(42);
        }

        @Test
        @DisplayName("fail(message) returns code '9999'")
        void fail_withMessageOnly_returnsCode9999() {
            ApiResult<Void> result = ApiResult.fail("something went wrong");
            assertThat(result.getCode()).isEqualTo("9999");
            assertThat(result.getMessage()).isEqualTo("something went wrong");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("fail(code, message) returns custom code and message")
        void fail_withCodeAndMessage_returnsCustomCode() {
            ApiResult<Void> result = ApiResult.fail("4000", "Bad Request");
            assertThat(result.getCode()).isEqualTo("4000");
            assertThat(result.getMessage()).isEqualTo("Bad Request");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("fail(code, message, data) returns custom code with data payload")
        void fail_withCodeMessageAndData_includesPayload() {
            ApiResult<String> result = ApiResult.fail("5000", "Server Error", "details");
            assertThat(result.getCode()).isEqualTo("5000");
            assertThat(result.getMessage()).isEqualTo("Server Error");
            assertThat(result.getData()).isEqualTo("details");
        }
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Default constructor sets success defaults")
        void defaultConstructor_setsSuccessDefaults() {
            ApiResult<String> result = new ApiResult<>();
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isNull();
            assertThat(result.getTimestamp()).isNotNull();
            assertThat(result.getTimestamp()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Two-arg constructor sets code and message only")
        void twoArgConstructor_setsCodeAndMessage() {
            ApiResult<String> result = new ApiResult<>("4010", "Unauthorized");
            assertThat(result.getCode()).isEqualTo("4010");
            assertThat(result.getMessage()).isEqualTo("Unauthorized");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("Three-arg constructor sets code, message, and data")
        void threeArgConstructor_setsAllFields() {
            ApiResult<Integer> result = new ApiResult<>("0000", "SUCCESS", 123);
            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("Serialization")
    class Serialization {

        @Test
        @DisplayName("Serialization roundtrip preserves all fields")
        void jackson_roundtrip_preservesAllFields() throws Exception {
            // Arrange
            ApiResult<String> original = ApiResult.ok("payload");

            // Act
            String json = objectMapper.writeValueAsString(original);
            ApiResult<String> deserialized = objectMapper.readValue(json, ApiResult.class);

            // Assert
            assertThat(deserialized.getCode()).isEqualTo(original.getCode());
            assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
            assertThat(deserialized.getData()).isEqualTo(original.getData());
            assertThat(deserialized.getTimestamp()).isEqualTo(original.getTimestamp());
        }

        @Test
        @DisplayName("Serialization roundtrip preserves failure result")
        void jackson_roundtrip_preservesFailureResult() throws Exception {
            ApiResult<Void> original = ApiResult.fail("5000", "Internal Error");

            String json = objectMapper.writeValueAsString(original);
            ApiResult<?> deserialized = objectMapper.readValue(json, ApiResult.class);

            assertThat(deserialized.getCode()).isEqualTo("5000");
            assertThat(deserialized.getMessage()).isEqualTo("Internal Error");
        }
    }
}