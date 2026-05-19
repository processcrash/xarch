# xarch - AI-Enabled Enterprise Backend Framework

> xarch 是 AI 时代企业级后台管理项目规范，基于 Spring Boot 4.0 + Spring Cloud + Vue 3 + MyBatis Plus 构建，为 AI 原生企业应用提供开箱即用的后台管理解决方案。

---

## 核心定位

xarch 不仅是一个框架，更是一套 **AI 时代企业后台管理的标准规范**：

- **AI-First Architecture** - 内置 AI 能力集成接口，支持智能辅助、内容生成、语义理解等 AI 功能
- **Enterprise-Grade** - 面向生产环境设计，提供完整的企业级功能：权限管理、操作审计、数据可视化
- **Spring Cloud Native** - 原生支持 Spring Cloud 微服务架构，Nacos 3.2 服务注册与发现
- **MCP Server 集成** - 内置 MCP (Model Context Protocol) 服务器，支持 AI 与企业系统的无缝连接
- **Modular Design** - 采用 Spring Boot Starter 架构，可按需引入，灵活组合
- **Convention Over Configuration** - 约定优于配置，极简开发体验

---

## 为什么选择 xarch？

| 特性 | xarch | 传统方案 |
|------|-------|---------|
| AI 能力集成 | 内置 MCP Server 接口，开箱即用 | 需自行集成，复杂度高 |
| Spring Cloud | 原生支持 Nacos 3.2 服务注册 | 无原生支持 |
| MCP 协议 | 支持数据库、知识库、文件系统 MCP | 不支持 |
| 开发效率 | Starter 按需引入，5 分钟启动 | 搭建繁琐，重复造轮子 |
| 代码规范 | 统一分包、命名、架构规范 | 无统一标准，质量参差 |
| 可维护性 | 分层清晰，模块解耦 | 容易形成巨石应用 |
| 测试覆盖 | 100% 控制器单元测试 | 缺乏测试，回归风险高 |

---

## 技术栈

### Backend

| 分类 | 技术 | 说明 |
|------|------|------|
| **Runtime** | Java 25 / Spring Boot 4.0 | 最新 LTS 版本 |
| **Build** | Gradle (Kotlin DSL) | 现代构建工具 |
| **ORM** | MyBatis Plus 3.5+ | 简化 CRUD 操作 |
| **Database** | PostgreSQL 16 / MySQL 8.0 / MongoDB / SQL Server | 多数据库支持（默认 PostgreSQL） |
| **Connection** | Druid | 监控型连接池 |
| **Cache** | Redis 7 + Redisson | 分布式缓存与锁 |
| **Auth** | Sa-Token (JWT) | 无状态认证 |
| **API Docs** | Knife4j (Swagger 3.0) | API 文档生成 |
| **Pagination** | PageHelper | 分页插件 |
| **Spring Cloud** | Spring Cloud 2025.0.0.0 | 微服务架构 |
| **Service Registry** | Nacos 3.2 | 服务发现与配置 |
| **API Gateway** | Spring Cloud Gateway | 统一入口 |

### Frontend

| 分类 | 技术 | 说明 |
|------|------|------|
| **Framework** | Vue 3.5 + Vite 6 | 现代化前端框架 |
| **UI Library** | Element Plus | 企业级组件库 |
| **State** | Pinia | 状态管理 |
| **Language** | TypeScript | 类型安全 |
| **HTTP** | Axios | HTTP 请求 |

---

## 项目结构

```
xarch/
├── backend/                                    # Spring Boot 后端 (Gradle)
│   ├── xarch-spring-boot-starter/              # Spring Boot Starter 模块
│   │   ├── xarch-core-spring-boot-starter/    # 核心模块：工具类、注解、实体基类
│   │   ├── xarch-db-spring-boot-starter/      # 数据库模块：MyBatis Plus、Druid 连接池
│   │   ├── xarch-web-spring-boot-starter/     # Web 模块：REST API、Swagger、Sa-Token 认证
│   │   ├── xarch-cache-spring-boot-starter/    # 缓存模块：Redis、Redisson 分布式锁
│   │   └── xarch-mcp/                          # MCP Servers 模块（Java）
│   │       ├── xarch-mcp-database/             # 数据库 MCP Server
│   │       ├── xarch-mcp-knowledge/            # 知识库 MCP Server (RAG)
│   │       └── xarch-mcp-filesystem/           # 文件系统 MCP Server
│   ├── xarch-spring-cloud/                     # Spring Cloud 微服务模块
│   │   └── xarch-cloud/
│   │       ├── xarch-cloud-starter-nacos/     # Nacos 服务注册（含 MCP 服务注册）
│   │       ├── xarch-cloud-starter-gateway/   # API Gateway 路由配置
│   │       └── xarch-cloud-starter-mcp/        # MCP 协议核心
│   └── xarch-example/                          # 示例应用（23 个控制器）
│
├── mcp-servers/                                 # Node.js MCP Servers (TypeScript)
│   ├── database-mcp/                          # 数据库 MCP Server
│   ├── knowledge-mcp/                         # 知识库 MCP Server (RAG)
│   └── filesystem-mcp/                         # 文件系统 MCP Server
│
├── vue3-admin/                                 # Vue 3 前端
│   ├── src/
│   │   ├── api/                               # API 调用层
│   │   ├── views/                             # 页面组件
│   │   ├── router/                            # 路由配置
│   │   ├── stores/                            # Pinia 状态管理
│   │   └── utils/                             # 工具函数
│   ├── nginx/                                 # Nginx 配置
│   └── Dockerfile                             # 容器化部署
│
├── k8s/                                         # Kubernetes 部署配置
│   └── base/
│       └── postgresql.yaml                    # PostgreSQL 数据库
│
├── docker-compose.yml                          # Docker 编排
├── init.sql                                    # MySQL 初始化脚本
└── init-postgresql.sql                          # PostgreSQL 初始化脚本
```

---

## 快速导航

| 文档 | 说明 |
|------|------|
| [安装指南](docs/wiki/INSTALL.md) | 环境准备与项目安装 |
| [架构设计](docs/wiki/ARCHITECTURE.md) | 系统架构与模块设计 |
| [API 参考](docs/wiki/API.md) | 接口文档与调用示例 |
| [部署手册](docs/wiki/DEPLOYMENT.md) | 生产环境部署 |
| [开发规范](docs/wiki/DEVELOPMENT.md) | 代码规范与开发指南 |
| [MCP 服务器](docs/wiki/MCP.md) | MCP Server 使用说明 |

---

## License

MIT License - 自由使用，商用免费