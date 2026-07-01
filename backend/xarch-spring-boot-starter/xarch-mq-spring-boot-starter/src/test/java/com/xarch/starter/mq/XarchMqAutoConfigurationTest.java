package com.xarch.starter.mq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.xarch.starter.mq.producer.MessagePublisher;
import com.xarch.starter.mq.support.JsonMessageConverterFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class XarchMqAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RabbitAutoConfiguration.class, XarchMqAutoConfiguration.class))
            .withPropertyValues(
                    "spring.rabbitmq.host=localhost",
                    "spring.rabbitmq.port=5672"
            );

    @Test
    void autoConfiguresBeansWhenEnabled() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(XarchMqProperties.class);
            assertThat(ctx).hasSingleBean(RabbitTemplate.class);
            assertThat(ctx).hasSingleBean(MessagePublisher.class);
            assertThat(ctx).hasSingleBean(MessageConverter.class);
        });
    }

    @Test
    void disabledMasterSwitchExcludesBeans() {
        runner.withPropertyValues("xarch.mq.enabled=false").run(ctx -> {
            assertThat(ctx).doesNotHaveBean(XarchMqProperties.class);
            assertThat(ctx).doesNotHaveBean(MessagePublisher.class);
            // Note: RabbitTemplate comes from Spring Boot's own auto-config,
            // so it's still present even when xarch is disabled.
        });
    }
}
