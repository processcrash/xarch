package com.xarch.starter.tracing.servlet;

import com.xarch.starter.tracing.XarchTraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * Servlet filter that opens a server span for every incoming HTTP request,
 * extracts the upstream W3C {@code traceparent} header (if any) and pushes
 * the resulting trace &amp; span identifiers into MDC so log lines are
 * correlatable with traces.
 *
 * <p>The current trace id is also echoed back to the caller via the
 * {@value #RESPONSE_TRACE_HEADER} response header — convenient when the
 * client is a browser or a non-OTLP-aware service.
 */
public class TracingFilter implements Filter {

    /** Response header used to surface the trace id to the caller. */
    public static final String RESPONSE_TRACE_HEADER = "X-Trace-Id";

    /** W3C trace context header. */
    public static final String TRACEPARENT_HEADER = "traceparent";

    private static final TextMapGetter<HttpServletRequest> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return java.util.Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier == null ? null : carrier.getHeader(key);
        }
    };

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public TracingFilter(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("com.xarch.starter.tracing.servlet");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        Context extracted = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), httpRequest, GETTER);

        Span span = tracer.spanBuilder(httpRequest.getMethod() + " " + httpRequest.getRequestURI())
                .setSpanKind(SpanKind.SERVER)
                .setParent(extracted)
                .startSpan();

        String previousTraceId = MDC.get(XarchTraceContext.MDC_TRACE_ID);
        String previousSpanId = MDC.get(XarchTraceContext.MDC_SPAN_ID);

        try (Scope ignored = span.makeCurrent()) {
            XarchTraceContext.pushMdc(span);
            httpResponse.setHeader(RESPONSE_TRACE_HEADER, span.getSpanContext().getTraceId());
            chain.doFilter(request, response);
            int status = httpResponse.getStatus();
            span.setAttribute("http.response.status_code", status);
            if (status >= 500) {
                span.setStatus(StatusCode.ERROR, "HTTP " + status);
            }
        } catch (IOException | ServletException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getClass().getSimpleName());
            throw ex;
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getClass().getSimpleName());
            throw ex;
        } finally {
            span.end();
            XarchTraceContext.restoreMdc(previousTraceId, previousSpanId);
        }
    }
}