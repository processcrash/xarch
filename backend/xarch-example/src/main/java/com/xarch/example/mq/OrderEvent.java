package com.xarch.example.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain event published when an order is created.
 * Used as a demonstration of the xarch RabbitMQ starter.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;
}
