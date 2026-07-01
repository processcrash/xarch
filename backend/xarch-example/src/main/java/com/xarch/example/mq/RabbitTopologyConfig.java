package com.xarch.example.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the demo exchange/queue/binding used by {@link OrderEventProducer}
 * and {@link OrderEventConsumer}. Routes rejected messages to the
 * xarch-default DLX.
 */
@Configuration
public class RabbitTopologyConfig {

    public static final String EXCHANGE_ORDERS = "orders";
    public static final String QUEUE_ORDER_CREATED = "orders.order-created";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE_ORDERS, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED)
                .withArgument("x-dead-letter-exchange", "xarch.dlx")
                .withArgument("x-dead-letter-routing-key", "orders.order-created")
                .build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(ordersExchange).with("order.created");
    }
}
