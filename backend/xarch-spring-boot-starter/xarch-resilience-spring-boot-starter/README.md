# xarch-resilience-spring-boot-starter

A reusable Spring Boot starter that bundles the four classical resilience
patterns on top of [Resilience4j](https://resilience4j.readme.io/) 2.2.0:

| Pattern          | What it does                                  | Annotation       |
| ---------------- | --------------------------------------------- | ---------------- |
| Rate limiter     | Caps request rate per IP / user / global      | `@RateLimit`     |
| Circuit breaker  | Fails fast when a downstream is misbehaving   | `@CircuitBreaker`|
| Retry            | Retries with exponential backoff              | `@Retry`         |
| Bulkhead         | Concurrency cap per method                    | `@Bulkhead`      |

It also registers a global servlet filter that rate-limits every
incoming HTTP request out of the box.

---

## 1. Installation

```gradle
implementation 'com.xarch:xarch-resilience-spring-boot-starter:1.0.0'
```

The starter brings in Resilience4j, Spring AOP, and Micrometer. Add
`spring-boot-starter-data-redis` if you want the cluster-wide rate
limiter (recommended for production).

---

## 2. Quick start

```yaml
xarch:
  resilience:
    enabled: true
    rate-limit:
      permits-per-second: 100
      burst-capacity: 100
      default-scope: IP
```

```java
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;

    @RateLimit(permitsPerSecond = 10, scope = RateLimit.Scope.IP)
    @PostMapping("/login")
    public ApiResult<Token> login(@RequestBody LoginRequest req) {
        return users.login(req);
    }
}

@Service
public class UserService {

    @CircuitBreaker(name = "user-query", fallback = "fallbackUser")
    public UserDTO getUser(Long id) {
        return remoteCall(id);
    }

    @SuppressWarnings("unused")
    private UserDTO fallbackUser(Long id, Throwable ex) {
        return UserDTO.cached(id);
    }
}
```

---

## 3. Configuration reference

See `META-INF/additional-spring-configuration-metadata.json` for the
full list. Highlights:

```yaml
xarch:
  resilience:
    rate-limit:
      servlet-enabled: true        # global filter
      permits-per-second: 100
      burst-capacity: 100
      default-scope: IP            # IP | USER | GLOBAL
      timeout-millis: 0            # 0 = fail fast
      redis-key-prefix: "xarch:rl:"
    circuit-breaker:
      failure-rate-threshold: 50   # %
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

---

## 4. Rate limiter

### Global servlet filter

Automatically registered for `/*` unless you set
`servlet-enabled: false`. It reads the client IP from
`X-Forwarded-For` / `X-Real-IP` (gateway-friendly) and rejects with
`429 Too Many Requests` when the bucket is empty.

### `@RateLimit` annotation

```java
@RateLimit(permitsPerSecond = 5, scope = RateLimit.Scope.USER, timeoutMillis = 0)
public ApiResult<Order> placeOrder(OrderRequest req) { ... }
```

The bucket is updated by a single Redis Lua script
(`EVAL … KEYS=[key] ARGV=[pps, burst, now, requested]`) which is
atomic and adds at most 1 ms of overhead per request.

---

## 5. Circuit breaker

```java
@CircuitBreaker(name = "user-query", fallback = "fallbackUser")
public UserDTO getUser(Long id) { ... }
```

The breaker is created on first invocation with the values from
`xarch.resilience.circuit-breaker.*`. Override per method:

```java
@CircuitBreaker(name = "billing",
                 slidingWindowSize = 20,
                 failureRateThreshold = 30.0f,
                 waitDurationInOpenStateMillis = 30000,
                 fallback = "fallback")
public Bill charge(Card card) { ... }
```

When the breaker is open, the configured fallback is invoked. If it
returns an `ApiResult`, the starter wraps the call so callers still get
a typed response (code = 503, message = "Service degraded").

---

## 6. Retry

```java
@Retry(maxAttempts = 5, delay = 200, multiplier = 2.0, maxDelay = 4000)
public Status callRemote() { ... }
```

Only `RuntimeException` is retried by default. For finer control,
register a custom `RetryConfig` in the `RetryRegistry`.

---

## 7. Bulkhead

```java
@Bulkhead(name = "report-gen", concurrentCalls = 10)
public Report generateReport() { ... }
```

When the cap is reached, the call fails with `BulkheadFullException`.

---

## 8. Metrics

Every pattern exports Micrometer metrics out of the box:

| Metric                                  | Tags                                |
| --------------------------------------- | ----------------------------------- |
| `resilience4j_circuitbreaker_state`     | `name`                              |
| `resilience4j_circuitbreaker_failure_rate` | `name`                          |
| `resilience4j_ratelimiter_available_permissions` | `name`                   |
| `resilience4j_retry_calls`              | `name`, `kind` (successful, failed) |
| `resilience4j_bulkhead_available_concurrent_calls` | `name`                |

Expose them via the actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,circuitbreakers,circuitbreakerevents,ratelimiters
```

---

## 9. Best practices

* **Always set `failureRateThreshold` and `minimumNumberOfCalls`** —
  a too-aggressive breaker trips on a single hiccup.
* **Keep `permitsPerSecond` ≤ `burstCapacity`** — otherwise the
  burst setting has no effect.
* **Use a fallback that returns real data** when possible (cached,
  default). An empty `Optional` is fine for read APIs.
* **Stack patterns**: `@Bulkhead` first, then `@CircuitBreaker`, then
  `@Retry`, then `@RateLimit`. Use Spring's `@Order` on the aspects if
  you need different ordering.
* **Disable the global filter in tests** with
  `xarch.resilience.rate-limit.servlet-enabled: false`.

---

## 10. Performance

* Rate limiter: a single Redis round-trip per request (~0.2-1 ms
  on a colocated node).
* Circuit breaker: in-process counter, no I/O. < 50 µs per call.
* Retry: zero overhead until a failure occurs.
* Bulkhead: in-process semaphore. < 10 µs per call.

End-to-end overhead is typically **1-5 %** at p99.
