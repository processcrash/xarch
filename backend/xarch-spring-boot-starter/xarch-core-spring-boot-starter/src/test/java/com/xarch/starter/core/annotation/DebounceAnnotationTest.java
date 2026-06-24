package com.xarch.starter.core.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Debounce}.
 *
 * <p>Validates meta-annotation targets, retention policy, default values and
 * custom value overrides for the debounce annotation.</p>
 */
@DisplayName("Debounce Annotation Tests")
class DebounceAnnotationTest {

    @Debounce
    void methodWithDefaults() {
        // no-op
    }

    @Debounce(key = "user:login", timeout = 5000L)
    void methodWithCustomValues() {
        // no-op
    }

    @Nested
    @DisplayName("Meta-Annotation")
    class MetaAnnotation {

        @Test
        @DisplayName("Target is METHOD")
        void target_isMethod() {
            Target target = Debounce.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.METHOD);
        }

        @Test
        @DisplayName("Retention is RUNTIME")
        void retention_isRuntime() {
            Retention retention = Debounce.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Annotation is Documented")
        void annotation_isDocumented() {
            assertThat(Debounce.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Default key() is empty string")
        void defaultKey_isEmptyString() throws NoSuchMethodException {
            Method method = DebounceAnnotationTest.class.getDeclaredMethod("methodWithDefaults");
            Debounce annotation = method.getAnnotation(Debounce.class);

            assertThat(annotation.key()).isEmpty();
        }

        @Test
        @DisplayName("Default timeout() is 3000 milliseconds")
        void defaultTimeout_is3000() throws NoSuchMethodException {
            Method method = DebounceAnnotationTest.class.getDeclaredMethod("methodWithDefaults");
            Debounce annotation = method.getAnnotation(Debounce.class);

            assertThat(annotation.timeout()).isEqualTo(3000L);
        }
    }

    @Nested
    @DisplayName("Custom Values")
    class CustomValues {

        @Test
        @DisplayName("Custom key() and timeout() are reflected on the method")
        void customValues_areReflectedOnMethod() throws NoSuchMethodException {
            Method method = DebounceAnnotationTest.class.getDeclaredMethod("methodWithCustomValues");
            Debounce annotation = method.getAnnotation(Debounce.class);

            assertThat(annotation.key()).isEqualTo("user:login");
            assertThat(annotation.timeout()).isEqualTo(5000L);
        }
    }
}