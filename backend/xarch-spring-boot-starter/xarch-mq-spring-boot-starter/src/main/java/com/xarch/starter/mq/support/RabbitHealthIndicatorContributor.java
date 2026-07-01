package com.xarch.starter.mq.support;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;

/**
 * Lightweight health check that verifies the RabbitMQ broker accepts a
 * connection. Intended to be invoked from the application's
 * {@code /actuator/health} endpoint.
 *
 * <p>Spring Boot's {@code RabbitHealthIndicator} already exists, but it
 * requires the full actuator dependency. This contributor provides a no-dep
 * equivalent that can be exposed by any monitoring tool.
 */
public class RabbitHealthIndicatorContributor {

    private final ConnectionFactory connectionFactory;

    public RabbitHealthIndicatorContributor(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Health check() {
        try (Connection connection = connectionFactory.createConnection()) {
            if (connection.isOpen()) {
                return Health.up()
                        .withDetail("broker", connection.toString())
                        .build();
            }
            return Health.down().withDetail("reason", "connection not open").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
