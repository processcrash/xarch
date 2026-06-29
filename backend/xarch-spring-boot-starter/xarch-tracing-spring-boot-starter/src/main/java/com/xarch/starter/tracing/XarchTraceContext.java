package com.xarch.starter.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * Convenience facade that exposes the current OpenTelemetry trace context to
 * business code.
 *
 * <p>The facade is intentionally lightweight: it relies on the
 * {@link OpenTelemetry} global instance configured by Spring Boot's starter,
 * so it does not require any bean to be injected.
 *
 * <p>All helpers push the trace and span identifiers into SLF4J's MDC under
 * the conventional {@code traceId} and {@code spanId} keys. The companion
 * Logback pattern in {@code logback-trace-pattern.xml} reads those keys.
 */
public final class XarchTraceContext {

    /** MDC key for the current W3C trace identifier. */
    public static final String MDC_TRACE_ID = "traceId";
    /** MDC key for the current span identifier. */
    public static final String MDC_SPAN_ID = "spanId";

    private static final String INSTRUMENTATION_NAME = "com.xarch.starter.tracing";

    private XarchTraceContext() {
    }

    /**
     * @return the current trace identifier or an empty string when no span is active.
     */
    public static String currentTraceId() {
        Span span = Span.current();
        return span.getSpanContext().isValid() ? span.getSpanContext().getTraceId() : "";
    }

    /**
     * @return the current span identifier or an empty string when no span is active.
     */
    public static String currentSpanId() {
        Span span = Span.current();
        return span.getSpanContext().isValid() ? span.getSpanContext().getSpanId() : "";
    }

    /**
     * Run the supplied action inside a new internal span. The trace &amp; span
     * ids are placed into MDC for log correlation while the action runs.
     */
    public static void withSpan(String name, Runnable action) {
        withSpan(name, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Same as {@link #withSpan(String, Runnable)} but allows the action to
     * return a value.
     */
    public static <T> T withSpan(String name, Supplier<T> action) {
        Tracer tracer = OpenTelemetry.noop().getTracer(INSTRUMENTATION_NAME);
        Span span = tracer.spanBuilder(name).setSpanKind(SpanKind.INTERNAL).startSpan();
        String previousTraceId = MDC.get(MDC_TRACE_ID);
        String previousSpanId = MDC.get(MDC_SPAN_ID);
        try (Scope ignored = span.makeCurrent()) {
            pushMdc(span);
            return action.get();
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
            throw ex;
        } finally {
            span.end();
            restoreMdc(previousTraceId, previousSpanId);
        }
    }

    static void pushMdc(Span span) {
        if (span.getSpanContext().isValid()) {
            MDC.put(MDC_TRACE_ID, span.getSpanContext().getTraceId());
            MDC.put(MDC_SPAN_ID, span.getSpanContext().getSpanId());
        } else {
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_SPAN_ID);
        }
    }

    static void restoreMdc(String previousTraceId, String previousSpanId) {
        if (previousTraceId == null) {
            MDC.remove(MDC_TRACE_ID);
        } else {
            MDC.put(MDC_TRACE_ID, previousTraceId);
        }
        if (previousSpanId == null) {
            MDC.remove(MDC_SPAN_ID);
        } else {
            MDC.put(MDC_SPAN_ID, previousSpanId);
        }
    }
}