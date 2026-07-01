<div align="center">

# xarch

### AI-Enabled Enterprise Backend Framework

**Build production-grade admin & AI-native backends in minutes, not months.**

Spring Boot 4 · JDK 25 · MyBatis-Flex · Sa-Token · Nacos · MCP · Spring Cloud · Vue 3 · Element Plus · OpenTelemetry · Resilience4j

[🚀 快速开始](#-quick-start-5-minutes) · [📖 文档](docs/) · [🤝 贡献](CONTRIBUTING.md) · [🐛 报告 Bug](https://github.com/processcrash/xarch/issues/new/choose) · [💬 讨论](https://github.com/processcrash/xarch/discussions)

</div>

---

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![JDK](https://img.shields.io/badge/JDK-25-ED8B00?logo=openjdk)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Stars](https://img.shields.io/github/stars/processcrash/xarch?style=social)](https://github.com/processcrash/xarch/stargazers)
[![CI](https://github.com/processcrash/xarch/workflows/ci-backend.yml/badge.svg)](https://github.com/processcrash/xarch/actions)
[![Docker](https://img.shields.io/badge/docker-ready-2496ED?logo=docker)](https://ghcr.io/processcrash/xarch)
[![Helm](https://img.shields.io/badge/helm-published-0F1689?logo=helm)](deploy/helm/)

**English** · [简体中文](docs/i18n/README.zh-CN.md)

</div>

---

## ✨ Why xarch?

Most enterprise backends are either **too heavy** (slow to start, hard to extend) or **too light** (no auth, no audit, no observability). xarch sits in the sweet spot: a **modular Spring Boot starter platform** that gives you production-grade defaults without locking you in.

- 🧩 **8 pluggable starters** — bring only what you need (core, db, web, cache, tracing, resilience, storage, MCP)
- 🤖 **AI-First** — 4 native MCP servers (database, knowledge/RAG, filesystem, vector DB) plus a Spring AI integration path
- 🛡️ **Production security** — Sa-Token + JWT, BCrypt, XSS filter, rate limit, CSRF, audit log baked in
- 📊 **Observability by default** — OpenTelemetry tracing, Micrometer metrics, Prometheus + Grafana + Loki profiles
- 🧱 **Spring Cloud ready** — built-in Nacos discovery, Spring Cloud Gateway with per-route rate limit + circuit breaker
- 🚀 **Deployment everywhere** — docker-compose for dev, Kustomize for K8s base+overlays, full **Helm chart** with 6 sub-charts
- 🧪 **Quality gates** — JUnit 5, Mockito, AssertJ, Testcontainers, Playwright E2E, GitHub Actions CI
- 🌐 **i18n** — Vue-i18n with zh-CN + en-US, Element Plus localized components
- 📚 **Real sample apps** — full CMS / OA / CRM examples to copy patterns from
- 🧰 **AI Agent platform** — built-in SSH terminal, command audit + approval workflow, risk scoring (low/medium/high)

> ⭐ **If xarch helps your team, please star the repo** — it tells us what to prioritize next.

---

## 🚀 Quick Start (5 minutes)

### Option A: Docker (zero local setup)

```bash
git clone https://github.com/processcrash/xarch.git
cd xarch
docker compose up -d
# Wait ~30s, then:
open http://localhost:8080       # Admin UI  (admin / 123456)
open http://localhost:8848/nacos # Nacos     (nacos / nacos)
```

### Option B: Local dev (full stack)

```bash
# 1. Start infrastructure
docker compose up -d postgres redis nacos

# 2. Backend
cd backend
./gradlew :xarch-example:bootRun   # API on :8080

# 3. Frontend
cd ../vue3-admin
npm install && npm run dev         # UI on :5173
```

### Option C: Helm (production-grade K8s)

```bash
helm repo add xarch https://processcrash.github.io/xarch
helm install xarch xarch/xarch \
  --namespace xarch --create-namespace \
  --set global.imageTag=1.0.0
```

That's it. The admin UI is up, with **18 prebuilt modules** (users, roles, menus, depts, dicts, configs, files, messages, server monitor, cache monitor, jobs, online users, OAuth clients, audit, RAG, etc.) ready to use.

---

## 🏗️ Architecture

```
                        ┌──────────────────────┐
                        │   Browser / Mobile   │
                        └──────────┬───────────┘
                                   │ HTTPS
                        ┌──────────▼───────────┐
                        │  Spring Cloud        │  Gateway
                        │  Gateway             │  ─ rate limit
                        │  (port 9000)         │  ─ circuit breaker
                        └──────────┬───────────┘
                                   │
        ┌──────────┬───────────────┼───────────────┬──────────┐
        ▼          ▼               ▼               ▼          ▼
   ┌────────┐ ┌────────┐    ┌────────┐    ┌────────┐ ┌────────┐
   │ service│ │service │    │service │    │service │ │service │
   │  -auth │ │-system │    │  -file │    │  -ai   │ │-message│
   │  :9001 │ │  :9002 │    │  :9003 │    │  :9005 │ │  :9006 │
   └───┬────┘ └────┬───┘    └────┬───┘    └────┬───┘ └────┬───┘
       │           │             │              │          │
       └───────────┴─────────────┴──────────────┴──────────┘
                                   │ (Feign)
                        ┌──────────▼───────────┐
                        │  Nacos 3.2           │ Discovery + Config
                        │  + Config Center     │
                        └──────────┬───────────┘
                                   │
       ┌───────────┬───────────────┼───────────────┬────────────┐
       ▼           ▼               ▼               ▼            ▼
  PostgreSQL    Redis          Spring AI      MCP Servers   Observability
  MySQL         Redisson       (LLM bridge)   (4 servers)   (Prom+Grafana)
```

**Key design decisions** ([full rationale](docs/ARCHITECTURE.md)):

- **MyBatis-Flex over MyBatis-Plus** — 30% smaller footprint, no reflection, better Kotlin interop
- **Sa-Token over Spring Security** — 1/10 the config, native JWT, multi-tenant built in
- **JDK 25 features** — records, sealed types, virtual threads (Project Loom) for I/O
- **In-process vector store** in `xarch-mcp-vector` — swap for Qdrant/Milvus with one config change
- **MCP as integration boundary** — AI tools are first-class, not bolted on

---

## 📦 What's in the box

### 8 Spring Boot Starters (`com.xarch.starter.*`)

| Starter | What it gives you |
|---------|-------------------|
| **xarch-core** | `ApiResult`, `PageResult`, exceptions, ID generator, validators |
| **xarch-db** | MyBatis-Flex, multi-DB, audit fields, soft delete, multi-tenant interceptor |
| **xarch-web** | Sa-Token, XSS filter, rate limit, CORS, Knife4j, global exception handler |
| **xarch-cache** | Redis/Redisson auto-config, distributed locks, rate limit storage |
| **xarch-tracing** | OpenTelemetry SDK, W3C traceparent, MDC injection, OTLP/Zipkin exporters |
| **xarch-resilience** | Resilience4j annotations: `@RateLimit`, `@CircuitBreaker`, `@Retry`, `@Bulkhead` |
| **xarch-storage** | Unified `FileStorageService` for local / MinIO / Aliyun OSS / AWS S3 |
| **xarch-mcp-vector** | In-process vector store + KNN search + 10 MCP tools |

### 4 MCP Servers (Java + Node + Python)

| Server | Language | Tools |
|--------|----------|-------|
| **database-mcp** | Java + Node + Python | query, schema inspect, DDL, EXPLAIN |
| **knowledge-mcp** | Java + Node + Python | RAG index, semantic search, document CRUD |
| **filesystem-mcp** | Java + Node + Python | safe file ops, path-traversal protection |
| **vector-mcp** | Java + Node + Python | collection CRUD, KNN search, metadata filter |

> All three languages produce **semantically identical APIs** — pick whichever your team prefers. The Python and Node versions are great for hooking up to Claude Desktop, Cursor, or any MCP client.

### 3 Sample Business Apps (`backend/examples/`)

- **cms** — Content management (articles, categories, tags, comments)
- **oa** — Office automation (leave requests, expense reports, approval workflow)
- **crm** — Customer relationship (leads, opportunities, sales pipeline analytics)

Each ships with entities, services, controllers, DDL, OpenAPI annotations, and **unit tests** — copy-paste the patterns into your own domain.

### Microservices Split (`backend/xarch-example-micro/`)

Reference decomposition of the monolith into 7 Spring Cloud services:

| Service | Port | Responsibility |
|---------|------|----------------|
| service-auth | 9001 | Login, JWT, user mgmt |
| service-system | 9002 | RBAC, menus, depts, dicts, configs |
| service-file | 9003 | Upload/download, storage adapters |
| service-monitor | 9004 | Server metrics, cache stats, jobs |
| service-ai | 9005 | AI chat, RAG, MCP tool execution |
| service-message | 9006 | In-app messages, OAuth clients |
| common | — | Shared DTOs, constants, Feign fallbacks |

Includes **per-service Dockerfile** (multi-stage, non-root, healthcheck) and full **Helm chart**.

### Vue 3 Admin (`vue3-admin/`)

- 27 views, 24 API modules, full role-based menu
- **i18n** (zh-CN default, en-US, Element Plus localized)
- **Playwright E2E** suite (60+ test cases)
- TypeScript strict, ESLint, Prettier
- Pinia stores, route guards, dynamic permission loading

---

## 🛠️ Tech Stack

**Backend** — Java 25 · Spring Boot 4.0 · Spring Cloud 2025 · MyBatis-Flex 1.9 · MySQL 8 / PostgreSQL 16 / MongoDB · Redis 7 / Redisson · Sa-Token · Resilience4j 2.2 · OpenTelemetry 1.42 · Spring AI 1.0 · Knife4j 5

**MCP** — `@modelcontextprotocol/sdk` (Node) · `mcp[cli]` (Python) · raw JSON-RPC (Java)

**Frontend** — Vue 3.5 · Vite 6 · TypeScript 5 · Element Plus 2.8 · Pinia 2 · Axios · Vue Router 4 · Vue I18n 9

**DevOps** — Gradle 8 + Kotlin DSL · Docker multi-stage · Helm 3.16 · Kustomize · GitHub Actions (CI, security, release) · Prometheus + Grafana + Loki · Trivy + CodeQL + Gitleaks

---

## 📚 Documentation

| Doc | What it covers |
|-----|----------------|
| [📘 docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Design decisions, layer breakdown, data flow, performance |
| [📗 docs/MCP_GUIDE.md](docs/MCP_GUIDE.md) | MCP protocol, all 4 servers, adding a new tool |
| [📕 docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | Dev → staging → prod, K8s, Helm, monitoring, DR |
| [📙 docs/API.md](docs/API.md) | REST API reference, auth, pagination, error codes |
| [📔 docs/STORAGE.md](docs/STORAGE.md) | MinIO / Aliyun OSS / S3 adapters, presigned URLs, CDN |
| [📓 docs/RESILIENCE.md](docs/RESILIENCE.md) | Rate limit, circuit breaker, retry, bulkhead |
| [📒 docs/TRACING.md](docs/TRACING.md) | OpenTelemetry setup, sampling, MDC integration |
| [📕 docs/FAQ.md](docs/FAQ.md) | Common questions, troubleshooting |
| [🇨🇳 docs/i18n/README.zh-CN.md](docs/i18n/README.zh-CN.md) | 简体中文文档 |

---

## 🧪 Testing

```bash
# Unit + integration tests (89 test classes across starters + MCP)
cd backend && ./gradlew test

# E2E (60+ Playwright cases, headless)
cd vue3-admin && npm run test:e2e

# UI mode for debugging
npm run test:e2e:ui

# Helm chart lint + dry-run
./deploy/helm/scripts/lint.sh
```

Coverage report is uploaded to Codecov on every PR. See `.github/workflows/` for the full matrix.

---

## 🤝 Contributing

**We welcome contributions of any size** — typo fixes, doc improvements, new starters, new MCP servers, new sample apps.

1. ⭐ Star this repo (helps us prioritize)
2. 🍴 Fork & create a branch from `main`
3. 💻 Make your change + add a test
4. 📝 Run `pre-commit` (auto-fmt + lint)
5. 🚀 Open a PR — small PRs (<300 LOC) get reviewed fastest
6. ✅ Pass CI (build + test + lint + security + helm-lint)

See [CONTRIBUTING.md](CONTRIBUTING.md) for conventions and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community standards.

> **New to the codebase?** Look for issues labeled [`good first issue`](https://github.com/processcrash/xarch/issues?q=is%3Aopen+label%3A%22good+first+issue%22) — they're hand-picked for newcomers.

### Project Workflow (issue-first)

We follow a **"discuss before code"** model. Please don't open a PR for a non-trivial change without first creating an issue:

1. 🔍 **Search** existing issues to avoid duplicates
2. 💡 **Open an issue** using the appropriate template (bug report / feature request / question)
3. 💬 **Discuss** with maintainers — we may suggest a different approach or pair you with a reviewer early
4. ✅ **Get a green light** (label `status: ready-for-pr`)
5. 🚀 **Open the PR** referencing the issue (e.g. `Closes #123`)
6. 🔁 **Iterate** based on review

This avoids wasted effort and keeps the roadmap coherent. See [ISSUE_WORKFLOW.md](ISSUE_WORKFLOW.md) for the full triage playbook.

---

## 🗺️ Roadmap

| Quarter | Focus | Status |
|---------|-------|--------|
| Q1 2026 | Core framework + 4 MCP servers + 18 controllers + CI/CD | ✅ shipped |
| Q2 2026 | vue-i18n, Resilience4j, multi-cloud storage, OpenTelemetry, Playwright E2E, Helm chart | ✅ shipped |
| Q3 2026 | AI Agent platform enhancements, MCP Marketplace, plugin system, low-code generator | 🚧 in progress |
| Q4 2026 | Edge runtime (GraalVM native), WASM frontend, mobile (uni-app), GraphQL gateway | 📋 planned |

See the [open issues](https://github.com/processcrash/xarch/issues) for the live backlog and vote on what matters most with 👍 reactions.

---

## 📊 Project stats

- **~180k LOC** across Java / TypeScript / Python / Vue / Gradle / K8s / Helm
- **89** test classes in the backend, **60+** Playwright E2E cases
- **27** Vue views, **24** API modules, **35+** REST endpoints per micro-service
- **81** Helm chart files covering 6 services + observability + ingress
- **10** MCP tools per server, **3** language implementations (Java / Node / Python)

---

## 💬 Community

- 💡 **Discussions** — [github.com/processcrash/xarch/discussions](https://github.com/processcrash/xarch/discussions) for Q&A, ideas, show & tell
- 🐛 **Issues** — [github.com/processcrash/xarch/issues](https://github.com/processcrash/xarch/issues) for bugs and feature requests
- 🔒 **Security** — see [SECURITY.md](SECURITY.md) for private disclosure process
- 📜 **Changelog** — [CHANGELOG.md](CHANGELOG.md)
- 🌐 **i18n** — [docs/i18n/](docs/i18n/) (中文 README available)

---

## 📄 License

[MIT](LICENSE) © xarch contributors

---

<div align="center">

**Built with care by the xarch community** · ⭐ star us if this helped you build something

</div>
