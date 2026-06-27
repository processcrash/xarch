# common

Shared library used by every micro-service in `xarch-example-micro`.

This module deliberately has **no Spring Boot / Spring Cloud runtime** —
it is a plain `java-library` jar so it can be wired into both services
and (potentially) test utilities without dragging in a full application
context.

## What lives here

| Package | Purpose |
|---|---|
| `com.xarch.example.common.constants` | Canonical Spring application names (`XARCH_SERVICE_AUTH`, …) |
| `com.xarch.example.common.dto`       | `ErrorCode` enum with HTTP-style status codes |
| `com.xarch.example.common.util`      | `BeanCopyUtil` — thin wrapper around `BeanUtils.copyProperties` with logging |
| `com.xarch.example.common.response`  | `MicroResponse<T>` — uniform success/failure envelope for Feign payloads |
| `com.xarch.example.common.feign`     | `UserFeignFallback`, `FileFeignFallback` — default fallbacks for cross-service calls |

## Why a record-based response?

`MicroResponse<T>(int code, String message, T data)` mirrors
`com.xarch.starter.core.result.ApiResult` so Feign clients can decode a
shared envelope without depending on the starter at compile time. The
helper factories `ok(T)`, `ok()`, and `fail(int, String)` keep call
sites tidy.

## Build

```bash
./gradlew :common:build
```

The produced jar is consumed by every `service-*` module via
`implementation project(':common')`.