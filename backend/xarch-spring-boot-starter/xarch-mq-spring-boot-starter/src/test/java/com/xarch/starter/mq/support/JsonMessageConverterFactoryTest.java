package com.xarch.starter.mq.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMessageConverterFactoryTest {

    public static class SampleEvent {
        private final String name;
        private final int value;
        public SampleEvent(String name, int value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @Test
    void convertsToJson() {
        MessageConverter converter = JsonMessageConverterFactory.create(new ObjectMapper());
        Message message = converter.toMessage(new SampleEvent("hello", 42), new MessageProperties());

        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("\"name\":\"hello\"").contains("\"value\":42");
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
    }

    @Test
    void messageIdIsSet() {
        MessageConverter converter = JsonMessageConverterFactory.create(new ObjectMapper());
        Message message = converter.toMessage(new SampleEvent("x", 1), new MessageProperties());
        assertThat(message.getMessageProperties().getMessageId()).isNotBlank();
    }
}
