package com.xarch.starter.core.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NotZero}.
 *
 * <p>Verifies the meta-annotation targets, retention policy, and the default
 * {@code message()} of the field/parameter constraint.</p>
 */
@DisplayName("NotZero Annotation Tests")
class NotZeroAnnotationTest {

    @Nested
    @DisplayName("Meta-Annotation")
    class MetaAnnotation {

        @Test
        @DisplayName("Target allows FIELD and PARAMETER")
        void target_allowsFieldAndParameter() {
            Target target = NotZero.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value())
                .contains(ElementType.FIELD)
                .contains(ElementType.PARAMETER);
        }

        @Test
        @DisplayName("Retention is RUNTIME")
        void retention_isRuntime() {
            Retention retention = NotZero.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Annotation is Documented")
        void annotation_isDocumented() {
            assertThat(NotZero.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Default message() is 'Value must not be zero'")
        void defaultMessage_isExpected() throws NoSuchMethodException {
            NotZero annotation = getClass()
                .getDeclaredMethod("annotatedMethod", Long.class)
                .getAnnotation(NotZero.class);

            assertThat(annotation.message()).isEqualTo("Value must not be zero");
        }
    }

    @NotZero
    Long annotatedMethod(@NotZero(message = "must not be zero") Long value) {
        return value;
    }
}