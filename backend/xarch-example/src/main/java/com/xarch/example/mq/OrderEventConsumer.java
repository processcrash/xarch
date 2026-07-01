package com.xarch.example.mq;

import com.xarch.starter.mq.consumer.XarchMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo consumer that listens for {@link OrderEvent} messages on the
 * queue bound to the {@code orders} exchange. Failures are retried
 * (per {@code xarch.mq.consumer.retry-*}) and then routed to
 * {@code xarch.dlq}.
 */
@Slf4j
@Component
public class OrderEventConsumer {

    private int successCount = 0;
    private int failureCount = 0;

    @XarchMessageListener(queues = "orders.order-created")
    public void onOrderCreated(OrderEvent event) {
        log.info("Received OrderEvent: orderId={} customerId={} amount={} {}",
                event.getOrderId(), event.getCustomerId(), event.getAmount(), event.getCurrency());
        successCount++;

        // Uncomment to test the DLQ path:
        // if (event.getOrderId().startsWith("fail-")) {
        //     failureCount++;
        //     throw new RuntimeException("Simulated processing failure for " + event.getOrderId());
        // }
    }

    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
}
