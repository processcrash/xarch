package com.xarch.starter.mq.deadletter;

import com.xarch.starter.mq.XarchMqProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the dead-letter exchange and queue topology that the
 * {@link com.xarch.starter.mq.consumer.XarchRabbitListenerEndpointRegistryPostProcessor}
 * uses when a message exhausts its retry budget.
 *
 * <p>By default:
 * <ul>
 *   <li>{@code xarch.dlx} — topic exchange</li>
 *   <li>{@code xarch.dlq} — durable queue with TTL {@code xarch.mq.dlx.message-ttl}
 *       and max length {@code xarch.mq.dlx.max-length}</li>
 *   <li>Binding: {@code xarch.dlq} ← {@code xarch.dlx} with routing key
 *       {@code xarch.mq.dlx.routing-key} (default {@code #})</li>
 * </ul>
 *
 * <p>To route a specific application's main queue to the DLX on rejection,
 * set its {@code x-dead-letter-exchange} argument to {@code xarch.dlx}.
 * The {@link com.xarch.starter.mq.consumer.XarchRabbitListenerEndpointRegistryPostProcessor}
 * already wires this on its default container via
 * {@code setDefaultRequeueRejected(false)}.
 */
@Configuration
public class DeadLetterTopology {

    @Bean
    public SmartInitializingSingleton deadLetterDeclarer(RabbitAdmin admin,
                                                          XarchMqProperties properties,
                                                          TopicExchange deadLetterExchange,
                                                          Queue deadLetterQueue,
                                                          Binding deadLetterBinding) {
        return () -> {
            if (!properties.getDlx().isEnabled()) {
                return;
            }
            admin.declareExchange(deadLetterExchange);
            admin.declareQueue(deadLetterQueue);
            admin.declareBinding(deadLetterBinding);
        };
    }

    @Bean
    public TopicExchange deadLetterExchange(XarchMqProperties properties) {
        return new TopicExchange(properties.getDlx().getExchange(), true, false);
    }

    @Bean
    public Queue deadLetterQueue(XarchMqProperties properties) {
        XarchMqProperties.Dlx cfg = properties.getDlx();
        return QueueBuilder.durable(cfg.getQueue())
                .withArgument("x-message-ttl", cfg.getMessageTtl())
                .withArgument("x-max-length", cfg.getMaxLength())
                .build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange, XarchMqProperties properties) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(properties.getDlx().getRoutingKey());
    }
}
