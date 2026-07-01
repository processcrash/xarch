package com.xarch.starter.mq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XarchMqPropertiesTest {

    @Test
    void defaultsAreSensible() {
        XarchMqProperties p = new XarchMqProperties();
        assertThat(p.isEnabled()).isTrue();
        assertThat(p.getProducer().isAutoDeclare()).isTrue();
        assertThat(p.getProducer().isPublisherConfirms()).isTrue();
        assertThat(p.getProducer().isPublisherReturns()).isTrue();
        assertThat(p.getProducer().getDefaultExchange()).isEmpty();
        assertThat(p.getProducer().isMandatory()).isTrue();

        assertThat(p.getConsumer().getConcurrency()).isEqualTo(1);
        assertThat(p.getConsumer().getMaxConcurrency()).isEqualTo(8);
        assertThat(p.getConsumer().getPrefetch()).isEqualTo(10);
        assertThat(p.getConsumer().isAutoStartup()).isTrue();
        assertThat(p.getConsumer().getRetryMaxAttempts()).isEqualTo(3);

        assertThat(p.getDlx().isEnabled()).isTrue();
        assertThat(p.getDlx().getExchange()).isEqualTo("xarch.dlx");
        assertThat(p.getDlx().getQueue()).isEqualTo("xarch.dlq");
        assertThat(p.getDlx().getMessageTtl()).isEqualTo(60_000L);
        assertThat(p.getDlx().getMaxLength()).isEqualTo(100_000);

        assertThat(p.getTracing().isEnabled()).isTrue();
    }

    @Test
    void propertiesAreBindable() {
        XarchMqProperties p = new XarchMqProperties();
        p.getConsumer().setConcurrency(4);
        p.getConsumer().setMaxConcurrency(16);
        p.getConsumer().setPrefetch(50);
        p.getDlx().setEnabled(false);
        p.getDlx().setQueue("custom.dlq");
        p.getTracing().setEnabled(false);

        assertThat(p.getConsumer().getConcurrency()).isEqualTo(4);
        assertThat(p.getConsumer().getMaxConcurrency()).isEqualTo(16);
        assertThat(p.getConsumer().getPrefetch()).isEqualTo(50);
        assertThat(p.getDlx().isEnabled()).isFalse();
        assertThat(p.getDlx().getQueue()).isEqualTo("custom.dlq");
        assertThat(p.getTracing().isEnabled()).isFalse();
    }
}
