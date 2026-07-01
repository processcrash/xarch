package com.xarch.starter.mq.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * Builds the Jackson-based JSON {@link MessageConverter} used by both
 * {@code RabbitTemplate} (producer) and {@code SimpleMessageListenerContainer}
 * (consumer).
 *
 * <p>Using JSON instead of {@code SimpleMessageConverter} (Java serialization)
 * eliminates deserialization-gadget attack vectors and makes messages
 * language-agnostic — a Python or Go consumer can read them.
 */
public final class JsonMessageConverterFactory {

    private JsonMessageConverterFactory() {}

    public static MessageConverter create(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        // Use a typed class header ("__TypeId__") so the consumer can
        // deserialize back to the same POJO. This is the Spring AMQP default
        // when using the Jackson converter, but we make it explicit.
        converter.setCreateMessageIds(true);
        return converter;
    }
}
