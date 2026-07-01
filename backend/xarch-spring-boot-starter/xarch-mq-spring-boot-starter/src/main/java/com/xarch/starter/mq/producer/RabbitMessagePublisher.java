package com.xarch.starter.mq.producer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

/**
 * Default {@link MessagePublisher} backed by {@link RabbitTemplate}.
 */
public class RabbitMessagePublisher implements MessagePublisher {

    private final RabbitTemplate template;
    private final String defaultExchange;

    public RabbitMessagePublisher(RabbitTemplate template, String defaultExchange) {
        this.template = template;
        this.defaultExchange = defaultExchange == null ? "" : defaultExchange;
    }

    @Override
    public void publish(String exchange, String routingKey, Object payload) {
        template.convertAndSend(exchange, routingKey, payload);
    }

    @Override
    public void publishToQueue(String queue, Object payload) {
        template.convertAndSend(defaultExchange, queue, payload);
    }

    @Override
    public void publish(String exchange, String routingKey, Object payload, Map<String, Object> headers) {
        template.convertAndSend(exchange, routingKey, payload, message -> {
            if (headers != null) {
                headers.forEach((k, v) -> message.getMessageProperties().setHeader(k, v));
            }
            return message;
        });
    }

    @Override
    public void publishDelayed(String exchange, String routingKey, Object payload, long delayMillis) {
        template.convertAndSend(exchange, routingKey, payload, (MessagePostProcessor) message -> {
            // Spring AMQP sets the x-delay header that the
            // rabbitmq-delayed-message-exchange plugin reads. Falls back to
            // a no-op (no delay) if the plugin is not installed on the
            // broker.
            message.getMessageProperties().setHeader("x-delay", (int) delayMillis);
            return message;
        });
    }
}
