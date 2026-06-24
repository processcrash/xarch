# Changelog

All notable changes to **xarch** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

> See [README.md](README.md) for an overview. Version tags follow the
> `vX.Y.Z` convention and are immutable once released. Bug fixes are released
> as patch versions, new backward-compatible features as minor versions, and
> any breaking change as a new major version.

---

## [Unreleased]

### In Progress

- **P0** — Replace remaining legacy `MyBatis Plus` callers with `MyBatis-Flex`
  in non-example modules and add a migration guide.
- **P0** — Promote `xarch-cloud-starter-mcp` to a first-class starter and
  expose Spring Boot autoconfiguration for it.
- **P1** — Native gRPC bridge between MCP servers (Java <-> Node <-> Python)
  alongside the existing stdio mode.
- **P1** — Vector MCP adapter for `Qdrant` and `Milvus` (backend implementation
  finished; SDK adapters outstanding).
- **P1** — Frontend internationalization (zh-CN / en-US) for the admin shell.
- **P2** — Replace `PageHelper` with a `MyBatis-Flex` native paginator and
  drop the legacy plugin.
- **P2** — WebFlux support in `xarch-web-spring-boot-starter` for non-blocking
  endpoints.
- **P2** — `vector_mcp` Python wheel packaging and publish to a private PyPI.
- **P3** — WASM-based plugin model for first-party MCP servers.
- **P3** — Federated GraphQL gateway aggregation alongside the existing
  REST gateway.

### Planned

- Multi-tenant isolation helpers in `xarch-core-spring-boot-starter`.
- Distributed tracing (OpenTelemetry) auto-instrumentation in the cache
  and db starters.
- Generic code-gen module (`xarch-codegen`) producing CRUD scaffolding
  from database schemas.

---

## [1.0.0] - 2026-06-24

The first production-ready release of **xarch — AI-Enabled Enterprise Backend
Framework**. This release ships the full starter set, three MCP runtimes,
Spring Cloud integration, an end-to-end example application, and the
production deployment story (K8s, Docker, observability).

### Added

#### Backend Framework

- Spring Boot **4.0** baseline on **JDK 25** (records, sealed types, virtual
  threads).
- Gradle (Kotlin DSL) multi-module build with unified version catalog.
- **MyBatis-Flex** ORM (replacing MyBatis Plus for new code) with paginator
  and SQL injection protection out of the box.

#### Starter Modules (`com.xarch.starter.*`)

- `xarch-core-spring-boot-starter` — `ApiResult`, `ResultCode`, global
  exception handler, common utilities, base annotations.
- `xarch-db-spring-boot-starter` — Druid connection pool, multi-dialect
  support (MySQL, PostgreSQL, MongoDB, SQL Server), `MyBatis-Flex` autoconfig.
- `xarch-web-spring-boot-starter` — REST conventions, Knife4j (Swagger 3.0),
  Sa-Token (JWT) auth, request/response interceptors, WebSocket support.
- `xarch-cache-spring-boot-starter` — Redis 7 + Redisson (distributed lock,
  rate limiter, Bloom filter, semaphore).

#### MCP Servers (Java, Node.js, Python)

- `xarch-mcp-database` (Java) — SQL `query` / `execute` / `schema` tools,
  multi-dialect, registered to Nacos as an MCP service.
- `xarch-mcp-knowledge` (Java) — RAG pipeline: document indexing, chunking,
  embedding, semantic search.
- `xarch-mcp-filesystem` (Java) — Path-traversal-proof file ops with
  configurable allowed roots and extensions.
- `xarch-mcp-vector` (Java) — Vector CRUD + KNN, in-process store, pluggable
  backends (Qdrant, Milvus, Chroma, Weaviate, Pinecone, pgvector,
  OpenSearch, Elasticsearch, FAISS).
- Node.js MCP servers (`node-mcp-servers/*`) — TypeScript implementations of
  database / knowledge / filesystem / vector servers; **Bun runtime
  supported**.
- Python MCP servers (`py-mcp-servers/*`, `python/vector_mcp`) — Reference
  Python implementations, stdio mode.

#### Spring Cloud Modules (`com.xarch.cloud.*`)

- `xarch-cloud-starter-nacos` — Nacos 3.2 service registry, MCP service
  registration via `@McpServer` annotation.
- `xarch-cloud-starter-gateway` — Spring Cloud Gateway with dynamic routes
  loaded from Nacos.
- `xarch-cloud-starter-mcp` — MCP protocol core (transport, tool registry,
  error model).
- `xarch-cloud-admin-server` — Spring Boot Admin for fleet-wide health,
  metrics, log levels, thread/heap dumps.

#### Example Application (`xarch-example`) — 17 controllers

- **Auth**: `AuthController` (login, logout, captcha, current user).
- **System**: `UserController`, `RoleController`, `MenuController`,
  `DeptController`, `PostController`, `NoticeController`, `DictController`,
  `ConfigController`.
- **Monitor**: `SysServerController`, `SysCacheController`,
  `SysUserOnlineController`, `SysJobController`, `SysJobLogController`,
  `LoginLogController`, `OpLogController`.
- **Business**: `CaptchaController`, `ClientController` (OAuth2),
  `MessageController`, `ResourceController`, `TempFileController`,
  `CommonController`, `ExcelController`, `FileController` (multi-backend
  storage: local / MinIO / Aliyun OSS), `ServerManageController` (SSH +
  AI agent platform).

#### Frontend (Vue 3 Admin) — 16 views

- Vue **3.5** + Vite **6**, TypeScript, Pinia, Element Plus, Axios.
- Views: Login, Dashboard, User, Role, Menu, Dept, Post, Dict, Config,
  Notice, Log, File, Server Manager, Message, Client, Profile.
- Route guards, API encapsulation layer, pagination component, advanced
  form validation, xterm.js-based SSH terminal.

#### Deployment & Infrastructure

- **8 base Kubernetes resources** under `k8s/base/` (Deployments, Services,
  ConfigMaps, Secrets, Ingress, ServiceAccount, HPA, PDB) plus
  `overlays/dev/` and `overlays/prod/` (Kustomize).
- **Docker Compose** for local development (frontend, backend, Postgres,
  Redis, Nacos, observability stack).
- **Alloy + Loki + Grafana** log collection stack shipped as both a
  compose file (`logging/`) and K8s manifests.
- Database init scripts relocated to `docs/db/init-mysql.sql` and
  `docs/db/init-postgresql.sql` (30+ tables).

#### Observability

- Spring Boot Admin integration for live service health.
- Grafana dashboards under `logging/grafana/`.
- Loki log aggregation with Promtail / Alloy pipeline.

### Changed

- ORM migrated from **MyBatis Plus** to **MyBatis-Flex** across new code;
  legacy MP bindings removed from example controllers and services.
- Build upgraded to **Spring Boot 4.0** and **JDK 25**; old Java 17
  compatibility paths removed.
- Default database switched to **PostgreSQL 16**; MySQL 8.0 still
  first-class supported via alternate init script.
- Response envelope (`ApiResult`) standardized to include `timestamp`
  (epoch millis) on every payload.

### Fixed

- Test compilation errors under Java 25 (resolved via `--enable-preview`
  cleanup and removal of reflective access).
- MyBatis-Flex mapper scanning now honors `xarch.db.scan-packages`
  property (was hardcoded previously).
- Gateway route reload no longer drops in-flight requests on Nacos
  config refresh.
- XSS filter correctly handles `multipart/form-data` requests
  (previously could double-escape JSON bodies).

### Security

- All passwords hashed with **BCrypt** (strength 12).
- Sa-Token issued as JWT with configurable TTL and refresh policy.
- Default CORS allow-list is empty (must be opted in via
  `xarch.web.cors.allowed-origins`).
- Sensitive request headers (`Authorization`, `Cookie`) redacted in
  default access logs.

---

## Versioning Policy

- **MAJOR** — Breaking API or schema change (a migration guide is
  required).
- **MINOR** — New backward-compatible feature.
- **PATCH** — Backward-compatible bug fix or security patch.
- Pre-1.0 minor bumps may contain breaking changes; the rules above
  become strict starting from 1.0.0.

[Unreleased]: https://example.com/xarch/compare/v1.0.0...HEAD
[1.0.0]: https://example.com/xarch/releases/tag/v1.0.0