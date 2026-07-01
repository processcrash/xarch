package com.xarch.starter.mq.producer;

import java.util.Map;

/**
 * High-level message publisher facade. Application code should depend on this
 * interface — never directly on {@code RabbitTemplate} — so the underlying
 * transport can be swapped (RabbitMQ → Kafka → Redis Streams) without
 * touching callers.
 *
 * <p>Implementations are responsible for:
 * <ul>
 *   <li>Serializing the payload (typically via the configured
 *       {@code MessageConverter}).</li>
 *   <li>Injecting tracing headers when
 *       {@code xarch.mq.tracing.enabled=true}.</li>
 *   <li>Handling publisher confirms / returns (logging or callback). For
 *       strict at-least-once semantics, callers may chain
 *       {@link #publishAsync} with their own callback.</li>
 * </ul>
 */
public interface MessagePublisher {

    /**
     * Synchronously send {@code payload} to {@code exchange} with
     * {@code routingKey}. Throws on broker failure.
     */
    void publish(String exchange, String routingKey, Object payload);

    /**
     * Send {@code payload} to the default exchange (configured via
     * {@code xarch.mq.producer.default-exchange}, default "" — the AMQP
     * default direct exchange). {@code routingKey} is used as the queue name.
     */
    void publishToQueue(String queue, Object payload);

    /**
     * Variant that allows extra AMQP headers to be set, e.g. tenant id,
     * correlation id, schema version.
     */
    void publish(String exchange, String routingKey, Object payload, Map<String, Object> headers);

    /**
     * Send a message with a delay (milliseconds). Implementation may use
     * the rabbitmq-delayed-message-exchange plugin or a per-message TTL
     * fallback. The plugin path is preferred; auto-configures if the
     * exchange {@code xarch.delayed} is declared.
     */
    void publishDelayed(String exchange, String routingKey, Object payload, long delayMillis);
}
