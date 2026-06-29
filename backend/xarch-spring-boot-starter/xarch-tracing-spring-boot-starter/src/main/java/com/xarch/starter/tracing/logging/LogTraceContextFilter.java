package com.xarch.starter.tracing.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.xarch.starter.tracing.XarchTraceContext;
import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * Logback {@link TurboFilter} that injects the current OpenTelemetry trace
 * and span identifiers into the SLF4J MDC before every log event is
 * formatted.
 *
 * <p>The filter never short-circuits log output — it only enriches MDC so
 * the {@code %X{traceId}} / {@code %X{spanId}} conversion words in the
 * logback pattern can be substituted.
 *
 * <p>The filter is registered automatically by the xarch tracing
 * auto-configuration via the {@code logback-trace-pattern.xml} include.
 */
public class LogTraceContextFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        Span span = Span.current();
        if (span.getSpanContext().isValid()) {
            MDC.put(XarchTraceContext.MDC_TRACE_ID, span.getSpanContext().getTraceId());
            MDC.put(XarchTraceContext.MDC_SPAN_ID, span.getSpanContext().getSpanId());
        } else {
            MDC.remove(XarchTraceContext.MDC_TRACE_ID);
            MDC.remove(XarchTraceContext.MDC_SPAN_ID);
        }
        return FilterReply.NEUTRAL;
    }
}