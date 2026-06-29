# xarch-tracing-spring-boot-starter

Distributed tracing for xarch services, powered by OpenTelemetry.

## Installation

The starter is included automatically by `xarch-web-spring-boot-starter`.
If you want to add tracing to a `xarch-core`-only module, declare the
dependency explicitly:

```gradle
dependencies {
    implementation 'com.xarch:xarch-tracing-spring-boot-starter:1.0.0'
}
```

## Quick start

```yaml
xarch:
  tracing:
    enabled: true
    service-name: order-service
    exporter: otlp                 # OTLP | LOGGING | ZIPKIN
    endpoint: http://jaeger:4317   # Jaeger / Tempo / SigNoz
    sample-rate: 1.0               # 0.0 disables, 1.0 keeps everything
```

That's it. On startup the starter:

1. Registers a global `OpenTelemetry` instance.
2. Adds a servlet filter that opens a SERVER span per request and
   propagates W3C `traceparent`.
3. Registers `RestTemplateTraceInterceptor` and `WebClientTraceFilter`
   beans for outbound propagation.
4. Pushes `traceId` / `spanId` into SLF4J MDC for log correlation.

## Logback pattern

Add the bundled pattern to `logback-spring.xml`:

```xml
<configuration>
  <include resource="logback-trace-pattern.xml"/>
</configuration>
```

Output becomes:

```
2026-06-29 14:02:01.012  INFO [4bf92f3577b34da6a3ce929d0e0e4736,00f067aa0ba902b7] [http-nio-8080-exec-3] c.x.s.order.OrderService : reserving stock
```

## Configuration

| Property                            | Default                        | Notes                                                  |
| ----------------------------------- | ------------------------------ | ------------------------------------------------------ |
| `xarch.tracing.enabled`             | `true`                         | Master switch.                                         |
| `xarch.tracing.service-name`        | `${spring.application.name}`   | Logical service name on every span.                    |
| `xarch.tracing.exporter`            | `OTLP`                         | `OTLP`, `LOGGING` or `ZIPKIN`.                         |
| `xarch.tracing.endpoint`            | OTLP `4317`, Zipkin `9411`     | Collector endpoint, default depends on exporter.       |
| `xarch.tracing.sample-rate`         | `1.0`                          | Probability of sampling a new root trace.              |
| `xarch.tracing.propagators`         | `[tracecontext, baggage]`      | W3C propagators.                                       |
| `xarch.tracing.resource-attributes` | `{}`                           | Extra `Resource` attributes (env, version, region...). |

## Custom spans from business code

```java
String result = XarchTraceContext.withSpan("compute-discount", () -> {
    log.info("calculating discount for cart={}", cartId);
    return pricingService.discount(cartId);
});
```

`XarchTraceContext.withSpan(name, supplier)` opens an internal span,
pushes `traceId`/`spanId` into MDC, records exceptions, and restores MDC
afterwards.

## Deployment recipes

### Jaeger

```bash
docker run --rm -d --name jaeger \
  -p 16686:16686 -p 4317:4317 \
  jaegertracing/all-in-one:1.55
```

```yaml
xarch.tracing.exporter: otlp
xarch.tracing.endpoint: http://jaeger:4317
```

Open `http://localhost:16686`.

### Zipkin

```bash
docker run --rm -d --name zipkin \
  -p 9411:9411 openzipkin/zipkin:latest
```

```yaml
xarch.tracing.exporter: zipkin
xarch.tracing.endpoint: http://zipkin:9411/api/v2/spans
```

Open `http://localhost:9411`.

### SigNoz / Tempo / Honeycomb

All speak OTLP gRPC — set `exporter: otlp` and point `endpoint` at the
collector.

## Disabling in tests

```java
@SpringBootTest(properties = "xarch.tracing.enabled=false")
```

See `docs/TRACING.md` in the repository root for the full reference.