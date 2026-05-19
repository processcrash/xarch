# 架构设计

## 系统架构图

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

## 技术选型

| 层级 | 技术 | 说明 |
|------|------|------|
| **接入层** | Spring Cloud Gateway | 统一 API 入口，路由转发 |
| **服务注册** | Nacos 3.2 | 服务发现与配置管理 |
| **业务层** | Spring Boot 4.0 | 微服务业务逻辑 |
| **数据层** | MyBatis Plus + Druid | ORM 与连接池 |
| **缓存层** | Redis + Redisson | 分布式缓存与锁 |
| **认证层** | Sa-Token (JWT) | 无状态身份认证 |
| **AI 层** | MCP Servers | AI 能力抽象 |
| **前端** | Vue 3 + Element Plus | 响应式管理界面 |

---

## 模块架构

### Spring Boot Starter 模块

```
xarch-spring-boot-starter/
├── xarch-core-spring-boot-starter/     # 核心基础
│   ├── 工具类 (Utils)
│   ├── 通用注解 (Annotations)
│   └── 实体基类 (BaseEntity)
│
├── xarch-db-spring-boot-starter/       # 数据访问
│   ├── MyBatis Plus 配置
│   ├── Druid 连接池
│   └── 分页插件
│
├── xarch-web-spring-boot-starter/       # Web 层
│   ├── REST API 基类
│   ├── Swagger/Knife4j 文档
│   ├── Sa-Token 认证
│   └── XSS 过滤
│
├── xarch-cache-spring-boot-starter/     # 缓存
│   ├── Redis 配置
│   └── Redisson 分布式锁
│
└── xarch-mcp/                          # AI 能力
    ├── xarch-mcp-database/             # 数据库 MCP
    ├── xarch-mcp-knowledge/            # 知识库 MCP
    └── xarch-mcp-filesystem/           # 文件系统 MCP
```

### Spring Cloud 模块

```
xarch-spring-cloud/
└── xarch-cloud/
    ├── xarch-cloud-starter-nacos/      # Nacos 服务注册
    ├── xarch-cloud-starter-gateway/    # API Gateway
    └── xarch-cloud-starter-mcp/        # MCP 协议
```

---

## MCP Server 架构

MCP (Model Context Protocol) 是 AI 与企业系统连接的桥梁：

```
        ┌─────────────┐
        │   AI Model  │
        │  (Claude)  │
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │  MCP Client │
        └──────┬──────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐ ┌───▼───┐ ┌───▼───┐
│  DB   │ │  KB   │ │  FS   │
│  MCP  │ │  MCP  │ │  MCP  │
└───────┘ └───────┘ └───────┘
```

### Database MCP

提供数据库操作工具：
- `query_execute` - 执行 SQL 查询
- `schema_get` - 获取数据库架构
- `table_list` - 列出所有表
- `execute_update` - 执行写操作

### Knowledge Base MCP

提供知识库操作工具：
- `kb_index_document` - 索引文档
- `kb_search` - 语义搜索
- `kb_delete` - 删除文档

### Filesystem MCP

提供文件系统操作工具：
- `list_directory` - 列出目录
- `read_file` - 读取文件
- `write_file` - 写入文件
- `search_files` - 搜索文件

---

## 数据流

```
用户请求 → API Gateway → Nacos (服务发现)
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
              Business Service      MCP Server
                    │                   │
                    ▼                   ▼
              MyBatis Plus         AI Tools
                    │                   │
                    ▼                   ▼
              PostgreSQL            RAG/DB
```

---

## 安全架构

```
┌─────────────────────────────────────┐
│           XSS Filter                 │
├─────────────────────────────────────┤
│        Rate Limiting                 │
├─────────────────────────────────────┤
│       Sa-Token JWT                   │
├─────────────────────────────────────┤
│       RBAC Permission                │
└─────────────────────────────────────┘
```

---

## 监控架构

```
┌─────────────┐     ┌─────────────┐
│   Prometheus │────▶│   Grafana   │
└─────────────┘     └─────────────┘
       ▲
       │
┌──────┴──────┐
│ Spring Boot │
│   Actuator  │
└─────────────┘

┌─────────────┐     ┌─────────────┐
│    Loki     │────▶│   Grafana   │
└─────────────┘     └─────────────┘
       ▲
       │
┌──────┴──────┐
│   Alloy     │
│  (Log Col)  │
└─────────────┘
```

---

## 扩展阅读

- [MCP 服务器文档](MCP.md)
- [API 参考](API.md)
- [部署手册](DEPLOYMENT.md)