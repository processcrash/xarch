package com.xarch.example.mq;

import com.xarch.starter.mq.producer.MessagePublisher;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Demo producer that publishes {@link OrderEvent} messages to a topic
 * exchange. Useful as a smoke test for the xarch RabbitMQ starter.
 *
 * <p>Exposed at {@code /api/mq-demo/publish}. To run the full flow, also
 * start the {@link OrderEventConsumer} which listens on the bound queue.
 */
@Slf4j
@RestController
@RequestMapping("/api/mq-demo")
@RequiredArgsConstructor
@Tag(name = "MQ Demo", description = "Demonstration of xarch RabbitMQ producer")
public class OrderEventProducer {

    private final MessagePublisher messagePublisher;

    @PostMapping("/publish")
    @Operation(summary = "Publish a sample order event")
    public ApiResult<String> publish(@RequestBody(required = false) OrderEvent event) {
        if (event == null) {
            event = new OrderEvent(
                    UUID.randomUUID().toString(),
                    "demo-customer",
                    new java.math.BigDecimal("99.00"),
                    "USD",
                    Instant.now()
            );
        } else if (event.getOrderId() == null) {
            event.setOrderId(UUID.randomUUID().toString());
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }

        Map<String, Object> headers = Map.of(
                "tenantId", "demo-tenant",
                "schemaVersion", 1
        );

        messagePublisher.publish("orders", "order.created", event, headers);
        log.info("Published OrderEvent: orderId={} amount={} {}",
                event.getOrderId(), event.getAmount(), event.getCurrency());

        return ApiResult.success(event.getOrderId());
    }

    @PostMapping("/publish-delayed")
    @Operation(summary = "Publish a sample order event with 10s delay")
    public ApiResult<String> publishDelayed(@RequestBody(required = false) OrderEvent event) {
        if (event == null) {
            event = new OrderEvent(
                    UUID.randomUUID().toString(),
                    "demo-customer",
                    new java.math.BigDecimal("199.00"),
                    "USD",
                    Instant.now()
            );
        }
        if (event.getOrderId() == null) {
            event.setOrderId(UUID.randomUUID().toString());
        }

        messagePublisher.publishDelayed("orders.delayed", "order.created", event, 10_000L);
        log.info("Published delayed OrderEvent: orderId={}", event.getOrderId());
        return ApiResult.success(event.getOrderId());
    }
}
