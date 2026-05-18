# xarch 需求文档

## 概述

xarch 是 **AI 时代企业级后台管理项目规范**，基于 Spring Boot 4.0 + Spring Cloud + Vue 3 + MyBatis Plus 构建，为 AI 原生企业应用提供开箱即用的后台管理解决方案。

**核心定位：**
- **AI-First Architecture** - 内置 MCP (Model Context Protocol) 服务器，支持 AI 与企业系统的无缝连接
- **Enterprise-Grade** - 面向生产环境设计，提供完整的企业级功能：权限管理、操作审计、数据可视化
- **Spring Cloud Native** - 原生支持 Spring Cloud 微服务架构，Nacos 3.2 服务注册与发现
- **Modular Design** - 采用 Spring Boot Starter 架构，可按需引入，灵活组合
- **Convention Over Configuration** - 约定优于配置，极简开发体验

---

## 技术栈

### 后端

| 分类 | 技术 | 说明 |
|------|------|------|
| Runtime | Java 25 / Spring Boot 4.0 | 最新 LTS 版本 |
| Build | Gradle (Kotlin DSL) | 现代构建工具 |
| ORM | MyBatis Plus 3.5+ | 简化 CRUD 操作 |
| Database | PostgreSQL 16（默认）/ MySQL 8.0 / MongoDB / SQL Server | 多数据库支持 |
| Connection | Druid | 监控型连接池 |
| Cache | Redis 7 + Redisson | 分布式缓存与锁 |
| Auth | Sa-Token (JWT) | 无状态认证 |
| API Docs | Knife4j (Swagger 3.0) | API 文档生成 |
| Pagination | PageHelper | 分页插件 |
| Spring Cloud | Spring Cloud 2025.0.0.0 | 微服务架构 |
| Service Registry | Nacos 3.2 | 服务发现与配置 |
| API Gateway | Spring Cloud Gateway | 统一入口 |

### MCP Servers

| 分类 | 技术 | 说明 |
|------|------|------|
| Java MCP | Spring Boot + Nacos | Spring Cloud 集成 |
| Node.js MCP | TypeScript + @modelcontextprotocol/sdk | 独立部署 |
| Python MCP | Python 3.10+ + mcp SDK | 独立部署 |

### 前端

| 分类 | 技术 | 说明 |
|------|------|------|
| Framework | Vue 3.5 + Vite 6 | 现代化前端框架 |
| UI Library | Element Plus | 企业级组件库 |
| State | Pinia | 状态管理 |
| Language | TypeScript | 类型安全 |
| HTTP | Axios | HTTP 请求 |
| Terminal | xterm.js | Web SSH 终端 |

---

## 项目结构

```
xarch/
├── backend/                                    # Spring Boot 后端 (Gradle)
│   ├── xarch-spring-boot-starter/              # Spring Boot Starter 模块
│   │   ├── xarch-core-spring-boot-starter/    # 核心模块：工具类、注解、实体基类
│   │   ├── xarch-db-spring-boot-starter/      # 数据库模块：MyBatis Plus、Druid 连接池
│   │   ├── xarch-web-spring-boot-starter/     # Web 模块：REST API、Swagger、Sa-Token 认证
│   │   └── xarch-cache-spring-boot-starter/   # 缓存模块：Redis、Redisson 分布式锁
│   ├── xarch-spring-cloud/                     # Spring Cloud 微服务模块
│   │   └── xarch-cloud/
│   │       ├── xarch-cloud-starter-nacos/     # Nacos 服务注册（含 MCP 服务注册）
│   │       ├── xarch-cloud-starter-gateway/   # API Gateway 路由配置
│   │       └── xarch-cloud-starter-mcp/        # MCP 协议核心
│   └── xarch-example/                          # 示例应用
│
├── mcp-servers/                                 # Node.js MCP Servers (TypeScript)
│   ├── database-mcp/                          # 数据库 MCP Server
│   ├── knowledge-mcp/                         # 知识库 MCP Server (RAG)
│   └── filesystem-mcp/                         # 文件系统 MCP Server
│
├── python-mcp/                                 # Python MCP Servers
│   ├── database_mcp/                         # 数据库 MCP Server
│   ├── knowledge_mcp/                         # 知识库 MCP Server
│   └── filesystem_mcp/                        # 文件系统 MCP Server
│
├── vue3-admin/                                 # Vue 3 前端
├── k8s/                                         # Kubernetes 部署配置
├── docker-compose.yml                          # Docker 编排
└── init-postgresql.sql                         # PostgreSQL 初始化脚本
```

---

## 已实现功能模块

### 1. 系统管理

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 用户管理 | /api/users/* | CRUD、分页、角色分配 |
| 角色管理 | /api/roles/* | CRUD、菜单分配、数据权限 |
| 菜单管理 | /api/menus/* | 树形结构、权限标识 |
| 部门管理 | /api/depts/* | 组织架构树 |
| 岗位管理 | /api/posts/* | 职位设置 |
| 通知公告 | /api/notices/* | 信息发布 |

### 2. 系统配置

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 字典管理 | /api/dicts/* | 类型与数据 |
| 参数配置 | /api/configs/* | 系统参数 |

### 3. 日志管理

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 登录日志 | /monitor/logininfor/* | 访问记录 |
| 操作日志 | /monitor/operlog/* | 业务审计 |

### 4. 监控管理

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 服务器监控 | /monitor/server/* | CPU、内存、JVM |
| 缓存监控 | /monitor/cache/* | Redis 状态 |
| 在线用户 | /monitor/online/* | 会话管理 |
| 定时任务 | /monitor/job/* | 调度管理 |
| 任务日志 | /monitor/jobLog/* | 执行记录 |

### 5. 企业文件管理中心

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 文件管理 | /resource/* | 文件上传、下载、删除、预览 |
| 存储配置 | /storage/* | 本地存储、MinIO、阿里云 OSS |

**存储策略：**
- LocalStorageStrategy - 本地文件存储
- MinioStorageStrategy - MinIO 对象存储
- AliyunOssStorageStrategy - 阿里云 OSS

### 6. Linux 服务器管理 AI Agent 平台

| 模块 | 端点 | 功能说明 |
|------|------|---------|
| 服务器管理 | /ai/server/* | 服务器 CRUD、连接管理 |
| 命令执行 | /ai/server/command | 远程命令执行 |
| AI 命令生成 | /ai/server/ai/* | 自然语言转 Shell 命令 |
| 命令审计 | /ai/audit/* | 操作审计、审批工作流 |
| WebSocket 终端 | /ws/ssh | 实时 SSH 终端 |

**AI Agent 能力：**
- 自然语言到 Shell 命令转换（20+ 模式）
- 命令安全验证和风险评估
- 多步骤任务分解
- LLM 集成支持（Spring AI）

### 7. 可观测性平台

**Spring Boot Admin 监控：**
- 服务状态监控
- 应用信息查看
- 日志级别管理
- 线程 dump / Heap dump

**Alloy + Loki + Grafana 日志收集：**
- Docker 容器日志收集
- Docker Compose 日志收集
- Kubernetes Pod 日志收集
- 统一日志查询和可视化

### 8. 认证与安全

| 模块 | 功能说明 |
|------|---------|
| XSS 防护 | XssFilter + XssHttpServletRequestWrapper |
| API 限流 | RateLimitFilter - 基于令牌桶算法 |
| JWT 认证 | Sa-Token 无状态认证 |
| SQL 防护 | MyBatis Plus 参数绑定 |

---

## MCP Server 模块

### MCP (Model Context Protocol) 协议

MCP 是 AI 与企业系统连接的桥梁，xarch 提供三套 MCP Server 实现：

#### Java 实现（Spring Boot Starter）

适用于 Spring Cloud 微服务架构，与 Nacos 无缝集成。

#### Node.js + TypeScript 实现

独立运行，通过 stdio 与 AI 客户端通信，适用于跨语言场景。

#### Python 实现

独立运行，兼容 Python 生态，适用于数据科学和 ML 场景。

### 1. Database MCP Server

**支持的数据库：**
- PostgreSQL 16（默认）
- MySQL 8.0
- MongoDB
- Microsoft SQL Server

**MCP 工具：**
| 工具名称 | 功能说明 |
|----------|---------|
| `configure` | 配置数据库连接 |
| `query_execute` | 执行 SQL SELECT 查询 |
| `execute_update` | 执行 INSERT/UPDATE/DELETE |
| `schema_get` | 获取数据库架构 |
| `table_list` | 列出所有表 |
| `table_describe` | 描述表结构 |
| `health` | 健康检查 |

### 2. Knowledge Base MCP Server (RAG)

企业级知识库，支持 RAG (Retrieval Augmented Generation)。

**功能特性：**
- 语义搜索（基于向量相似度）
- 文档索引与管理
- 支持多种文件类型

**MCP 工具：**
| 工具名称 | 功能说明 |
|----------|---------|
| `kb_index_document` | 索引文档 |
| `kb_search` | 语义搜索 |
| `kb_get_document` | 获取文档 |
| `kb_delete` | 删除文档 |
| `kb_list` | 列出所有文档 |
| `kb_stats` | 知识库统计 |
| `health` | 健康检查 |

### 3. Filesystem MCP Server

安全的企业级文件系统操作（路径遍历防护）。

**安全特性：**
- 路径遍历防护
- 允许文件扩展名限制
- 隔离的工作目录

**MCP 工具：**
| 工具名称 | 功能说明 |
|----------|---------|
| `list_directory` | 列出目录内容 |
| `read_file` | 读取文件内容 |
| `write_file` | 写入文件内容 |
| `delete` | 删除文件或目录 |
| `create_directory` | 创建目录 |
| `search_files` | 搜索文件 |
| `get_file_info` | 获取文件信息 |
| `health` | 健康检查 |

---

## Nacos MCP 服务注册

Java 实现的 MCP Server 可注册为 Nacos 服务，实现服务发现：

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: xarch-cloud
        group: MCP
```

---

## API 规范

### 统一响应格式

```json
{
  "code": "0000",
  "msg": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

### 响应码定义

| 响应码 | 说明 |
|--------|------|
| 0000 | 成功 |
| 1001 | 参数错误 |
| 1002 | 业务异常 |
| 1003 | 认证失败 |
| 1004 | 资源未找到 |
| 1005 | 系统错误 |

---

## 数据库表（30+ 张）

```
# 基础模块
sys_user           # 用户表
sys_role           # 角色表
sys_menu           # 菜单表（权限）
sys_dept           # 部门表
sys_post           # 岗位表

# 关联表
sys_user_role      # 用户-角色关联
sys_role_menu      # 角色-菜单关联
sys_role_dept      # 角色-部门关联
sys_user_post      # 用户-岗位关联

# 配置模块
sys_dict_type      # 字典类型
sys_dict_data      # 字典数据
sys_config         # 参数配置
sys_notice         # 通知公告

# 日志模块
sys_oper_log       # 操作日志
sys_logininfor     # 登录日志

# 任务模块
sys_job            # 定时任务
sys_job_log        # 任务日志

# 文件存储模块
sys_resource       # 文件资源表
sys_storage_config # 存储配置表

# AI 服务器管理模块
ai_server          # AI 服务器表
ai_command_history # 命令历史表
ai_command_session # 会话管理表
ai_command_audit   # 命令审计表
```

---

## 配置规范

### PostgreSQL 配置（默认）

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/xarch
    username: postgres
    password: postgres
```

### Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
```

### Nacos 配置

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: xarch-cloud
        group: DEFAULT_GROUP
```

---

## 部署要求

### Docker 部署
- 支持 Docker 容器化部署
- 支持 docker-compose 一键启动
- 前端 Nginx 反向代理到后端
- 数据库初始化脚本即开即用

### Kubernetes 部署
- 支持 K8s 部署，提供完整的 Deployment/Service/Ingress 配置
- 支持 dev/prod 环境分离（Kustomize overlays）
- 支持 HPA 自动扩缩容
- 支持 NFS/云存储持久化

### 可观测性部署
- Alloy 日志收集代理
- Loki 日志存储
- Grafana 可视化面板
- 支持 Docker/Docker Compose/K8s 日志收集

---

## 开发规范

### 包命名规范

```
com.xarch.starter.*     # 框架 Starter 模块
com.xarch.cloud.*       # Spring Cloud 模块
com.xarch.mcp.*         # MCP Server 模块
com.xarch.example.*     # 业务应用模块
```

### 分层架构

```
controller/   # REST 控制器层
service/      # 业务服务层（接口 + 实现）
mapper/       # 数据访问层（MyBatis）
entity/       # 领域实体层
dto/          # 数据传输对象
vo/           # 视图对象
```
