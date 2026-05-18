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

### Node.js MCP Servers

| 分类 | 技术 | 说明 |
|------|------|------|
| Runtime | Node.js 20+ | JavaScript 运行时 |
| Language | TypeScript 5.3 | 类型安全 |
| SDK | @modelcontextprotocol/sdk | MCP 协议实现 |
| Database Clients | mysql2, pg, mongodb, mssql | 多数据库支持 |

### 前端

| 分类 | 技术 | 说明 |
|------|------|------|
| Framework | Vue 3.5 + Vite 6 | 现代化前端框架 |
| UI Library | Element Plus | 企业级组件库 |
| State | Pinia | 状态管理 |
| Language | TypeScript | 类型安全 |
| HTTP | Axios | HTTP 请求 |

---

## 项目结构

```
xarch/
├── backend/                                    # Spring Boot 后端 (Gradle)
│   ├── xarch-spring-boot-starter/              # Spring Boot Starter 模块
│   │   ├── xarch-core-spring-boot-starter/    # 核心模块：工具类、注解、实体基类
│   │   ├── xarch-db-spring-boot-starter/      # 数据库模块：MyBatis Plus、Druid 连接池
│   │   ├── xarch-web-spring-boot-starter/     # Web 模块：REST API、Swagger、Sa-Token 认证
│   │   ├── xarch-cache-spring-boot-starter/   # 缓存模块：Redis、Redisson 分布式锁
│   │   └── xarch-mcp/                          # MCP Servers 模块（Java 实现）
│   │       ├── xarch-mcp-database/
│   │       ├── xarch-mcp-knowledge/
│   │       └── xarch-mcp-filesystem/
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
├── k8s/                                         # Kubernetes 部署配置
│   ├── base/                                  # 基础配置
│   └── overlays/                              # 环境覆盖配置
├── docker-compose.yml                          # Docker 编排
├── init.sql                                    # MySQL 初始化脚本
└── init-postgresql.sql                          # PostgreSQL 初始化脚本
```

---

## 模块设计

### 后端 Starter 模块

遵循 Spring Boot Starter 规范，引入即用：

| 模块 | 描述 |
|------|------|
| xarch-core-spring-boot-starter | 核心模块：ApiResult, ResultCode, Exception, Utils, 注解 |
| xarch-db-spring-boot-starter | 数据库模块：MyBatis Plus, Druid, PageHelper |
| xarch-web-spring-boot-starter | Web 模块：REST API, Knife4j, Sa-Token, Exception Handler |
| xarch-cache-spring-boot-starter | 缓存模块：Redis, Redisson |

### Spring Cloud 模块

| 模块 | 描述 |
|------|------|
| xarch-cloud-starter-nacos | Nacos 3.2 服务注册，MCP 服务自动注册 |
| xarch-cloud-starter-gateway | Spring Cloud Gateway 路由配置 |
| xarch-cloud-starter-mcp | MCP 协议核心定义 |

### Node.js MCP Servers (TypeScript)

使用 `@modelcontextprotocol/sdk` 实现完整的 MCP 协议：

| 模块 | 描述 | 协议能力 |
|------|------|----------|
| database-mcp | 多数据库 MCP Server | tools, resources, prompts |
| knowledge-mcp | RAG 知识库 MCP Server | tools, resources, prompts |
| filesystem-mcp | 安全文件系统 MCP Server | tools, resources, prompts |

---

## MCP Server 模块

### MCP (Model Context Protocol) 协议

MCP 是 AI 与企业系统连接的桥梁，xarch 提供三套 MCP Server 实现：

#### Java 实现（Spring Boot Starter）

适用于 Spring Cloud 微服务架构，与 Nacos 无缝集成。

#### Node.js + TypeScript 实现

独立运行，通过 stdio 与 AI 客户端通信，适用于跨语言场景。

### 1. Database MCP Server

**支持的数据库：**
- PostgreSQL 16（默认）
- MySQL 8.0
- MongoDB
- Microsoft SQL Server

**MCP 工具（Node.js 版本）：**
| 工具名称 | 功能说明 |
|----------|---------|
| `configure` | 配置数据库连接 |
| `query_execute` | 执行 SQL SELECT 查询 |
| `execute_update` | 执行 INSERT/UPDATE/DELETE |
| `schema_get` | 获取数据库架构 |
| `table_list` | 列出所有表 |
| `table_describe` | 描述表结构 |
| `index_list` | 列出索引 |
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
| `kb_index_file` | 索引文件（PDF、Markdown、TXT） |
| `kb_search` | 语义搜索 |
| `kb_get_document` | 获取文档 |
| `kb_delete` | 删除文档 |
| `kb_list` | 列出所有文档 |
| `kb_update` | 更新文档 |
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
| `copy_file` | 复制文件 |
| `move_file` | 移动文件 |
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

使用 `@McpServer` 注解自动注册：

```java
@McpServer(
    name = "xarch-mcp-knowledge",
    type = "knowledge-base",
    port = 9091,
    capabilities = {"vector-search", "rag", "document-processing"}
)
public class KnowledgeMcpController { }
```

---

## 功能模块（23 个控制器）

### 系统管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| UserController | /api/users/* | 用户管理：CRUD、分页、角色分配 |
| RoleController | /api/roles/* | 角色管理：CRUD、菜单分配、数据权限 |
| MenuController | /api/menus/* | 菜单管理：树形结构、权限标识 |
| DeptController | /api/depts/* | 部门管理：组织架构树 |
| PostController | /api/posts/* | 岗位管理：职位设置 |
| NoticeController | /api/notices/* | 通知公告：信息发布 |

### 系统配置

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| DictController | /api/dicts/* | 字典管理：类型与数据 |
| ConfigController | /api/configs/* | 参数配置：系统参数 |

### 日志管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| LoginLogController | /monitor/logininfor/* | 登录日志：访问记录 |
| OpLogController | /monitor/operlog/* | 操作日志：业务审计 |

### 监控管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| SysServerController | /monitor/server/* | 服务器监控：CPU、内存、JVM |
| SysCacheController | /monitor/cache/* | 缓存监控：Redis 状态 |
| SysUserOnlineController | /monitor/online/* | 在线用户：会话管理 |
| SysJobController | /monitor/job/* | 定时任务：调度管理 |
| SysJobLogController | /monitor/jobLog/* | 任务日志：执行记录 |

### 业务模块

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| CaptchaController | /captcha/* | 验证码：图形验证 |
| ClientController | /client/* | 客户端管理：OAuth 客户端 |
| MessageController | /message/* | 消息中心：站内消息 |
| ResourceController | /resource/* | 资源管理：文件资源 |
| TempFileController | /tempFile/* | 临时文件：上传管理 |
| CommonController | /common/* | 通用操作：文件上传下载 |
| ExcelController | /api/excel/* | Excel 导入导出 |

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

### 命名约定

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | XxxController | UserController |
| Service 接口 | IXxxService | IUserService |
| Service 实现 | XxxServiceImpl | UserServiceImpl |
| Mapper | XxxMapper | UserMapper |
| Entity | Xxx | User |

---

## 数据库表（20+ 张）

```
# 基础模块
sys_user         # 用户表
sys_role         # 角色表
sys_menu         # 菜单表（权限）
sys_dept         # 部门表
sys_post         # 岗位表

# 关联表
sys_user_role    # 用户-角色关联
sys_role_menu    # 角色-菜单关联
sys_role_dept    # 角色-部门关联
sys_user_post    # 用户-岗位关联

# 配置模块
sys_dict_type    # 字典类型
sys_dict_data    # 字典数据
sys_config       # 参数配置
sys_notice       # 通知公告

# 日志模块
sys_oper_log     # 操作日志
sys_logininfor   # 登录日志

# 任务模块
sys_job          # 定时任务
sys_job_log      # 任务日志
```