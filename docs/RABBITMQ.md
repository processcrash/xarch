# RabbitMQ (xarch-mq) 使用指南

> Spring Boot 4.0 + JDK 25 + Spring AMQP + xarch 一站式封装

`xarch-mq-spring-boot-starter` 把 RabbitMQ 集成中所有"样板代码"集中起来，让你用 4 行代码完成：声明拓扑、发布消息、消费消息、失败重试、死信队列、链路追踪。

---

## 1. 模块组成

| 类 | 作用 |
|----|------|
| `XarchMqAutoConfiguration` | Spring Boot 自动配置入口（master switch: `xarch.mq.enabled`）|
| `XarchMqProperties` | 全部配置属性（`xarch.mq.*`）|
| `MessagePublisher` | 高级发布门面（4 个重载）|
| `RabbitMessagePublisher` | 基于 `RabbitTemplate` 的默认实现 |
| `@XarchMessageListener` | 注解 + `xarchRabbitListenerContainerFactory` 自动注入 |
| `XarchRabbitListenerEndpointRegistryPostProcessor` | 构建带重试 + DLQ 的默认监听容器工厂 |
| `DeadLetterTopology` | 自动声明 `xarch.dlx` + `xarch.dlq` |
| `JsonMessageConverterFactory` | Jackson JSON 消息转换器（替代 `SimpleMessageConverter`，更安全）|
| `RabbitMessagePostProcessor` | OpenTelemetry trace context 注入/提取 |
| `RabbitHealthIndicatorContributor` | 健康检查（可挂到 actuator）|

---

## 2. 5 分钟集成

### 2.1 添加依赖

`backend/xarch-example/build.gradle`:

```gradle
dependencies {
    implementation project(':xarch-spring-boot-starter:xarch-mq-spring-boot-starter')
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
}
```

### 2.2 配置

`application.yml`：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: xarch
    password: xarch
    virtual-host: /

xarch:
  mq:
    enabled: true
    consumer:
      concurrency: 4
      max-concurrency: 16
      retry-max-attempts: 3
    dlx:
      enabled: true
    tracing:
      enabled: true   # 配合 xarch-tracing-spring-boot-starter
```

### 2.3 启动 RabbitMQ

```bash
docker run -d --name xarch-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=xarch \
  -e RABBITMQ_DEFAULT_PASS=xarch \
  rabbitmq:3.13-management-alpine
```

或用项目自带的 docker-compose：

```bash
docker compose up -d rabbitmq
```

### 2.4 声明拓扑

```java
@Configuration
public class RabbitTopologyConfig {

    public static final String EXCHANGE = "orders";
    public static final String QUEUE = "orders.order-created";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", "xarch.dlx")
                .withArgument("x-dead-letter-routing-key", QUEUE)
                .build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue q, TopicExchange e) {
        return BindingBuilder.bind(q).to(e).with("order.created");
    }
}
```

### 2.5 发布

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MessagePublisher publisher;

    public void placeOrder(Order order) {
        publisher.publish("orders", "order.created", order,
                Map.of("tenantId", "acme", "schemaVersion", 1));
    }
}
```

### 2.6 消费

```java
@Component
public class OrderEventConsumer {

    @XarchMessageListener(queues = "orders.order-created")
    public void onOrderCreated(OrderEvent event) {
        log.info("Processing order {}", event.getOrderId());
    }
}
```

完成。消息异常时自动重试 3 次（1s/2s/4s 指数退避），最终失败落入 `xarch.dlq`。

---

## 3. 关键概念

### 3.1 可靠投递（at-least-once）

xarch 默认开启 **publisher confirms** + **publisher returns**：

- `publisher-confirms=true`：broker 收到消息后 ACK，NACK 时日志告警
- `publisher-returns=true`：消息无法路由时返回（配合 `mandatory=true`）
- 生产者代码无需关心，框架自动处理

如需更严格保证，可改用事务发送或 `RabbitTemplate.invoke + waitForConfirms`：

```java
boolean ack = template.invoke(t -> {
    t.convertAndSend("orders", "order.created", order);
    return t.waitForConfirms(5_000);
});
if (!ack) {
    throw new AmqpException("Message not confirmed within 5s");
}
```

### 3.2 死信队列（DLQ）

**默认配置** (`xarch.mq.dlx.*`)：

| 项 | 默认值 |
|---|--------|
| `enabled` | `true` |
| `exchange` | `xarch.dlx`（topic）|
| `queue` | `xarch.dlq` |
| `routing-key` | `#`（topic 通配）|
| `message-ttl` | 60_000 ms |
| `max-length` | 100_000 |

**触发路径**：

```
listener throws → retry N times → AmqpRejectAndDontRequeueException
   → 消息带 x-death header 重发到 x-dead-letter-exchange
   → xarch.dlx (#) → xarch.dlq
```

> `setDefaultRequeueRejected(false)` 在容器工厂里已设置，确保失败消息**不**重新入队，直接进 DLQ。

**手动消费 DLQ**：

```java
@XarchMessageListener(queues = "xarch.dlq")
public void onDeadLetter(Message message) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    log.warn("Dead letter received: x-death={} body={}",
            headers.get("x-death"),
            new String(message.getBody()));
}
```

### 3.3 延迟消息

```java
publisher.publishDelayed("orders.delayed", "order.created", order, 10_000L);
```

需要 broker 启用 `rabbitmq-delayed-message-exchange` 插件：

```bash
docker exec xarch-rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

未启用时，x-delay 头被设置但无效果（消息立即投递）。

### 3.4 链路追踪

`xarch-mq` 与 `xarch-tracing` 集成：

- **生产端**：发布消息时打开 producer span，并把当前 trace context 注入到 AMQP headers (`traceparent`)。
- **消费端**：消费者从 headers 提取 trace context 并成为子 span。
- **结果**：在 Jaeger / Tempo / SigNoz 里看到一条完整的"用户请求 → 发布 → 消费"链路。

需保证 `xarch-tracing` 在 classpath 上。

### 3.5 监听器容器

`xarchRabbitListenerContainerFactory` 默认行为：

| 设置 | 值 | 说明 |
|------|---|------|
| `concurrentConsumers` | 1 | 初始并发数 |
| `maxConcurrentConsumers` | 8 | 弹性上限 |
| `prefetchCount` | 10 | 预取 |
| `defaultRequeueRejected` | false | 失败不重入队 |
| `acknowledgeMode` | AUTO | 容器自动 ACK |
| `retryTemplate` | 3 attempts, 1s/2s/4s | 指数退避 |
| `recoveryCallback` | throws `AmqpRejectAndDontRequeueException` | 重试用尽后路由到 DLQ |

方法级覆盖：可在 `@XarchMessageListener(containerFactory = "myCustomFactory")` 指定自定义工厂。

---

## 4. 配置参考

详见 `additional-spring-configuration-metadata.json`（IDE 也有提示）：

| Property | Default | Description |
|----------|---------|-------------|
| `xarch.mq.enabled` | `true` | 总开关 |
| `xarch.mq.producer.auto-declare` | `true` | 启动时声明 exchanges/queues |
| `xarch.mq.producer.publisher-confirms` | `true` | 开启 publisher confirm |
| `xarch.mq.producer.publisher-returns` | `true` | 开启 publisher return |
| `xarch.mq.producer.default-exchange` | `""` | `publishToQueue` 的默认 exchange |
| `xarch.mq.producer.mandatory` | `true` | 路由失败时回执 |
| `xarch.mq.consumer.concurrency` | `1` | 初始消费者数 |
| `xarch.mq.consumer.max-concurrency` | `8` | 最大消费者数 |
| `xarch.mq.consumer.prefetch` | `10` | 预取 |
| `xarch.mq.consumer.auto-startup` | `true` | 启动时拉起容器 |
| `xarch.mq.consumer.retry-max-attempts` | `3` | 重试次数（用尽后 → DLQ）|
| `xarch.mq.consumer.retry-initial-interval` | `1000` | 首次退避（ms）|
| `xarch.mq.consumer.retry-multiplier` | `2.0` | 退避倍数 |
| `xarch.mq.consumer.retry-max-interval` | `30000` | 最大退避（ms）|
| `xarch.mq.dlx.enabled` | `true` | 自动声明 DLX + DLQ |
| `xarch.mq.dlx.exchange` | `xarch.dlx` | DLX 名称 |
| `xarch.mq.dlx.queue` | `xarch.dlq` | DLQ 名称 |
| `xarch.mq.dlx.routing-key` | `"#"` | DLQ 绑定路由键 |
| `xarch.mq.dlx.message-ttl` | `60000` | DLQ 消息 TTL（ms）|
| `xarch.mq.dlx.max-length` | `100000` | DLQ 最大长度（0=无限制）|
| `xarch.mq.tracing.enabled` | `true` | OTel 链路追踪注入 |

---

## 5. 测试策略

### 5.1 单元测试（无需 broker）

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private MessagePublisher publisher;
    @InjectMocks private OrderService service;

    @Test
    void publishesOnPlace() {
        service.placeOrder(new Order("o-1", "c-1", BigDecimal.TEN));
        verify(publisher).publish(eq("orders"), eq("order.created"), any(Order.class));
    }
}
```

### 5.2 集成测试（Testcontainers）

```java
@SpringBootTest
@Testcontainers
class RabbitIntegrationTest {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void wire(DynamicPropertyRegistry r) {
        r.add("spring.rabbitmq.host", rabbit::getHost);
        r.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @Autowired RabbitTemplate template;
    @Autowired MessagePublisher publisher;

    @Test
    void publishAndReceive() {
        publisher.publish("test.exchange", "test.key", new TestEvent("hello"));
        await().atMost(5, SECONDS).untilAsserted(() -> {
            // assert the message arrived
        });
    }
}
```

`build.gradle` 已添加：

```gradle
testImplementation 'org.springframework.amqp:spring-rabbit-test'
testImplementation 'org.testcontainers:junit-jupiter:1.20.4'
testImplementation 'org.testcontainers:rabbitmq:1.20.4'
```

### 5.3 手工验证

```bash
# 启动 broker
docker compose up -d rabbitmq

# 启动 xarch-example
./gradlew :xarch-example:bootRun

# 触发发布
curl -X POST http://localhost:8080/api/mq-demo/publish

# 查看管理控制台
open http://localhost:15672    # xarch / xarch
# → Queues → orders.order-created 看到消息流入
# → Queues → xarch.dlq 看到重试耗尽的消息
```

---

## 6. 生产最佳实践

### 6.1 连接

- 生产环境用 `fail-fast` 启动（broker 不可用时立即失败）
- 启用 `spring.rabbitmq.connection-timeout: 5000`
- 启用 `spring.rabbitmq.template.retry.enabled: true`（连接重试，独立于消息重试）

### 6.2 拓扑

- **topic exchange** 优于 direct（路由键可分阶段细化）
- 队列设 `x-message-ttl` + `x-max-length` 防止内存爆炸
- **一个队列一个消费者**（同一队列多消费者是轮询，不是广播；广播用 fanout 或各自绑定）

### 6.3 消息设计

- 使用 **JSON**（默认）而非 Java 序列化，跨语言兼容
- 在 payload 顶部放 `schemaVersion` 字段，方便演进
- 使用 `id` + `timestamp` + `source` 字段做幂等性
- 单条消息 < 1 MB（AMQP 帧默认限制）

### 6.4 幂等消费

消费者必须**幂等**（可能重复收到）：

```java
@XarchMessageListener(queues = "orders.order-created")
public void onOrderCreated(OrderEvent event) {
    if (processedOrderRepository.exists(event.getOrderId())) {
        log.info("Order {} already processed, skipping", event.getOrderId());
        return;
    }
    // ... handle ...
    processedOrderRepository.save(event.getOrderId());
}
```

### 6.5 监控

- Prometheus 通过 `RabbitTemplate` 自带 `rabbitmq_*` 指标
- DLQ 长度是关键告警：`xarch_dlq_size > 100` 持续 5 分钟 → P2
- 消费者滞后：`rabbitmq_queue_messages_ready / messages_unacknowledged`

---

## 7. 常见问题

### Q: 消费者收不到消息？

1. 检查队列是否绑定到正确的 exchange + routing key
2. 查看 `/api/actuator/health` 中 RabbitMQ 状态
3. 看 `org.springframework.amqp` 包的日志（debug 级别）

### Q: 消息一直重复？

- 消费者持续抛异常 → 检查重试次数
- `acknowledge-mode` 是否正确（AUTO/MANUAL）
- 是否忘记在 try-catch 之外抛异常以触发重试

### Q: DLQ 一直空？

- `x-dead-letter-exchange` 参数没设
- 或 broker 端 Queue 声明覆盖了 client 声明
- 或重试用尽后没抛 `AmqpRejectAndDontRequeueException`

### Q: 想实现广播？

用 fanout exchange，每个消费者组绑定自己的 queue。

### Q: Spring AMQP 5 vs 6？

xarch-mq 基于 Spring AMQP 3.1+（Spring Boot 3.4 配套）。注意：
- `RabbitTemplate.convertAndSend` 行为不变
- `@RabbitListener` 仍可用，但我们推荐 `@XarchMessageListener` 获得重试 + DLQ
- `MessageConverter` 替换为 Jackson 而不是 SimpleMessageConverter

---

## 8. 相关链接

- 源码: `backend/xarch-spring-boot-starter/xarch-mq-spring-boot-starter/`
- 示例: `backend/xarch-example/src/main/java/com/xarch/example/mq/`
- Docker: `docker-compose.yml` 中 `rabbitmq` 服务
- Spring AMQP 文档: <https://docs.spring.io/spring-amqp/reference/>
- RabbitMQ 文档: <https://www.rabbitmq.com/documentation.html>
- xarch-tracing: `docs/TRACING.md`
- xarch-resilience: `docs/RESILIENCE.md`

---

最后更新：2026-07-01
