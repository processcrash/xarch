# xarch-mq (RabbitMQ) Spring Boot Starter

Production-grade RabbitMQ integration for the xarch platform.

## Features

- **Auto-configured** `ConnectionFactory`, `RabbitTemplate`, and `MessageConverter` (Jackson JSON).
- **High-level `MessagePublisher` facade** — application code never touches `RabbitTemplate` directly.
- **Publisher confirms + returns** for at-least-once semantics.
- **Auto-declared DLX** (`xarch.dlx`) and DLQ (`xarch.dlq`) with TTL and max-length.
- **`@XarchMessageListener`** annotation with built-in retry (exponential back-off) + DLQ routing on exhaustion.
- **OpenTelemetry tracing integration** — producer's span links to consumer's span via AMQP headers.
- **Delayed messages** via the `rabbitmq-delayed-message-exchange` plugin (or no-op fallback).

## Quick start

### 1. Add the dependency

```gradle
dependencies {
    implementation 'com.xarch:xarch-mq-spring-boot-starter:1.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
}
```

### 2. Configure

```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: xarch
    password: xarch

xarch:
  mq:
    enabled: true
    consumer:
      concurrency: 4
      max-concurrency: 16
    dlx:
      enabled: true
    tracing:
      enabled: true   # requires xarch-tracing-spring-boot-starter
```

### 3. Publish

```java
@Service
public class OrderService {
    private final MessagePublisher publisher;

    public OrderService(MessagePublisher publisher) {
        this.publisher = publisher;
    }

    public void placeOrder(Order order) {
        publisher.publish("orders", "order.created", order);
    }
}
```

### 4. Consume

```java
@Component
public class OrderListener {
    @XarchMessageListener(queues = "orders.order-created")
    public void onOrderCreated(Order order) {
        log.info("Received order: {}", order);
    }
}
```

If the listener throws, the message is retried up to 3 times with exponential
back-off (1s, 2s, 4s, ...), then routed to `xarch.dlq` for inspection.

## Full configuration reference

| Property | Default | Description |
|----------|---------|-------------|
| `xarch.mq.enabled` | `true` | Master switch |
| `xarch.mq.producer.auto-declare` | `true` | Declare exchanges/queues on startup |
| `xarch.mq.producer.publisher-confirms` | `true` | Ack from broker on persist |
| `xarch.mq.producer.publisher-returns` | `true` | Detect unroutable |
| `xarch.mq.producer.default-exchange` | `""` | Default exchange for `publishToQueue` |
| `xarch.mq.producer.mandatory` | `true` | Require routability |
| `xarch.mq.consumer.concurrency` | `1` | Min consumers per listener |
| `xarch.mq.consumer.max-concurrency` | `8` | Max consumers per listener |
| `xarch.mq.consumer.prefetch` | `10` | Prefetch count |
| `xarch.mq.consumer.auto-startup` | `true` | Start containers at boot |
| `xarch.mq.consumer.retry-max-attempts` | `3` | Retry budget before DLQ |
| `xarch.mq.consumer.retry-initial-interval` | `1000` | First back-off (ms) |
| `xarch.mq.consumer.retry-multiplier` | `2.0` | Back-off multiplier |
| `xarch.mq.consumer.retry-max-interval` | `30000` | Max back-off (ms) |
| `xarch.mq.dlx.enabled` | `true` | Auto-declare DLX + DLQ |
| `xarch.mq.dlx.exchange` | `xarch.dlx` | DLX name |
| `xarch.mq.dlx.queue` | `xarch.dlq` | DLQ name |
| `xarch.mq.dlx.routing-key` | `"#"` | DLQ binding routing key |
| `xarch.mq.dlx.message-ttl` | `60000` | DLQ message TTL (ms) |
| `xarch.mq.dlx.max-length` | `100000` | DLQ max length (0 = unlimited) |
| `xarch.mq.tracing.enabled` | `true` | OpenTelemetry propagation |

## API reference

### `MessagePublisher`

```java
public interface MessagePublisher {
    void publish(String exchange, String routingKey, Object payload);
    void publishToQueue(String queue, Object payload);
    void publish(String exchange, String routingKey, Object payload, Map<String, Object> headers);
    void publishDelayed(String exchange, String routingKey, Object payload, long delayMillis);
}
```

### `@XarchMessageListener`

```java
@Target(METHOD)
@Retention(RUNTIME)
public @interface XarchMessageListener {
    String[] queues() default {};
    String containerFactory() default "xarchRabbitListenerContainerFactory";
}
```

## License

MIT
