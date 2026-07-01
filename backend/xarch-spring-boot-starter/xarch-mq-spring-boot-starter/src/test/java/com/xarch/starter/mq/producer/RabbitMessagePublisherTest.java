package com.xarch.starter.mq.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMessagePublisherTest {

    private RabbitTemplate template;
    private RabbitMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        template = mock(RabbitTemplate.class);
        publisher = new RabbitMessagePublisher(template, "");
    }

    @Test
    void publishUsesProvidedExchangeAndRoutingKey() {
        publisher.publish("orders", "order.created", new Object());
        verify(template).convertAndSend(eq("orders"), eq("order.created"), any(Object.class));
    }

    @Test
    void publishToQueueUsesDefaultExchange() {
        publisher.publishToQueue("my.queue", new Object());
        verify(template).convertAndSend(eq(""), eq("my.queue"), any(Object.class));
    }

    @Test
    void publishWithHeadersAttachesAllEntries() {
        Map<String, Object> headers = Map.of("tenantId", "acme", "schemaVersion", 2);
        publisher.publish("orders", "order.created", new Object(), headers);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(template).convertAndSend(eq("orders"), eq("order.created"), any(Object.class), captor.capture());

        // Apply the post-processor to a fake message and verify headers
        org.springframework.amqp.core.Message message = new org.springframework.amqp.core.Message(
                new byte[0], new org.springframework.amqp.core.MessageProperties());
        try {
            captor.getValue().postProcessMessage(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(message.getMessageProperties().getHeader("tenantId")).isEqualTo("acme");
        assertThat(message.getMessageProperties().getHeader("schemaVersion")).isEqualTo(2);
    }

    @Test
    void publishDelayedSetsXDelayHeader() {
        publisher.publishDelayed("orders.delayed", "order.created", new Object(), 5_000L);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(template).convertAndSend(eq("orders.delayed"), eq("order.created"), any(Object.class), captor.capture());

        Message message = new Message(new byte[0], new org.springframework.amqp.core.MessageProperties());
        try {
            captor.getValue().postProcessMessage(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(message.getMessageProperties().getHeader("x-delay")).isEqualTo(5_000);
    }

    @Test
    void brokerExceptionPropagates() {
        doThrow(new AmqpException("broker down"))
                .when(template).convertAndSend(any(String.class), any(String.class), any(Object.class));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> publisher.publish("orders", "order.created", new Object())
        ).isInstanceOf(AmqpException.class).hasMessageContaining("broker down");
    }
}
