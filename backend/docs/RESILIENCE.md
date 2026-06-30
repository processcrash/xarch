# Resilience in xarch

This document covers the xarch resilience patterns: rate limiting,
circuit breaking, retry and bulkhead. It is the canonical reference for
the `xarch-resilience-spring-boot-starter` and the gateway integration.

---

## 1. Overview

The starter is built on [Resilience4j](https://resilience4j.readme.io)
2.2.0 and exposes four patterns:

| Pattern          | Module                               | Annotation / Filter             |
| ---------------- | ------------------------------------ | ------------------------------- |
| Rate limiter     | `ratelimit`                          | `@RateLimit`, `RateLimitFilter` |
| Circuit breaker  | `circuitbreaker`                     | `@CircuitBreaker`               |
| Retry            | `retry`                              | `@Retry`                        |
| Bulkhead         | `bulkhead`                           | `@Bulkhead`                     |

All patterns:

* **Spring Boot 3.4 / JDK 25** compatible.
* **Metrics-first** — every aspect exports Micrometer gauges, timers
  and counters under `resilience4j_*`.
* **AOP driven** — annotations are picked up by `@Aspect` classes
  registered in `XarchResilienceAutoConfiguration`.
* **Zero-config fallback** — the starter works out of the box with
  sensible defaults.

The starter is master-switched by `xarch.resilience.enabled` (default
`true`). Setting it to `false` removes every bean and the annotations
become no-ops.

---

## 2. Configuration reference

```yaml
xarch:
  resilience:
    enabled: true                       # master switch
    rate-limit:
      servlet-enabled: true             # global servlet filter
      permits-per-second: 100           # default bucket rate
      burst-capacity: 100               # bucket max tokens
      default-scope: IP                 # IP | USER | GLOBAL
      timeout-millis: 0                 # 0 = fail fast
      redis-key-prefix: "xarch:rl:"
    circuit-breaker:
      failure-rate-threshold: 50
      sliding-window-size: 100
      minimum-number-of-calls: 20
      wait-duration-in-open-state-millis: 10000
      permitted-number-of-calls-in-half-open-state: 5
    retry:
      max-attempts: 3
      delay-millis: 200
      multiplier: 2.0
      max-delay-millis: 2000
    bulkhead:
      concurrent-calls: 25
      max-wait-duration-millis: 0
```

The full list of properties is also exported as Spring metadata, so
your IDE will autocomplete the keys.

---

## 3. Annotation usage

### `@RateLimit`

```java
@RateLimit(permitsPerSecond = 10, scope = RateLimit.Scope.USER)
public ApiResult<Token> login(LoginRequest req) { ... }
```

* `permitsPerSecond` — tokens per second. Minimum 1.
* `burstCapacity` — max tokens in the bucket.
* `scope` — `IP`, `USER` (uses `X-User-Id` header), or `GLOBAL`.
* `timeoutMillis` — wait time for a permit. `0` means fail fast.
* `key` — optional SpEL expression for the bucket key (e.g.
  `key = "#req.tenantId"` for per-tenant limits).

The aspect uses a single Redis round-trip per call.

### `@CircuitBreaker`

```java
@CircuitBreaker(name = "user-query",
                 slidingWindowSize = 20,
                 failureRateThreshold = 50.0f,
                 fallback = "fallbackUser")
public UserDTO getUser(Long id) { ... }

private UserDTO fallbackUser(Long id, Throwable ex) {
    return cache.get(id).orElse(UserDTO.empty(id));
}
```

* `name` — breaker name. The breaker is registered on first
  invocation and reused.
* `fallback` — name of a sibling method. May accept the original
  arguments plus an optional trailing `Throwable` for the cause.
* `slidingWindowSize` / `failureRateThreshold` /
  `waitDurationInOpenStateMillis` — override the global defaults.
  `0` / `-1` / `-1.0f` means "use the value from
  `xarch.resilience.circuit-breaker.*`".

### `@Retry`

```java
@Retry(maxAttempts = 5, delay = 200, multiplier = 2.0, maxDelay = 4000)
public Status callRemote() { ... }
```

* `maxAttempts` — number of attempts including the first call.
* `delay` / `multiplier` / `maxDelay` — exponential backoff
  parameters.

Only `RuntimeException` triggers a retry by default. For finer
control, register a custom `RetryConfig` in the `RetryRegistry` bean.

### `@Bulkhead`

```java
@Bulkhead(name = "report-gen", concurrentCalls = 10)
public Report generateReport() { ... }
```

* `name` — bulkhead name. Defaults to the method signature.
* `concurrentCalls` — max concurrent invocations.
* `maxWait` — wait time for a permit (`-1` = fail fast).

---

## 4. Gateway filter configuration

The gateway starter wires the rate limit + circuit breaker filters
into Spring Cloud Gateway. Two filter factories are exposed:

* `RateLimit` — bound to the shared `RedisRateLimiter`. Configurable
  per route.
* `CircuitBreakerGW` — wraps the downstream chain in a Resilience4j
  circuit breaker using the reactive operator.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: system-user
          uri: lb://xarch-example
          predicates:
            - Path=/api/system/user/**
          filters:
            - name: RateLimit
              args:
                permitsPerSecond: 50
                burstCapacity: 50
                scope: IP
            - name: CircuitBreakerGW
              args:
                name: xarch-example-user
                failureRateThreshold: 50
                slidingWindowSize: 20
                minimumNumberOfCalls: 10
                waitDurationInOpenStateMillis: 10000
```

The full sample lives in
`xarch-cloud-starter-gateway/src/main/resources/application-gateway.yml`.

---

## 5. Redis Lua script explained

The rate limiter is implemented as a single atomic Lua script:

```lua
local key = KEYS[1]
local permits_per_second = tonumber(ARGV[1])
local burst_capacity = tonumber(ARGV[2])
local now_millis = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local data = redis.call('HMGET', key, 'tokens', 'lastRefill')
local tokens = tonumber(data[1])
local last_refill = tonumber(data[2])

if tokens == nil then
    tokens = burst_capacity
    last_refill = now_millis
end

local elapsed = math.max(0, now_millis - last_refill) / 1000.0
local refill = elapsed * permits_per_second
tokens = math.min(burst_capacity, tokens + refill)

local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now_millis)
redis.call('PEXPIRE', key, 60000)
return allowed
```

Why a Lua script?

* **Atomic** — refill + decrement happens in a single Redis
  round-trip with no client-side locking.
* **Cheap** — under 0.2 ms on a colocated Redis.
* **Stateless** — the JVM only knows the key prefix. The bucket
  state lives entirely in Redis, so it works across instances.

The fallback (in-process) implementation mirrors the same algorithm
for tests and local dev.

---

## 6. Monitoring & metrics

Expose the actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,circuitbreakers,circuitbreakerevents,ratelimiters,retries,bulkheads
  endpoint:
    health:
      show-details: always
```

| Metric                                               | Tags                       |
| ---------------------------------------------------- | -------------------------- |
| `resilience4j_circuitbreaker_state`                  | `name`                     |
| `resilience4j_circuitbreaker_failure_rate`           | `name`                     |
| `resilience4j_circuitbreaker_calls`                  | `name`, `kind`             |
| `resilience4j_ratelimiter_available_permissions`     | `name`                     |
| `resilience4j_retry_calls`                          | `name`, `kind`             |
| `resilience4j_bulkhead_available_concurrent_calls`   | `name`                     |
| `xarch_resilience_retries_triggered`                 | -                          |
| `xarch_resilience_retries_succeeded`                 | -                          |
| `xarch_resilience_retries_exhausted`                 | -                          |

Grafana dashboards can pin the four metrics above to a single panel
and alert on `resilience4j_circuitbreaker_state{name=...} == 1`
(open) or
`resilience4j_ratelimiter_available_permissions{name=...} < 1`
(exhausted).

---

## 7. Best practices

* **Set `minimumNumberOfCalls` high enough** so a single
  timeout does not trip the breaker. A common starting point is
  `slidingWindowSize / 2`.
* **Use a fallback that returns real data** — a cached or default
  value, not just an empty `Optional`. The user gets a usable
  response and your service stays available.
* **Keep retry attempts short**. Exponential backoff is great for
  transient errors but a `maxAttempts = 10` will hold an HTTP
  connection for 30+ seconds.
* **Don't combine `@Retry` and `@CircuitBreaker` on the same
  method.** Retry hides failures, which makes the breaker think the
  call succeeded. If you need both, put retry on a private helper
  and the breaker on the public method.
* **Disable the global filter in tests** with
  `xarch.resilience.rate-limit.servlet-enabled: false`. Per-method
  annotations still work because they're driven by AOP, not the
  filter.
* **Use scope = USER for login** — IP-based limits punish entire
  NAT'd corporate networks.

---

## 8. Performance

* **Rate limiter** — single Redis round-trip per request
  (~0.2-1 ms on a colocated node, ~1-3 ms across a network).
* **Circuit breaker** — in-process counter, no I/O. < 50 µs per call.
* **Retry** — zero overhead until a failure occurs. Backoff sleeps
  the calling thread.
* **Bulkhead** — in-process semaphore. < 10 µs per call.

End-to-end overhead is **1-5%** at p99 on a typical web request.
