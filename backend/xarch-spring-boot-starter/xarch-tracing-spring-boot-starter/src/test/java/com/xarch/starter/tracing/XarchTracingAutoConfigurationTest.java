package com.xarch.starter.tracing;

import com.xarch.starter.tracing.http.RestTemplateTraceInterceptor;
import com.xarch.starter.tracing.http.WebClientTraceFilter;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link XarchTracingAutoConfiguration} wires the expected
 * beans when {@code xarch.tracing.enabled=true} (the default).
 */
class XarchTracingAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(XarchTracingAutoConfiguration.class));

    @Test
    void autoConfigurationRegistersOpenTelemetryBean() {
        runner.withPropertyValues("spring.application.name=xarch-it", "xarch.tracing.exporter=LOGGING")
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenTelemetry.class);
                    assertThat(context).hasSingleBean(RestTemplateTraceInterceptor.class);
                    assertThat(context).hasSingleBean(WebClientTraceFilter.class);
                });
    }

    @Test
    void disabledFlagSkipsAutoConfiguration() {
        runner.withPropertyValues("xarch.tracing.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(OpenTelemetry.class);
            assertThat(context).doesNotHaveBean(RestTemplateTraceInterceptor.class);
        });
    }

    @Test
    void placeholderServiceNameIsResolved() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.application.name", "billing-service");

        String resolved = XarchTracingAutoConfiguration.resolveServiceName(
                new TracingProperties(), environment);

        assertThat(resolved).isEqualTo("billing-service");
    }

    @Test
    void literalServiceNameIsKept() {
        MockEnvironment environment = new MockEnvironment();
        TracingProperties properties = new TracingProperties();
        properties.setServiceName("custom-name");

        assertThat(XarchTracingAutoConfiguration.resolveServiceName(properties, environment))
                .isEqualTo("custom-name");
    }
}