package com.xarch.starter.tracing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TracingProperties} defaults and simple setters.
 */
class TracingPropertiesTest {

    @Test
    void defaultsAreSensible() {
        TracingProperties properties = new TracingProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getServiceName()).isEqualTo("${spring.application.name}");
        assertThat(properties.getExporter()).isEqualTo(TracingProperties.Exporter.OTLP);
        assertThat(properties.getEndpoint()).isEqualTo(TracingProperties.DEFAULT_OTLP_ENDPOINT);
        assertThat(properties.getSampleRate()).isEqualTo(1.0);
        assertThat(properties.getPropagators()).containsExactly("tracecontext", "baggage");
        assertThat(properties.getResourceAttributes()).isEmpty();
    }

    @Test
    void exporterEnumRoundTrips() {
        for (TracingProperties.Exporter exporter : TracingProperties.Exporter.values()) {
            TracingProperties properties = new TracingProperties();
            properties.setExporter(exporter);
            assertThat(properties.getExporter()).isEqualTo(exporter);
        }
    }

    @Test
    void sampleRateIsNotClampedBySetter() {
        // The clamping lives in the auto-configuration's sampler builder.
        TracingProperties properties = new TracingProperties();
        properties.setSampleRate(0.25);
        assertThat(properties.getSampleRate()).isEqualTo(0.25);
    }
}