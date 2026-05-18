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
│       ├── postgresql.yaml                    # PostgreSQL 数据库
│       └── ...
│
├── docker-compose.yml                          # Docker 编排
├── init.sql                                    # MySQL 初始化脚本
└── init-postgresql.sql                          # PostgreSQL 初始化脚本
```

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

## Spring Cloud + Nacos 3.2 架构

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (Spring Cloud Gateway)     │
│                         Port: 8080                          │
└─────────────────────────────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Nacos 3.2 │       │  MCP Server │       │  Business   │
│  (Registry  │       │  Registry   │       │  Services   │
│  + Config)  │       │             │       │             │
│   Port:8848 │       │             │       │             │
└─────────────┘       └─────────────┘       └─────────────┘
         │                    │                    │
         │            ┌──────┴──────┐            │
         │            ▼             ▼            ▼
         │     ┌───────────┐  ┌───────────┐  ┌───────────┐
         │     │ Database  │  │ Knowledge │  │ Filesystem│
         │     │   MCP     │  │    MCP    │  │    MCP    │
         │     │ Server    │  │ Server    │  │ Server    │
         │     └───────────┘  └───────────┘  └───────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Supported Databases                      │
│  MySQL │ PostgreSQL │ MongoDB │ Redis │ SQL Server        │
└─────────────────────────────────────────────────────────────┘
```

---

## MCP Server 模块

### MCP (Model Context Protocol) 服务

MCP Server 是 AI 与企业系统连接的桥梁，xarch 提供三个开箱即用的 MCP Server：

### 1. Database MCP Server (`xarch-mcp-database`)

**支持的数据库：**
- MySQL
- PostgreSQL
- MongoDB
- Microsoft SQL Server

**提供的工具：**
| 工具名称 | 功能说明 |
|----------|---------|
| `query_execute` | 执行 SQL 查询 |
| `schema_get` | 获取数据库架构 |
| `table_list` | 列出所有表 |
| `table_describe` | 描述表结构 |
| `index_list` | 列出索引 |
| `execute_update` | 执行 INSERT/UPDATE/DELETE |

**端点：** `POST /mcp/database/tools/{tool_name}`

### 2. Knowledge Base MCP Server (`xarch-mcp-knowledge`)

企业级知识库，支持 RAG (Retrieval Augmented Generation)。

**提供的工具：**
| 工具名称 | 功能说明 |
|----------|---------|
| `kb_index_document` | 索引文档 |
| `kb_index_file` | 索引文件（支持 PDF、Markdown、TXT） |
| `kb_search` | 语义搜索 |
| `kb_get_document` | 获取文档 |
| `kb_delete` | 删除文档 |
| `kb_list` | 列出所有文档 |

**端点：** `POST /mcp/knowledge/tools/{tool_name}`

### 3. Filesystem MCP Server (`xarch-mcp-filesystem`)

安全的企业级文件系统操作。

**提供的工具：**
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

**端点：** `POST /mcp/filesystem/tools/{tool_name}`

---

## Node.js MCP Servers (TypeScript)

除了 Java 实现的 MCP Server，xarch 还提供 Node.js + TypeScript 实现的 MCP Server，具有完整的 MCP 协议支持：

### 1. Database MCP Server (`mcp-servers/database-mcp`)

**技术栈：** Node.js + TypeScript + @modelcontextprotocol/sdk

**支持的数据库：**
- MySQL
- PostgreSQL（默认）
- MongoDB
- Microsoft SQL Server

**MCP 工具：** query_execute, schema_get, table_list, table_describe, index_list, execute_update, configure, health

**启动：** `npm install && npm run dev`

### 2. Knowledge Base MCP Server (`mcp-servers/knowledge-mcp`)

**技术栈：** Node.js + TypeScript + @modelcontextprotocol/sdk

**功能：** 企业级 RAG 知识库，支持语义搜索

**MCP 工具：** kb_index_document, kb_index_file, kb_search, kb_get_document, kb_delete, kb_list, kb_update, kb_stats, health

**启动：** `npm install && npm run dev`

### 3. Filesystem MCP Server (`mcp-servers/filesystem-mcp`)

**技术栈：** Node.js + TypeScript + @modelcontextprotocol/sdk

**功能：** 安全文件操作（路径遍历防护）

**MCP 工具：** list_directory, read_file, write_file, delete, create_directory, search_files, get_file_info, copy_file, move_file, health

**启动：** `npm install && npm run dev`

---

## Python MCP Servers

xarch 还提供 Python 实现的 MCP Server，使用标准库实现，可直接运行无需额外依赖：

### 1. Database MCP Server (`mcp-servers/python/database_mcp`)

**支持的数据库：**
- MySQL
- PostgreSQL
- MongoDB
- Microsoft SQL Server

**MCP 工具：** configure, query_execute, execute_update, schema_get, table_list, table_describe, index_list, health

**启动：**

```bash
cd mcp-servers/python
python -m database_mcp
```

**可选依赖：**
```bash
pip install mysql-connector-python  # MySQL 支持
pip install psycopg2-binary        # PostgreSQL 支持
pip install pymongo                 # MongoDB 支持
pip install pymssql                # SQL Server 支持
```

### 2. Knowledge Base MCP Server (`mcp-servers/python/knowledge_mcp`)

**功能：** 企业级 RAG 知识库，使用 TF-IDF 算法实现语义搜索

**MCP 工具：** kb_index_document, kb_index_file, kb_search, kb_get_document, kb_delete, kb_list, kb_update, kb_stats, health

**启动：**

```bash
cd mcp-servers/python
python -m knowledge_mcp
```

### 3. Filesystem MCP Server (`mcp-servers/python/filesystem_mcp`)

**功能：** 安全文件操作，支持路径遍历防护

**MCP 工具：** list_directory, read_file, write_file, delete, create_directory, search_files, get_file_info, copy_file, move_file, health

**启动：**

```bash
cd mcp-servers/python
python -m filesystem_mcp
```

---

## Nacos MCP 服务注册

MCP Server 可注册为 Nacos 服务，实现服务发现：

```yaml
# application.yml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: xarch-cloud
        group: MCP

xarch:
  mcp:
    nacos:
      enabled: true
      service-name: xarch-mcp-database
      service-type: database
```

使用 `@McpServer` 注解自动注册：

```java
@McpServer(
    name = "xarch-mcp-knowledge",
    type = "knowledge-base",
    port = 9091,
    capabilities = {"vector-search", "rag", "document-processing"}
)
public class KnowledgeMcpController {
    // ...
}
```

---

## 快速开始

### 后端启动

```bash
cd backend
./gradlew build -x test
cd xarch-example
./gradlew bootRun

# API 文档：http://localhost:8080/doc.html
```

### 前端启动

```bash
cd vue3-admin
pnpm install
pnpm dev

# 访问地址：http://localhost:3000
```

### Docker 部署

```bash
docker-compose up -d

# 访问点：
# - 前端：http://localhost
# - 后端 API：http://localhost/api
# - API 文档：http://localhost:8080/doc.html
```

---

## 使用 Starter

在项目中添加依赖：

```kotlin
// build.gradle.kts
dependencies {
    // 基础 Starter
    implementation("com.xarch:xarch-db-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-web-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-cache-spring-boot-starter:1.0.0")

    // Spring Cloud + Nacos
    implementation("com.xarch:xarch-cloud-starter-nacos:1.0.0")
    implementation("com.xarch:xarch-cloud-starter-gateway:1.0.0")

    // MCP Servers
    implementation("com.xarch:xarch-mcp-database:1.0.0")
    implementation("com.xarch:xarch-mcp-knowledge:1.0.0")
    implementation("com.xarch:xarch-mcp-filesystem:1.0.0")
}
```

---

## 功能模块（22 个控制器）

### 系统管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `UserController` | `/system/user/*` | 用户管理：CRUD、角色分配 |
| `RoleController` | `/system/role/*` | 角色管理：权限分配、数据范围 |
| `MenuController` | `/system/menu/*` | 菜单管理：树形结构、权限标识 |
| `DeptController` | `/system/dept/*` | 部门管理：组织架构树 |
| `PostController` | `/system/post/*` | 岗位管理：职位设置 |
| `NoticeController` | `/system/notice/*` | 通知公告：信息发布 |

### 系统配置

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `DictController` | `/system/dict/*` | 字典管理：类型与数据 |
| `ConfigController` | `/system/config/*` | 参数配置：系统参数 |

### 日志管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `LoginLogController` | `/monitor/logininfor/*` | 登录日志：访问记录 |
| `OpLogController` | `/monitor/operlog/*` | 操作日志：业务审计 |

### 监控管理

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `SysServerController` | `/monitor/server/*` | 服务器监控：CPU、内存、JVM |
| `SysCacheController` | `/monitor/cache/*` | 缓存监控：Redis 状态 |
| `SysUserOnlineController` | `/monitor/online/*` | 在线用户：会话管理 |
| `SysJobController` | `/monitor/job/*` | 定时任务：调度管理 |
| `SysJobLogController` | `/monitor/jobLog/*` | 任务日志：执行记录 |

### 业务模块

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `CaptchaController` | `/captcha/*` | 验证码：图形验证 |
| `ClientController` | `/client/*` | 客户端管理：OAuth 客户端 |
| `MessageController` | `/message/*` | 消息中心：站内消息 |
| `ResourceController` | `/resource/*` | 资源管理：文件资源 |
| `TempFileController` | `/tempFile/*` | 临时文件：上传管理 |
| `CommonController` | `/common/*` | 通用操作：文件上传下载 |

### Excel 模块

| 控制器 | 端点 | 功能说明 |
|--------|------|---------|
| `ExcelController` | `/excel/*` | Excel 导入导出 |

---

## API 响应格式

统一响应结构 `ApiResult<T>`：

```json
{
  "code": "0000",
  "msg": "success",
  "data": { ... },
  "timestamp": 1716038400000
}
```

**响应码规范：**

| 响应码 | 说明 |
|--------|------|
| `0000` | 成功 |
| `1001` | 参数错误 |
| `1002` | 业务异常 |
| `1003` | 认证失败 |
| `1004` | 资源未找到 |
| `1005` | 系统错误 |

---

## 配置参考

```yaml
server:
  port: 8080

spring:
  application:
    name: xarch-example

  # Nacos 服务注册
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: xarch-cloud
        group: DEFAULT_GROUP

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123

  # Redis 配置
  redis:
    host: localhost
    port: 6379
    database: 0

# Sa-Token 配置
sa-token:
  token-name: Authorization
  timeout: 7200
  activity-timeout: -1

# MyBatis Plus
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.xarch.example.entity

# Knife4j
knife4j:
  enable: true
  production: false
```

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

---

## 单元测试

25+ 个控制器全部配置单元测试：

```bash
cd backend
./gradlew test

# 测试覆盖率
- SysUserControllerTest     # 用户管理测试
- SysRoleControllerTest      # 角色管理测试
- SysMenuControllerTest     # 菜单管理测试
- SysDeptControllerTest     # 部门管理测试
- SysPostControllerTest     # 岗位管理测试
- SysNoticeControllerTest    # 通知公告测试
- SysConfigControllerTest    # 参数配置测试
- SysJobControllerTest       # 定时任务测试
- SysJobLogControllerTest    # 任务日志测试
- SysServerControllerTest    # 服务器监控测试
- SysCacheControllerTest     # 缓存监控测试
- SysUserOnlineControllerTest # 在线用户测试
- DatabaseMcpControllerTest  # 数据库 MCP 测试
- KnowledgeMcpControllerTest # 知识库 MCP 测试
- FilesystemMcpControllerTest # 文件系统 MCP 测试
... 共 25+ 个测试类
```

---

## 开发规范

### 包命名规范

```
com.xarch.starter.*   # 框架 Starter 模块
com.xarch.cloud.*     # Spring Cloud 模块
com.xarch.mcp.*       # MCP Server 模块
com.xarch.example.*   # 业务应用模块
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
| Controller | `XxxController` | `UserController` |
| Service 接口 | `IXxxService` | `IUserService` |
| Service 实现 | `XxxServiceImpl` | `UserServiceImpl` |
| Mapper | `XxxMapper` | `UserMapper` |
| Entity | `Xxx` | `User` |
| REST API | `*Controller` | `/user`, `/role` |

---

## License

MIT License - 自由使用，商用免费

---

**xarch - 让企业后台开发更简单，让 AI 集成更容易，让微服务治理更轻松。**