package com.xarch.starter.mq.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Injects the current OpenTelemetry trace context into AMQP message headers
 * (and extracts the same headers on the consumer side) so that
 * {@code xarch-mq -> xarch-tracing} play nicely together — a producer's
 * span links to the consumer's span.
 *
 * <p>This implementation is resilient: it does not throw if OpenTelemetry is
 * not on the classpath, or if no span is currently active.
 */
public class RabbitMessagePostProcessor implements MessagePostProcessor {

    private static final String INSTRUMENTATION_NAME = "xarch.mq";

    private final Tracer tracer;
    private final TextMapPropagator propagator;

    private static final TextMapSetter<Map<String, Object>> SETTER =
            (carrier, key, value) -> carrier.put(key, value == null ? null : value.toString());

    public RabbitMessagePostProcessor(OpenTelemetry openTelemetry) {
        this(openTelemetry.getTracer(INSTRUMENTATION_NAME), openTelemetry.getPropagators().getTextMapPropagator());
    }

    public RabbitMessagePostProcessor(Tracer tracer, TextMapPropagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public Message postProcessMessage(Message message) {
        MessageProperties props = message.getMessageProperties();
        Map<String, Object> headers = props.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
            props.setHeaders(headers);
        }

        // Open a producer span — but only if there's an active parent (i.e.
        // we're inside a server span). Otherwise we get a no-op span.
        Span span = tracer.spanBuilder("amqp.publish " + props.getReceivedExchange() + "/" + props.getReceivedRoutingKey())
                .setSpanKind(SpanKind.PRODUCER)
                .setAttribute("messaging.system", "rabbitmq")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Inject the current trace context into headers
            propagator.inject(Context.current(), headers, SETTER);
        } finally {
            span.end();
        }
        return message;
    }

    /**
     * Extract context from incoming AMQP headers. Used by the consumer side
     * to continue a trace.
     */
    public Context extract(MessageProperties props) {
        return propagator.extract(Context.current(), props.getHeaders(), GETTER);
    }

    private static final TextMapGetter<Map<String, Object>> GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, Object> carrier) {
                    return carrier.keySet();
                }
                @Override
                public String get(Map<String, Object> carrier, String key) {
                    if (carrier == null) return null;
                    Object v = carrier.get(key);
                    return v == null ? null : v.toString();
                }
            };
}
