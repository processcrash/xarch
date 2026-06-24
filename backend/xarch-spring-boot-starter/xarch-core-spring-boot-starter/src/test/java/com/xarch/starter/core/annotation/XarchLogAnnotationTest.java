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
 * Unit tests for {@link XarchLog}.
 *
 * <p>Validates meta-annotation targets, retention policy, default values and
 * custom value overrides.</p>
 */
@DisplayName("XarchLog Annotation Tests")
class XarchLogAnnotationTest {

    /**
     * Helper target method that is annotated with {@link XarchLog} using default values.
     */
    @XarchLog
    void methodWithDefaults() {
        // no-op
    }

    /**
     * Helper target method with custom {@link XarchLog} values.
     */
    @XarchLog(value = "Create user", type = "USER")
    void methodWithCustomValues() {
        // no-op
    }

    @Nested
    @DisplayName("Meta-Annotation")
    class MetaAnnotation {

        @Test
        @DisplayName("Target is METHOD")
        void target_isMethod() {
            Target target = XarchLog.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.METHOD);
        }

        @Test
        @DisplayName("Retention is RUNTIME")
        void retention_isRuntime() {
            Retention retention = XarchLog.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Annotation is Documented")
        void annotation_isDocumented() {
            assertThat(XarchLog.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Default value() is empty string")
        void defaultValue_isEmptyString() throws NoSuchMethodException {
            Method method = XarchLogAnnotationTest.class.getDeclaredMethod("methodWithDefaults");
            XarchLog annotation = method.getAnnotation(XarchLog.class);

            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("Default type() is 'OPERATION'")
        void defaultType_isOperation() throws NoSuchMethodException {
            Method method = XarchLogAnnotationTest.class.getDeclaredMethod("methodWithDefaults");
            XarchLog annotation = method.getAnnotation(XarchLog.class);

            assertThat(annotation.type()).isEqualTo("OPERATION");
        }
    }

    @Nested
    @DisplayName("Custom Values")
    class CustomValues {

        @Test
        @DisplayName("Custom value() and type() are reflected on the method")
        void customValues_areReflectedOnMethod() throws NoSuchMethodException {
            Method method = XarchLogAnnotationTest.class.getDeclaredMethod("methodWithCustomValues");
            XarchLog annotation = method.getAnnotation(XarchLog.class);

            assertThat(annotation.value()).isEqualTo("Create user");
            assertThat(annotation.type()).isEqualTo("USER");
        }
    }
}