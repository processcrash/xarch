package com.xarch.starter.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link XarchTraceContext}.
 */
class XarchTraceContextTest {

    private InMemorySpanExporter exporter;
    private SdkTracerProvider provider;

    @BeforeEach
    void setUp() {
        exporter = InMemorySpanExporter.create();
        provider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        // Register the SDK as the global OpenTelemetry instance.
        OpenTelemetrySdk.builder()
                .setTracerProvider(provider)
                .buildAndRegisterGlobal();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        provider.close();
        MDC.clear();
    }

    @Test
    void currentIdsEmptyWhenNoSpanActive() {
        assertThat(XarchTraceContext.currentTraceId()).isEmpty();
        assertThat(XarchTraceContext.currentSpanId()).isEmpty();
    }

    @Test
    void withSpanRunnablePushesMdc() {
        XarchTraceContext.withSpan("do-work", () -> {
            assertThat(XarchTraceContext.currentTraceId()).isNotEmpty();
            assertThat(XarchTraceContext.currentSpanId()).isNotEmpty();
            assertThat(MDC.get(XarchTraceContext.MDC_TRACE_ID)).isEqualTo(XarchTraceContext.currentTraceId());
            assertThat(MDC.get(XarchTraceContext.MDC_SPAN_ID)).isEqualTo(XarchTraceContext.currentSpanId());
        });
        // After the span closes, the helpers fall back to the empty string and MDC is cleared.
        assertThat(XarchTraceContext.currentTraceId()).isEmpty();
        assertThat(MDC.get(XarchTraceContext.MDC_TRACE_ID)).isNull();
    }

    @Test
    void withSpanSupplierReturnsValue() {
        String result = XarchTraceContext.withSpan("compute", () -> "ok");
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void exceptionInWithSpanIsRecordedAndRethrown() {
        assertThatThrownBy(() -> XarchTraceContext.withSpan("boom", () -> {
            throw new IllegalStateException("kaboom");
        })).isInstanceOf(IllegalStateException.class).hasMessage("kaboom");

        assertThat(exporter.getFinishedSpanItems()).hasSize(1);
        assertThat(exporter.getFinishedSpanItems().get(0).getStatus().getStatusCode().name())
                .isEqualTo("ERROR");
    }

    @Test
    void mdcIsRestoredAfterSpan() {
        MDC.put(XarchTraceContext.MDC_TRACE_ID, "pre-existing-trace");
        MDC.put(XarchTraceContext.MDC_SPAN_ID, "pre-existing-span");

        XarchTraceContext.withSpan("inner", () -> {
            // Inside the lambda MDC reflects the inner span.
            assertThat(MDC.get(XarchTraceContext.MDC_TRACE_ID)).isNotEqualTo("pre-existing-trace");
        });

        // After the span closes, the previous MDC values are restored.
        assertThat(MDC.get(XarchTraceContext.MDC_TRACE_ID)).isEqualTo("pre-existing-trace");
        assertThat(MDC.get(XarchTraceContext.MDC_SPAN_ID)).isEqualTo("pre-existing-span");
    }
}