# MCP 服务器

## 概述

MCP (Model Context Protocol) 是 AI 与企业系统连接的桥梁。xarch 提供三个开箱即用的 MCP Server，支持 Java、Node.js、Python 三种实现。

---

## 架构

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

---

## Database MCP Server

### 功能

- 多数据库支持：MySQL, PostgreSQL, MongoDB, SQL Server
- SQL 查询执行
- 数据库架构查询
- 表结构查看

### 工具列表

| 工具名称 | 功能说明 |
|----------|---------|
| `query_execute` | 执行 SQL 查询（SELECT） |
| `execute_update` | 执行 INSERT/UPDATE/DELETE |
| `schema_get` | 获取数据库架构 |
| `table_list` | 列出所有表 |
| `table_describe` | 描述表结构 |
| `index_list` | 列出索引 |
| `configure` | 配置数据库连接 |
| `health` | 健康检查 |

### 启动

**Java 版本：**
```bash
cd backend/xarch-spring-boot-starter/xarch-mcp/xarch-mcp-database
./gradlew bootRun
# 端口：9090
```

**Node.js 版本：**
```bash
cd mcp-servers/database-mcp
npm install
npm run dev
# 端口：3000
```

**Python 版本：**
```bash
cd mcp-servers/python
python -m database_mcp
# 端口：8765
```

### API 端点

```
POST /mcp/database/tools/query_execute
POST /mcp/database/tools/execute_update
POST /mcp/database/tools/schema_get
POST /mcp/database/tools/table_list
POST /mcp/database/tools/table_describe
GET  /mcp/database/health
```

---

## Knowledge MCP Server

### 功能

- 企业级 RAG 知识库
- 文档索引与搜索
- 语义检索
- 多格式支持：PDF, Markdown, TXT

### 工具列表

| 工具名称 | 功能说明 |
|----------|---------|
| `kb_index_document` | 索引文档 |
| `kb_index_file` | 索引文件 |
| `kb_search` | 语义搜索 |
| `kb_get_document` | 获取文档 |
| `kb_delete` | 删除文档 |
| `kb_list` | 列出所有文档 |
| `kb_update` | 更新文档 |
| `kb_stats` | 统计信息 |
| `health` | 健康检查 |

### 启动

**Java 版本：**
```bash
cd backend/xarch-spring-boot-starter/xarch-mcp/xarch-mcp-knowledge
./gradlew bootRun
# 端口：9091
```

**Node.js 版本：**
```bash
cd mcp-servers/knowledge-mcp
npm install
npm run dev
# 端口：3001
```

**Python 版本：**
```bash
cd mcp-servers/python
python -m knowledge_mcp
# 端口：8766
```

### API 端点

```
POST /mcp/knowledge/tools/kb_index_document
POST /mcp/knowledge/tools/kb_index_file
POST /mcp/knowledge/tools/kb_search
POST /mcp/knowledge/tools/kb_get_document
POST /mcp/knowledge/tools/kb_delete
GET  /mcp/knowledge/health
```

---

## Filesystem MCP Server

### 功能

- 安全文件操作
- 路径遍历防护
- 目录浏览
- 文件读写

### 工具列表

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

### 启动

**Java 版本：**
```bash
cd backend/xarch-spring-boot-starter/xarch-mcp/xarch-mcp-filesystem
./gradlew bootRun
# 端口：9092
```

**Node.js 版本：**
```bash
cd mcp-servers/filesystem-mcp
npm install
npm run dev
# 端口：3002
```

**Python 版本：**
```bash
cd mcp-servers/python
python -m filesystem_mcp
# 端口：8767
```

### API 端点

```
POST /mcp/filesystem/tools/list_directory
POST /mcp/filesystem/tools/read_file
POST /mcp/filesystem/tools/write_file
POST /mcp/filesystem/tools/delete
POST /mcp/filesystem/tools/create_directory
GET  /mcp/filesystem/health
```

---

## Nacos 服务注册

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

## 使用示例

### Claude Desktop 集成

在 `claude_desktop_config.json` 中添加：

```json
{
  "mcpServers": {
    "xarch-database": {
      "command": "node",
      "args": ["/path/to/mcp-servers/database-mcp/dist/index.js"],
      "env": {
        "DATABASE_URL": "postgresql://localhost:5432/xarch"
      }
    }
  }
}
```

### 编程调用

```bash
# 查询数据库
curl -X POST http://localhost:9090/mcp/database/tools/query_execute \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM sys_user LIMIT 10"}'

# 搜索知识库
curl -X POST http://localhost:9091/mcp/knowledge/tools/kb_search \
  -H "Content-Type: application/json" \
  -d '{"query": "如何创建用户"}'
```

---

## 扩展阅读

- [架构设计](ARCHITECTURE.md)
- [API 参考](API.md)
- [部署手册](DEPLOYMENT.md)