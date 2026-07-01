package com.xarch.starter.mq.consumer;

import com.xarch.starter.mq.XarchMqProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

/**
 * Builds the default {@code SimpleRabbitListenerContainerFactory} used by
 * {@link XarchMessageListener}-annotated methods, applying the xarch defaults
 * from {@link XarchMqProperties.Consumer}.
 *
 * <p>The factory is named {@code xarchRabbitListenerContainerFactory} and is
 * what gets picked up automatically when a listener method specifies
 * {@code containerFactory = "xarchRabbitListenerContainerFactory"}.
 */
@Configuration
@EnableConfigurationProperties(XarchMqProperties.class)
public class XarchRabbitListenerEndpointRegistryPostProcessor {

    @Bean
    public SimpleRabbitListenerContainerFactory xarchRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            XarchMqProperties properties) {

        XarchMqProperties.Consumer c = properties.getConsumer();

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(c.getConcurrency());
        factory.setMaxConcurrentConsumers(c.getMaxConcurrency());
        factory.setPrefetchCount(c.getPrefetch());
        factory.setDefaultRequeueRejected(false); // critical — failed messages go to DLQ, not back to queue
        factory.setAutoStartup(c.isAutoStartup());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        // Retry with exponential back-off, then DLQ
        factory.setRetryTemplate(buildRetryTemplate(c));
        factory.setRecoveryCallback(ctx -> {
            // Re-throw so the container treats it as rejected and routes to DLQ
            throw (org.springframework.amqp.AmqpRejectAndDontRequeueException)
                new org.springframework.amqp.AmqpRejectAndDontRequeueException(
                    "Retry exhausted; routing to DLQ", ctx.getLastThrowable());
        });

        return factory;
    }

    private RetryTemplate buildRetryTemplate(XarchMqProperties.Consumer c) {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(c.getRetryMaxAttempts());
        template.setRetryPolicy(policy);

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(c.getRetryInitialInterval());
        backOff.setMultiplier(c.getRetryMultiplier());
        backOff.setMaxInterval(c.getRetryMaxInterval());
        template.setBackOffPolicy(backOff);

        template.registerListener(new org.springframework.retry.RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext ctx, RetryCallback<T, E> callback, Throwable throwable) {
                org.slf4j.LoggerFactory.getLogger(XarchRabbitListenerEndpointRegistryPostProcessor.class)
                        .warn("Retry attempt {} failed: {}", ctx.getRetryCount(), throwable.toString());
            }
        });
        return template;
    }
}
