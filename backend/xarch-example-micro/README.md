# xarch-example-micro

Spring Cloud micro-service decomposition of the `xarch-example` monolithic application.

This is a **structural refactor** — the original `xarch-example` module continues to work.
Each new micro-service is a stand-alone Spring Boot 3.4 application that can be deployed
independently once it is wired into Nacos.

---

## Why split?

| Reason | Benefit |
|---|---|
| Single deployable per concern | Faster, isolated releases |
| Independent scaling | Scale only the hot service (e.g. file uploads) |
| Failure isolation | One bad service does not bring down the whole system |
| Team ownership | Smaller codebases per team |
| Tech flexibility | Each service may evolve independently |

## Module map

| Module | Port | Spring application name | Responsibility | Depends on |
|---|---|---|---|---|
| `common`               | —    | —                          | Shared DTOs, constants, util | (library only) |
| `service-auth`         | 9001 | `xarch-service-auth`       | Auth, login, captcha, users  | web, db, cache, nacos, feign |
| `service-system`       | 9002 | `xarch-service-system`     | Roles, menus, depts, dicts, config, logs, notice, post, online | web, db, cache, feign |
| `service-file`         | 9003 | `xarch-service-file`       | File upload/download, resource, temp file, Excel | web, db, cache, feign |
| `service-monitor`      | 9004 | `xarch-service-monitor`    | Server info, cache monitor, scheduled jobs | web, db, cache, feign |
| `service-ai`           | 9005 | `xarch-service-ai`         | Chat, command audit, MCP, RAG, server (AI), skill, task, user behavior | web, db, cache, feign |
| `service-message`      | 9006 | `xarch-service-message`    | Messages, OAuth/SSO clients | web, db, cache, feign |

All services register with **Nacos** (`nacos.xarch:8848`, namespace `xarch-cloud`).

## Inter-service calls

Cross-service calls use **OpenFeign** with declarative interfaces.

```java
// inside service-system
@FeignClient(name = "xarch-service-auth", path = "/api/users")
public interface UserFeignClient {
    @GetMapping("/{id}")
    ApiResult<UserDTO> getById(@PathVariable Long id);
}
```

Use Feign for:

* Reading reference data (e.g. system displaying user name → `xarch-service-auth`)
* Triggering actions across bounded contexts (e.g. audit log shipped to message bus)

**Never** share a database transaction across services — use eventual consistency
(local TX + async event / outbox).

## How to run

### 1. Local infra (via docker-compose)

```bash
docker-compose up -d nacos mysql redis
```

### 2. Build all services

```bash
./gradlew clean build
```

### 3. Start a single service

```bash
./gradlew :service-auth:bootRun
```

Or with the produced jar:

```bash
java -jar service-auth/build/libs/service-auth-1.0.0.jar
```

### 4. Start everything

```bash
docker-compose up -d
```

## Migration guide

See [MIGRATION.md](./MIGRATION.md) for the controller-to-service map and breaking changes.

## Module structure

```
xarch-example-micro/
├── settings.gradle
├── build.gradle
├── docker-compose.yml
├── README.md
├── MIGRATION.md
├── common/                  shared DTOs, constants, util
├── service-auth/            9001
├── service-system/          9002
├── service-file/            9003
├── service-monitor/         9004
├── service-ai/              9005
└── service-message/         9006
```