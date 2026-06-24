package com.xarch.starter.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link XarchCacheAutoConfiguration}.
 *
 * <p>Verifies that the cache auto-configuration registers its placeholder
 * beans in a Spring application context.</p>
 */
@DisplayName("XarchCacheAutoConfiguration Tests")
class XarchCacheAutoConfigurationTest {

    private XarchCacheAutoConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new XarchCacheAutoConfiguration();
    }

    @Nested
    @DisplayName("Placeholder Bean")
    class PlaceholderBean {

        @Test
        @DisplayName("cachePlaceholder bean returns the expected placeholder value")
        void cachePlaceholder_returnsExpectedValue() {
            assertThat(configuration.cachePlaceholder()).isEqualTo("xarch-cache-placeholder");
        }
    }

    @Nested
    @DisplayName("Spring Context")
    class SpringContext {

        @Test
        @DisplayName("The auto-configuration registers the placeholder bean")
        void autoConfiguration_registersPlaceholderBean() {
            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
                context.register(XarchCacheAutoConfiguration.class);
                context.refresh();

                assertThat(context.containsBean("cachePlaceholder")).isTrue();
                assertThat(context.getBean("cachePlaceholder")).isEqualTo("xarch-cache-placeholder");
            }
        }
    }
}