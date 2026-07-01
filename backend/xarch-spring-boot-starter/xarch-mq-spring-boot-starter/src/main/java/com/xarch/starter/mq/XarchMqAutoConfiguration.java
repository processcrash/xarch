package com.xarch.starter.mq;

import com.xarch.starter.mq.consumer.XarchMessageListener;
import com.xarch.starter.mq.consumer.XarchRabbitListenerEndpointRegistryPostProcessor;
import com.xarch.starter.mq.deadletter.DeadLetterTopology;
import com.xarch.starter.mq.producer.MessagePublisher;
import com.xarch.starter.mq.producer.RabbitMessagePublisher;
import com.xarch.starter.mq.support.JsonMessageConverterFactory;
import com.xarch.starter.mq.support.RabbitHealthIndicatorContributor;
import com.xarch.starter.mq.tracing.RabbitMessagePostProcessor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Entry point for the xarch RabbitMQ starter.
 *
 * <p>This configuration is master-switched by {@code xarch.mq.enabled}
 * (default {@code true}). When enabled it:
 * <ol>
 *   <li>Provides a {@link MessageConverter} bean (Jackson JSON) — replaces
 *       Spring's default {@code SimpleMessageConverter} for safety.</li>
 *   <li>Configures the {@link RabbitTemplate} with publisher confirms,
 *       returns, and tracing header propagation.</li>
 *   <li>Exposes a high-level {@link MessagePublisher} facade so application
 *       code never touches {@code RabbitTemplate} directly.</li>
 *   <li>Auto-declares the dead-letter exchange and queue
 *       (configurable via {@code xarch.mq.dlx.*}).</li>
 *   <li>Registers a {@link org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer}
 *       that applies the default retry, concurrency, and tracing settings to
 *       every {@link XarchMessageListener}-annotated method.</li>
 * </ol>
 *
 * <p>The auto-configuration runs <strong>after</strong> Spring Boot's
 * {@link RabbitAutoConfiguration} so it can rely on the auto-configured
 * {@link ConnectionFactory} and {@link AmqpAdmin}.
 */
@AutoConfiguration(after = RabbitAutoConfiguration.class)
@EnableConfigurationProperties(XarchMqProperties.class)
@ConditionalOnProperty(prefix = "xarch.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RabbitTemplate.class)
@Import({
        JsonMessageConverterFactory.class,
        DeadLetterTopology.class,
        RabbitMessagePostProcessor.class,
        XarchRabbitListenerEndpointRegistryPostProcessor.class
})
public class XarchMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter xarchMessageConverter(ObjectProvider<com.fasterxml.jackson.databind.ObjectMapper> objectMapper) {
        return JsonMessageConverterFactory.create(objectMapper.getIfAvailable(com.fasterxml.jackson.databind.ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate xarchRabbitTemplate(ConnectionFactory connectionFactory,
                                              MessageConverter messageConverter,
                                              XarchMqProperties properties,
                                              ObjectProvider<RabbitMessagePostProcessor> tracingProcessor) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(properties.getProducer().isMandatory());
        if (properties.getProducer().isPublisherConfirms()) {
            template.setConfirmCallback((corr, ack, cause) -> {
                if (!ack) {
                    org.slf4j.LoggerFactory.getLogger(RabbitTemplate.class)
                        .warn("Publisher NACK for {}: {}", corr, cause);
                }
            });
        }
        if (properties.getProducer().isPublisherReturns()) {
            template.setReturnsCallback(returned ->
                org.slf4j.LoggerFactory.getLogger(RabbitTemplate.class)
                    .warn("Returned message (unroutable): exchange={} routingKey={} replyCode={} replyText={}",
                        returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText()));
        }
        // Tracing header injection (only if a post-processor is present and tracing is enabled)
        RabbitMessagePostProcessor processor = tracingProcessor.getIfAvailable();
        if (processor != null && properties.getTracing().isEnabled()) {
            template.addBeforePublishPostProcessors(processor);
        }
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessagePublisher xarchMessagePublisher(RabbitTemplate template, XarchMqProperties properties) {
        return new RabbitMessagePublisher(template, properties.getProducer().getDefaultExchange());
    }

    @Bean
    public SmartInitializingSingleton xarchMqStartupLogger(XarchMqProperties properties) {
        return () -> org.slf4j.LoggerFactory.getLogger(XarchMqAutoConfiguration.class)
                .info("xarch RabbitMQ starter enabled: producer.confirms={}, dlx={}, tracing={}",
                        properties.getProducer().isPublisherConfirms(),
                        properties.getDlx().isEnabled(),
                        properties.getTracing().isEnabled());
    }

    @Bean
    public RabbitHealthIndicatorContributor xarchMqHealthContributor(ConnectionFactory connectionFactory) {
        return new RabbitHealthIndicatorContributor(connectionFactory);
    }
}
