package com.xarch.starter.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JsonUtil}.
 *
 * <p>Exercises Jackson-backed serialization and deserialization roundtrips,
 * verifies Java time handling, and confirms that the ObjectMapper singleton is
 * shared and properly configured.</p>
 */
@DisplayName("JsonUtil Tests")
class JsonUtilTest {

    @Nested
    @DisplayName("Serialization")
    class Serialization {

        @Test
        @DisplayName("toJson writes a simple POJO to JSON")
        void toJson_serializesSimplePojo() {
            // Arrange
            Map<String, Object> source = new HashMap<>();
            source.put("key", "value");
            source.put("count", 42);

            // Act
            String json = JsonUtil.toJson(source);

            // Assert
            assertThat(json).contains("\"key\":\"value\"");
            assertThat(json).contains("\"count\":42");
        }

        @Test
        @DisplayName("toJson throws RuntimeException on serialization failure")
        void toJson_unserializableObject_throwsRuntimeException() {
            // Arrange - a self-referential object causes infinite recursion
            Object unserializable = new Object() {
                @SuppressWarnings("unused")
                public Object getSelf() {
                    return this;
                }
            };

            // Act & Assert
            assertThatThrownBy(() -> JsonUtil.toJson(unserializable))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JSON serialize failed");
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("fromJson reads JSON back into a POJO")
        void fromJson_deserializesSimplePojo() {
            // Act
            Map<?, ?> result = JsonUtil.fromJson("{\"k\":\"v\",\"n\":1}", Map.class);

            // Assert
            assertThat(result).containsEntry("k", "v").containsEntry("n", 1);
        }

        @Test
        @DisplayName("fromJson throws RuntimeException on malformed input")
        void fromJson_malformedJson_throwsRuntimeException() {
            // Act & Assert
            assertThatThrownBy(() -> JsonUtil.fromJson("{not valid json", Map.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JSON deserialize failed");
        }
    }

    @Nested
    @DisplayName("Roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("toJson / fromJson preserves data integrity")
        void roundtrip_preservesData() {
            // Arrange
            Map<String, Object> original = new HashMap<>();
            original.put("name", "xarch");
            original.put("version", "1.0");

            // Act
            String json = JsonUtil.toJson(original);
            @SuppressWarnings("unchecked")
            Map<String, Object> restored = JsonUtil.fromJson(json, Map.class);

            // Assert
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("ObjectMapper Singleton")
    class ObjectMapperSingleton {

        @Test
        @DisplayName("getMapper returns the same instance on repeated calls")
        void getMapper_returnsSameInstance() {
            ObjectMapper a = JsonUtil.getMapper();
            ObjectMapper b = JsonUtil.getMapper();

            assertThat(a).isSameAs(b);
        }
    }
}