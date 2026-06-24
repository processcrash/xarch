# Migration Guide — xarch-example  →  xarch-example-micro

This document describes how to move from the monolithic `xarch-example`
to the new micro-service decomposition in `xarch-example-micro/`.

---

## 1. Controller → Service mapping

| Old controller (in `xarch-example`) | New service | New port | Notes |
|---|---|---|---|
| `AuthController` (now `CaptchaController`) | `service-auth` | 9001 | Login/logout/captcha |
| `UserController`                   | `service-auth` | 9001 | User CRUD + role assignment |
| `RoleController`                   | `service-system` | 9002 | |
| `MenuController`                   | `service-system` | 9002 | |
| `DeptController`                   | `service-system` | 9002 | |
| `DictController`                   | `service-system` | 9002 | Dict + DictData |
| `ConfigController`                 | `service-system` | 9002 | |
| `OpLogController`                  | `service-system` | 9002 | |
| `LoginLogController`               | `service-system` | 9002 | |
| `SysNoticeController`              | `service-system` | 9002 | |
| `SysPostController`                | `service-system` | 9002 | |
| `SysUserOnlineController`          | `service-system` | 9002 | |
| `FileController`                   | `service-file`   | 9003 | Storage strategy endpoints |
| `ResourceController`               | `service-file`   | 9003 | |
| `TempFileController`               | `service-file`   | 9003 | |
| `ExcelController`                  | `service-file`   | 9003 | |
| `SysServerController`              | `service-monitor` | 9004 | |
| `SysCacheController`               | `service-monitor` | 9004 | |
| `SysJobController`                 | `service-monitor` | 9004 | |
| `SysJobLogController`              | `service-monitor` | 9004 | |
| `ChatController`                   | `service-ai`     | 9005 | (planned) |
| `CommandAuditController`           | `service-ai`     | 9005 | |
| `McpManagementController`          | `service-ai`     | 9005 | (planned) |
| `McpToolController`                | `service-ai`     | 9005 | (planned) |
| `RagController`                    | `service-ai`     | 9005 | (planned) |
| `ServerController` (AI)            | `service-ai`     | 9005 | (= `ai/ServerManageController`) |
| `SkillController`                  | `service-ai`     | 9005 | (planned) |
| `TaskController`                   | `service-ai`     | 9005 | (planned) |
| `UserBehaviorController`           | `service-ai`     | 9005 | (planned) |
| `MessageController`                | `service-message` | 9006 | |
| `ClientController`                 | `service-message` | 9006 | |

> Controllers listed as **(planned)** are referenced in the spec but not yet
> committed in `xarch-example`. The service skeleton still ships the directory
> and `application.yml` so they can be added incrementally.

---

## 2. Breaking API changes

### 2.1 Port

The monolith exposed everything on a single port (e.g. `8080`).
Each micro-service uses its own port:

| Old | New |
|---|---|
| `http://xarch.example.com/api/users` | `http://xarch.example.com:9001/api/users` |
| `http://xarch.example.com/api/roles` | `http://xarch.example.com:9002/api/roles` |

If you front the services with the `xarch-cloud-starter-gateway` (already
in the repo), the gateway can map `/api/**` → `xarch-service-auth` and so on,
which makes the port change transparent for the front-end.

### 2.2 Path prefixes

Most paths are unchanged. Two known changes:

* `AuthController` was renamed to `CaptchaController` (path `/api/common/captcha`).
* The AI `ServerController` is `/ai/server` in the monolith; in
  `service-ai` it remains `/ai/server` but is now served by port `9005`.

### 2.3 Service-to-service calls

Code that used to call a service directly (same JVM) must now use **Feign**.

Example (was a direct call into `UserService`):

```java
// Monolith
User u = userService.getById(1L);
```

In a micro-service:

```java
@Resource
private UserFeignClient userFeignClient;

UserDTO u = userFeignClient.getById(1L).getData();
```

`UserFeignClient` lives in `service-auth` and is reused by other services
through the dependency on `service-auth`’s exported `client` package.

### 2.4 Distributed transactions

There is **no** global transaction across services. Patterns to adopt:

* **Local TX** per service, publish an event after commit.
* **Outbox pattern** for guaranteed delivery.
* **Saga** for long-running multi-service workflows.

---

## 3. Step-by-step migration

1. **Cut over DNS** — point the front-end at the gateway host.
2. **Bring up infra** — `docker-compose up -d nacos mysql redis`.
3. **Boot `service-auth` first** — others depend on user lookups via Feign.
4. **Boot `service-system`, `service-message`, `service-file`, `service-monitor`,
   `service-ai`** in any order; each registers with Nacos and is reachable
   through the gateway.
5. **Switch traffic** — keep monolith running as fallback during the cut-over
   window; flip the gateway routes.
6. **Decommission** — once all routes are stable, retire `xarch-example`.

### Recommended cut-over order

```
service-auth (users & login)  ──►  service-system (RBAC)  ──►
service-message / service-file / service-monitor / service-ai
```

---

## 4. Data consistency

**Short answer:** keep one logical database; let each service own its own tables.

* `service-auth` owns `sys_user`, `sys_user_role`, `sys_login_log`
* `service-system` owns `sys_role`, `sys_menu`, `sys_dept`, `sys_dict`,
  `sys_dict_data`, `sys_config`, `sys_op_log`, `sys_notice`, `sys_post`,
  `sys_user_online`
* `service-file` owns `sys_resource`, `sys_temp_file`, `sys_storage_config`
* `service-monitor` owns `sys_job`, `sys_job_log`
* `service-ai` owns `ai_server`, `ai_command_audit`, `ai_command_history`
* `service-message` owns `sys_message`, `sys_client`

Initial database migration is the same schema as the monolith (one MySQL
instance, many services). To split schemas later, use the
**schema-per-service** pattern — each service gets its own database user
with limited grants on the tables it owns.

Cross-service joins (e.g. audit log → user name) are forbidden; do an extra
Feign call instead.

---

## 5. What is included in this scaffold

* All 6 services plus the shared `common` module.
* Each service has:
  * `<Micro>Application` — Spring Boot main class with `@EnableDiscoveryClient`,
    `@EnableFeignClients`, `@MapperScan`, `@EnableTransactionManagement`.
  * `bootstrap.yml` + `application.yml` — Nacos discovery + config.
  * `build.gradle` — Spring Boot 3.4, Spring Cloud 2023.0.3, Nacos 2023.0.1.0,
    OpenFeign 4.1.0, MyBatis-Flex 1.9.0, MySQL 8.4.0, Java 25.
  * Controllers migrated from `xarch-example` (same logic, relocated packages).
  * Entity stubs (copied from the monolith) + empty mappers.
* `FeignClient` interfaces for cross-service calls in
  `service-auth` (`UserFeignClient`) and `service-file` (`FileFeignClient`).
* `docker-compose.yml` — Nacos, MySQL, Redis, all 6 services on bridge network `xarch-net`.
* `README.md` per service.

> Service **implementations** are intentionally NOT included — this is
> structural scaffolding only. Each service compiles with stub services
> and stubs out business logic to be filled in later.