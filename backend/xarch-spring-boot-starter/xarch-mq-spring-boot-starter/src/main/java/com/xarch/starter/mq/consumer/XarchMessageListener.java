package com.xarch.starter.mq.consumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for consumer methods, layered on top of Spring's
 * {@code @RabbitListener}.
 *
 * <p>Methods annotated with {@code @XarchMessageListener} get the xarch default
 * retry / concurrency / DLQ wiring applied automatically (see
 * {@code XarchRabbitListenerEndpointRegistryPostProcessor}):
 * <ul>
 *   <li>Concurrency: {@code xarch.mq.consumer.concurrency..max-concurrency}</li>
 *   <li>Prefetch: {@code xarch.mq.consumer.prefetch}</li>
 *   <li>Retry: {@code xarch.mq.consumer.retry-*} (exponential back-off)</li>
 *   <li>DLQ: failed messages are routed to {@code xarch.dlx -> xarch.dlq}</li>
 * </ul>
 *
 * <p>Use the same parameters as {@code @RabbitListener} — queues to bind,
 * container factory reference, ack mode, etc.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XarchMessageListener {

    /** Queue(s) to listen on. At least one must be specified. */
    String[] queues() default {};

    /** Optional explicit container factory name. Defaults to the xarch factory. */
    String containerFactory() default "xarchRabbitListenerContainerFactory";
}
