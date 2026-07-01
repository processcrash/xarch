package com.xarch.starter.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration for the xarch RabbitMQ starter.
 *
 * <p>Master switch: {@code xarch.mq.enabled} (default {@code true}). When
 * disabled the auto-configuration backs off cleanly.
 *
 * <p>Connection details are read from standard Spring AMQP properties
 * ({@code spring.rabbitmq.*}) so existing {@code application.yml} keeps working
 * without renaming. The properties below add xarch-specific extensions on top.
 */
@ConfigurationProperties(prefix = "xarch.mq")
public class XarchMqProperties {

    /** Master switch. */
    private boolean enabled = true;

    @NestedConfigurationProperty
    private Producer producer = new Producer();

    @NestedConfigurationProperty
    private Consumer consumer = new Consumer();

    @NestedConfigurationProperty
    private Dlx dlx = new Dlx();

    @NestedConfigurationProperty
    private Tracing tracing = new Tracing();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Producer getProducer() { return producer; }
    public void setProducer(Producer producer) { this.producer = producer; }

    public Consumer getConsumer() { return consumer; }
    public void setConsumer(Consumer consumer) { this.consumer = consumer; }

    public Dlx getDlx() { return dlx; }
    public void setDlx(Dlx dlx) { this.dlx = dlx; }

    public Tracing getTracing() { return tracing; }
    public void setTracing(Tracing tracing) { this.tracing = tracing; }

    public static class Producer {
        /** Whether to auto-declare exchanges/queues/bindings at startup. */
        private boolean autoDeclare = true;
        /** Whether to use publisher confirms (recommended for at-least-once). */
        private boolean publisherConfirms = true;
        /** Whether to use publisher returns (detect unroutable messages). */
        private boolean publisherReturns = true;
        /** Default exchange to publish to when not specified. */
        private String defaultExchange = "";
        /** Mandatory flag — when true, unroutable messages trigger a return. */
        private boolean mandatory = true;

        public boolean isAutoDeclare() { return autoDeclare; }
        public void setAutoDeclare(boolean autoDeclare) { this.autoDeclare = autoDeclare; }
        public boolean isPublisherConfirms() { return publisherConfirms; }
        public void setPublisherConfirms(boolean publisherConfirms) { this.publisherConfirms = publisherConfirms; }
        public boolean isPublisherReturns() { return publisherReturns; }
        public void setPublisherReturns(boolean publisherReturns) { this.publisherReturns = publisherReturns; }
        public String getDefaultExchange() { return defaultExchange; }
        public void setDefaultExchange(String defaultExchange) { this.defaultExchange = defaultExchange; }
        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    }

    public static class Consumer {
        /** Default concurrency per consumer instance. */
        private int concurrency = 1;
        /** Max concurrency per consumer instance. */
        private int maxConcurrency = 8;
        /** Prefetch count per consumer. */
        private int prefetch = 10;
        /** Whether to auto-start container (default true). */
        private boolean autoStartup = true;
        /** Default retry config applied to all @XarchListener methods. */
        private int retryMaxAttempts = 3;
        private long retryInitialInterval = 1000L;
        private double retryMultiplier = 2.0;
        private long retryMaxInterval = 30_000L;

        public int getConcurrency() { return concurrency; }
        public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
        public int getMaxConcurrency() { return maxConcurrency; }
        public void setMaxConcurrency(int maxConcurrency) { this.maxConcurrency = maxConcurrency; }
        public int getPrefetch() { return prefetch; }
        public void setPrefetch(int prefetch) { this.prefetch = prefetch; }
        public boolean isAutoStartup() { return autoStartup; }
        public void setAutoStartup(boolean autoStartup) { this.autoStartup = autoStartup; }
        public int getRetryMaxAttempts() { return retryMaxAttempts; }
        public void setRetryMaxAttempts(int retryMaxAttempts) { this.retryMaxAttempts = retryMaxAttempts; }
        public long getRetryInitialInterval() { return retryInitialInterval; }
        public void setRetryInitialInterval(long retryInitialInterval) { this.retryInitialInterval = retryInitialInterval; }
        public double getRetryMultiplier() { return retryMultiplier; }
        public void setRetryMultiplier(double retryMultiplier) { this.retryMultiplier = retryMultiplier; }
        public long getRetryMaxInterval() { return retryMaxInterval; }
        public void setRetryMaxInterval(long retryMaxInterval) { this.retryMaxInterval = retryMaxInterval; }
    }

    public static class Dlx {
        /** Whether to auto-declare the dead-letter exchange and queue. */
        private boolean enabled = true;
        /** Dead letter exchange name. */
        private String exchange = "xarch.dlx";
        /** Dead letter queue name. */
        private String queue = "xarch.dlq";
        /** Routing key used to bind dlq to dlx. */
        private String routingKey = "#";
        /** Time-to-live (ms) before the message is considered dead. */
        private long messageTtl = 60_000L;
        /** Max length of dlq (0 = unlimited). */
        private int maxLength = 100_000;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getExchange() { return exchange; }
        public void setExchange(String exchange) { this.exchange = exchange; }
        public String getQueue() { return queue; }
        public void setQueue(String queue) { this.queue = queue; }
        public String getRoutingKey() { return routingKey; }
        public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
        public long getMessageTtl() { return messageTtl; }
        public void setMessageTtl(long messageTtl) { this.messageTtl = messageTtl; }
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    }

    public static class Tracing {
        /** Inject xarch-tracing context (traceparent) into AMQP headers. */
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
