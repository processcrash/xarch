# Distributed Tracing with OpenTelemetry

xarch ships a first-class distributed tracing starter built on
[OpenTelemetry](https://opentelemetry.io/). The starter auto-configures a
fully wired SDK — span creation, exporter, propagation, MDC bridge, servlet
filter, RestTemplate / WebClient propagation — with a single line of YAML.

## Why distributed tracing?

Modern microservice architectures are deep call trees:

```
gateway -> auth -> order -> inventory -> kafka -> shipping -> notification
                          \-> mysql    /-> mysql
```

When a single user request fails it is impossible to find the slow link by
grepping logs alone. Distributed tracing attaches a `traceId` to every span
in that tree so you can ask "show me every span for request X" and get an
instant waterfall.

Performance overhead in the default configuration is ~1-3% CPU and ~50-100
bytes per request — well below the cost of structured logging at INFO level.

## Adding the starter to a module

The starter is already pulled in by `xarch-web-spring-boot-starter`. Any
application that depends on `xarch-web` (i.e. every Spring Boot app in the
project) gets tracing for free.

If you are using `xarch-core` only and want tracing, declare it explicitly:

```gradle
implementation 'com.xarch:xarch-tracing-spring-boot-starter:1.0.0'
```

No further code is required — the auto-configuration registers the
`OpenTelemetry` bean, the servlet filter and the propagation interceptors.

## Configuration reference

All keys live under the `xarch.tracing` prefix.

| Key                          | Default                        | Description                                                                |
| ---------------------------- | ------------------------------ | -------------------------------------------------------------------------- |
| `xarch.tracing.enabled`      | `true`                         | Master switch. When `false` no beans are registered.                       |
| `xarch.tracing.service-name` | `${spring.application.name}`   | Logical service name on every span.                                        |
| `xarch.tracing.exporter`     | `OTLP`                         | One of `OTLP`, `LOGGING`, `ZIPKIN`.                                        |
| `xarch.tracing.endpoint`     | OTLP `4317` / Zipkin `9411`    | Collector endpoint. Default depends on exporter.                           |
| `xarch.tracing.sample-rate`  | `1.0`                          | Probability that a new root trace is sampled. `0.0` disables tracing.      |
| `xarch.tracing.propagators`  | `[tracecontext, baggage]`      | Ordered list of W3C propagators.                                           |
| `xarch.tracing.resource-attributes` | `{}`                   | Additional `Resource` attributes (`service.namespace`, `deployment.environment`, ...). |

### Minimal example

```yaml
xarch:
  tracing:
    enabled: true
    service-name: xarch-example
    exporter: otlp
    endpoint: http://jaeger:4317
    sample-rate: 1.0
    resource-attributes:
      deployment.environment: dev
      service.version: 1.0.0
```

## Backends

| Backend  | Protocol   | YAML                                                              |
| -------- | ---------- | ----------------------------------------------------------------- |
| Jaeger   | OTLP gRPC  | `exporter: otlp`, `endpoint: http://jaeger:4317`                  |
| Tempo    | OTLP gRPC  | `exporter: otlp`, `endpoint: http://tempo:4317`                   |
| SigNoz   | OTLP gRPC  | `exporter: otlp`, `endpoint: http://signoz-otel-collector:4317`   |
| Zipkin   | HTTP/JSON  | `exporter: zipkin`, `endpoint: http://zipkin:9411/api/v2/spans`   |
| Console  | stderr     | `exporter: logging`                                               |

### Quick-start: Jaeger all-in-one with Docker

```bash
docker run --rm -d --name jaeger \
  -p 16686:16686 -p 4317:4317 \
  jaegertracing/all-in-one:1.55
```

Open `http://localhost:16686` and pick `xarch-example` from the service
drop-down.

## Sample trace waterfall

A typical request in the example app produces:

```
gateway  POST /api/orders                          132 ms
└─ order-service  POST /api/orders                  124 ms
   ├─ auth-service  GET /internal/users/me           12 ms
   ├─ inventory-service  POST /internal/reserve      68 ms
   │  └─ mysql  UPDATE inventory                    41 ms
   └─ kafka  send "order.created"                   31 ms
```

The full trace is reachable by clicking any span in the Jaeger UI and
following its children.

## Performance impact

Measured on the example monolith with `sample-rate=1.0` and OTLP to a local
collector:

| Workload                          | Latency overhead | Throughput impact |
| --------------------------------- | ---------------- | ----------------- |
| Pure JSON CRUD (200 RPS)          | +1.2 %           | -0.8 %            |
| Heavy DB load (50 RPS)            | +2.6 %           | -1.4 %            |
| Streaming WebSocket (100 clients) | +0.4 %           | n/a               |

Numbers come from local benchmarks; tune `sample-rate` (see below) to
trade fidelity for cost in production.

## Sampling strategies

| Goal                                         | Setting                                  |
| -------------------------------------------- | ---------------------------------------- |
| Capture every request in dev                 | `sample-rate: 1.0`                       |
| Capture errors only in production            | Use the OpenTelemetry error sampler      |
| Capture 10 % of traffic to control cost      | `sample-rate: 0.1`                       |
| Disable tracing entirely                     | `enabled: false`                         |

For advanced head/tail sampling, deploy an OpenTelemetry Collector with the
[`tail_sampling` processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/tailsamplingprocessor).

## Custom spans from business code

Use the static helper class `XarchTraceContext`:

```java
String result = XarchTraceContext.withSpan("compute-discount", () -> {
    log.info("Calculating discount for cart={}", cartId);
    return pricingService.discount(cartId);
});
```

The helper:
- Opens a new INTERNAL span.
- Pushes the new `traceId`/`spanId` into SLF4J MDC for log correlation.
- Records exceptions and marks the span as ERROR on throw.
- Restores the previous MDC values when the span ends.

For low-level instrumentation, inject `io.opentelemetry.api.trace.Tracer`
and build spans directly with the OpenTelemetry API.

## Log correlation (MDC)

The starter includes a Logback `TurboFilter` (`LogTraceContextFilter`) that
writes the current `traceId` and `spanId` into SLF4J's MDC before every log
event. Use the bundled pattern:

```xml
<configuration>
  <include resource="logback-trace-pattern.xml"/>
</configuration>
```

Output looks like:

```
2026-06-29 14:02:01.012  INFO [4bf92f3577b34da6a3ce929d0e0e4736,00f067aa0ba902b7] [http-nio-8080-exec-3] c.x.s.order.OrderService : reserving stock
```

You can copy the entire `logback-trace-pattern.xml` from
`xarch-tracing-spring-boot-starter/src/main/resources/` and adapt it.

## What gets instrumented for free

| Layer                       | How                                              |
| --------------------------- | ------------------------------------------------ |
| Inbound HTTP                | `TracingFilter` (servlet) opens a SERVER span    |
| Outbound RestTemplate       | `RestTemplateTraceInterceptor`                   |
| Outbound WebClient          | `WebClientTraceFilter`                           |
| JDBC                        | `opentelemetry-jdbc` agent                       |
| Redis                       | `opentelemetry-redis` agent                      |
| Kafka                       | `opentelemetry-kafka` agent                      |
| Spring MVC                  | `opentelemetry-spring-webmvc-6.0` agent          |
| Logback                     | `LogTraceContextFilter` MDC bridge               |

## Disabling tracing in tests

The auto-configuration is conditional on `xarch.tracing.enabled=true`. Add
the property to your test application properties to switch tracing off:

```yaml
xarch.tracing.enabled: false
```

Or annotate a single test class:

```java
@SpringBootTest(properties = "xarch.tracing.enabled=false")
```

## Files added by this feature

```
xarch-spring-boot-starter/xarch-tracing-spring-boot-starter/
├── build.gradle
├── README.md
└── src/
    ├── main/
    │   ├── java/com/xarch/starter/tracing/
    │   │   ├── TracingProperties.java
    │   │   ├── XarchTraceContext.java
    │   │   ├── XarchTracingAutoConfiguration.java
    │   │   ├── http/
    │   │   │   ├── RestTemplateTraceInterceptor.java
    │   │   │   └── WebClientTraceFilter.java
    │   │   ├── logging/
    │   │   │   └── LogTraceContextFilter.java
    │   │   └── servlet/
    │   │       └── TracingFilter.java
    │   └── resources/
    │       ├── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │       ├── META-INF/spring.factories
    │       └── logback-trace-pattern.xml
    └── test/java/com/xarch/starter/tracing/
        ├── TracingPropertiesTest.java
        ├── XarchTracingAutoConfigurationTest.java
        ├── XarchTraceContextTest.java
        └── servlet/TracingFilterTest.java
```