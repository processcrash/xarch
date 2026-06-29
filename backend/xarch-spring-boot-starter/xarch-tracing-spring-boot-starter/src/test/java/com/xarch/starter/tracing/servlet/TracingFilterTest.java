package com.xarch.starter.tracing.servlet;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Servlet-layer tests for {@link TracingFilter}.
 */
class TracingFilterTest {

    private InMemorySpanExporter exporter;
    private SdkTracerProvider provider;
    private OpenTelemetry openTelemetry;
    private TracingFilter filter;

    @BeforeEach
    void setUp() {
        exporter = InMemorySpanExporter.create();
        provider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(provider)
                .setPropagators(io.opentelemetry.context.propagation.ContextPropagators.create(
                        io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance()))
                .build();
        filter = new TracingFilter(openTelemetry);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        provider.close();
        MDC.clear();
    }

    @Test
    void opensServerSpanAndEchoesTraceId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> observedTraceId = new AtomicReference<>();
        FilterChain chain = (req, resp) -> {
            observedTraceId.set(io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId());
        };

        filter.doFilter(request, response, chain);

        assertThat(exporter.getFinishedSpanItems()).hasSize(1);
        var span = exporter.getFinishedSpanItems().get(0);
        assertThat(span.getName()).isEqualTo("GET /api/orders/42");
        assertThat(span.getKind().name()).isEqualTo("SERVER");
        assertThat(response.getHeader(TracingFilter.RESPONSE_TRACE_HEADER)).isEqualTo(span.getTraceId());
        assertThat(observedTraceId.get()).isEqualTo(span.getTraceId());
    }

    @Test
    void extractsW3CTraceparentHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.addHeader(TracingFilter.TRACEPARENT_HEADER,
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, resp) -> { /* no-op */ });

        var span = exporter.getFinishedSpanItems().get(0);
        // The extracted parent trace id must propagate to the new server span.
        assertThat(span.getTraceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
        // The parent span id should appear in the parent span context of the recorded span.
        assertThat(span.getParentSpanContext().getSpanId()).isEqualTo("00f067aa0ba902b7");
    }

    @Test
    void errorStatusMarksSpanAsError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, resp) -> {
            ((MockHttpServletResponse) resp).setStatus(503);
        });

        var span = exporter.getFinishedSpanItems().get(0);
        assertThat(span.getStatus().getStatusCode().name()).isEqualTo("ERROR");
        assertThat(span.getAttributes().get(io.opentelemetry.api.common.AttributeKey.longKey("http.response.status_code"))
                .intValue()).isEqualTo(503);
    }

    @Test
    void restoresMdcAfterRequest() throws ServletException, IOException {
        MDC.put("traceId", "before");
        MDC.put("spanId", "before-span");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, resp) -> { /* no-op */ });

        assertThat(MDC.get("traceId")).isEqualTo("before");
        assertThat(MDC.get("spanId")).isEqualTo("before-span");
    }
}