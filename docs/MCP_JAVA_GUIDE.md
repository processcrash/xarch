# xarch Java MCP Servers 使用指南

> `java-mcp-servers/` — Java 实现的 4 个 stdio MCP 服务器，与 Node.js / Python 版本 API 完全对等

---

## 1. 是什么

xarch 平台的 MCP 服务器有 **三套实现**，互相等价：

| 语言 | 路径 | 适合 |
|------|------|------|
| **Node.js / TypeScript** | `node-mcp-servers/` | JS 团队、Claude Desktop 首发 |
| **Python** | `py-mcp-servers/` | Python 团队、AI 研究 |
| **Java** | `java-mcp-servers/` ← **本文档** | JVM 团队、与 Spring 服务同进程 |

三个版本都遵循 **MCP 2024-11-05** 标准 stdio JSON-RPC 2.0 协议，工具名称、参数、返回值**完全一致**，可以在客户端配置里无缝切换。

---

## 2. 4 个 stdio 服务器一览

| 服务器 | 工具数 | 资源 | 提示 | 文件 |
|--------|--------|------|------|------|
| **database-mcp** | 8 | 1 (`config://current`) | 1 (`sql-query`) | `database-mcp/` |
| **knowledge-mcp** | 9 | 1 (`kb://stats`) | 1 (`rag-search`) | `knowledge-mcp/` |
| **filesystem-mcp** | 10 | 2 (`fs://config`, `fs://stats`) | 1 (`file-search`) | `filesystem-mcp/` |
| **vector-mcp** | 9 | 2 (`vector://config`, `vector://collections`) | 2 (`semantic-search`, `similarity-search`) | `vector-mcp/` |

详见每个子模块的 README。

---

## 3. 5 分钟集成

### 3.1 构建

```bash
cd java-mcp-servers
./gradlew installDist
```

产出位置：
```
database-mcp/build/install/database-mcp/bin/database-mcp
knowledge-mcp/build/install/knowledge-mcp/bin/knowledge-mcp
filesystem-mcp/build/install/filesystem-mcp/bin/filesystem-mcp
vector-mcp/build/install/vector-mcp/bin/vector-mcp
```

### 3.2 冒烟测试

```bash
./scripts/test-server.sh database-mcp
```

会向服务器发送 `initialize`、`tools/list`、`ping` 三个请求，验证协议正确性。

### 3.3 接入 Claude Desktop

`~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) 或 `%APPDATA%\Claude\claude_desktop_config.json` (Windows)：

```json
{
  "mcpServers": {
    "xarch-database": {
      "command": "java",
      "args": ["-jar", "C:/workspace/java-mcp-servers/database-mcp/build/install/database-mcp/lib/database-mcp-1.0.0.jar"]
    },
    "xarch-knowledge": {
      "command": "java",
      "args": ["-jar", "C:/workspace/java-mcp-servers/knowledge-mcp/build/install/knowledge-mcp/lib/knowledge-mcp-1.0.0.jar"]
    },
    "xarch-filesystem": {
      "command": "java",
      "args": ["-jar", "C:/workspace/java-mcp-servers/filesystem-mcp/build/install/filesystem-mcp/lib/filesystem-mcp-1.0.0.jar"],
      "env": {
        "XARCH_FS_ALLOWED_ROOTS": "C:/workspace,C:/Users/me/Documents"
      }
    },
    "xarch-vector": {
      "command": "java",
      "args": ["-jar", "C:/workspace/java-mcp-servers/vector-mcp/build/install/vector-mcp/lib/vector-mcp-1.0.0.jar"]
    }
  }
}
```

重启 Claude Desktop，工具就出现在 LLM 上下文里。

### 3.4 接入 Cursor

`~/.cursor/mcp.json`：

```json
{
  "mcpServers": {
    "xarch-vector": { "command": "java", "args": ["-jar", "/path/to/vector-mcp.jar"] }
  }
}
```

---

## 4. 与 HTTP MCP 的区别

| 维度 | stdio (本目录) | HTTP (xarch-mcp-*) |
|------|----------------|---------------------|
| 协议 | JSON-RPC over stdio | REST over HTTP |
| 部署 | 桌面端、本地开发 | K8s、生产 |
| 鉴权 | 不需要（本地信任） | Sa-Token / OAuth |
| 状态 | 进程内 | 服务注册中心（Nacos）|
| 用途 | AI 客户端直连 | 跨服务、跨语言 |
| 端口 | 0 | 9091-9094 |

**生产部署用 HTTP 版本（`backend/xarch-spring-boot-starter/xarch-mcp/`），开发调试用 stdio 版本（本目录）。**

---

## 5. 协议实现细节

### 5.1 `mcp-runtime` 库

`java-mcp-servers/mcp-runtime/` 是一个 ~300 行的轻量 stdio MCP 协议实现，支持：

| 方法 | 用途 |
|------|------|
| `initialize` | 握手，返回 serverInfo + capabilities |
| `notifications/initialized` | 客户端通知就绪（无响应）|
| `ping` | 心跳 |
| `tools/list` | 列出已注册工具 |
| `tools/call` | 调用工具，返回 `content[]` |
| `resources/list` | 列出已注册资源 |
| `resources/read` | 读取资源内容 |
| `prompts/list` | 列出已注册提示模板 |
| `prompts/get` | 渲染提示模板 |

### 5.2 服务器注册工具的最小代码

```java
public static void main(String[] args) {
    new StdioMcpServer("my-server", "1.0.0")
        .tool("greet", "Greet a person", schema, args -> {
            String name = args.path("name").asText("World");
            return List.of(ContentBlock.text("Hello, " + name + "!"));
        })
        .run();   // reads stdin, writes stdout, logs to stderr
}
```

### 5.3 输入 schema 示例

```java
ObjectNode schema = StdioMcpServer.mapper().createObjectNode();
schema.put("type", "object");
ObjectNode props = schema.putObject("properties");
props.putObject("name").put("type", "string").put("description", "Person to greet");
props.putObject("greeting").put("type", "string").put("default", "Hello");
schema.put("required", StdioMcpServer.mapper().createArrayNode().add("name"));
```

### 5.4 错误处理

- 工具抛异常 → runtime 返回 JSON-RPC 错误码 `-32603`（内部错误）
- 未知方法 → `-32601`（方法未找到）
- 工具不存在 → 抛 `IllegalArgumentException`（被 runtime 转成 `-32603`）

---

## 6. 各服务器实现亮点

### 6.1 database-mcp

- **模拟 JDBC 客户端**（无真驱动依赖）：返回固定的 mock 表（users, orders, products, audit_log, files）
- 切换真实数据库：替换 `DatabaseClient.java` 内部，加入对应 JDBC 驱动依赖
- **Prompt `sql-query`**：让 LLM 根据自然语言意图生成 SQL

### 6.2 knowledge-mcp

- **内存 RAG**：文档分块（500 字符 + 50 重叠），按 token 出现频次打分
- **8 工具**含 `kb_update`（重新索引）
- **Prompt `rag-search`**：标准 RAG 模板（query → context → answer）

### 6.3 filesystem-mcp

- **路径遍历保护**：`PathGuard` 强制所有 path 必须在 `XARCH_FS_ALLOWED_ROOTS` 范围内
- **10 工具**覆盖读、写、复制、移动、搜索、stat
- 默认允许根目录：`System.getProperty("user.dir")`，可被环境变量覆盖
- Windows + Linux 路径都支持

### 6.4 vector-mcp

- **复用现有 math**：从 `xarch-mcp-vector`（REST 版本）原样拷贝 `DistanceFunction` + `VectorCollection` + `VectorStore`，保证两个实现的数学完全一致
- **9 工具**含 KNN search、按 metadata 过滤、批量 upsert
- **两个 prompt**：`semantic-search`（自然语言）+ `similarity-search`（向量）

---

## 7. 测试

```bash
# 单元测试
./gradlew test

# 协议端到端测试（手动）
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{...}}' | \
  java -jar vector-mcp/build/install/vector-mcp/lib/vector-mcp-1.0.0.jar
```

每个服务器至少包含：
- 工具处理函数测试
- 配置/状态测试
- 边界条件测试（空值、超长、错误输入）

---

## 8. 常见问题

### Q: 怎么把 stdio 工具切换到生产 HTTP？

用 `backend/xarch-spring-boot-starter/xarch-mcp/`，工具名称完全一致，只是 transport 从 stdio 变 HTTP。

### Q: 服务器启动后没有响应？

检查：
- `java -jar` 路径是否正确
- `~/.mcp/logs/`（如有）是否有错误
- 端口被占用（stdio 不占端口）— 那一定是别的问题

### Q: 怎么添加自定义工具？

```java
new StdioMcpServer("my-server", "1.0.0")
    .tool("my_tool", "Description", schema, args -> {
        // your logic
        return List.of(ContentBlock.text("result"));
    })
    .run();
```

### Q: 怎么禁用某个工具？

不调用 `.tool(name, ...)` 即可。

### Q: 性能如何？

stdio 启动 < 1s，单次工具调用 < 10ms（in-memory），适合交互式使用。
HTTP 版本（`xarch-mcp-*`）适合服务化部署，但有网络开销。

### Q: 能和 Spring Boot 应用一起跑吗？

可以 — 启动时 `Runtime.getRuntime().exec(...)` 拉起 stdio 进程，通过 `Process.getInputStream()` / `getOutputStream()` 通信。
xarch 的 MCP starter (`xarch-mcp-vector` 等) 就是用类似方式集成。

---

## 9. 调试技巧

### 9.1 查看原始 JSON-RPC 流量

```bash
# 终端 1：启动服务器
./vector-mcp/build/install/vector-mcp/bin/vector-mcp 2>server.log

# 终端 2：发送请求
echo '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | nc -U /tmp/mcp.sock
```

### 9.2 用 mcp-cli 调试

如果有 Python 的 `mcp` 包：

```bash
pip install mcp
mcp-client --stdio "java -jar vector-mcp.jar" list-tools
```

### 9.3 启用详细日志

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar vector-mcp.jar
```

日志**只写 stderr**，绝不污染 stdout 的 MCP 通道。

---

## 10. 相关文档

- [MCP 协议规范](https://spec.modelcontextprotocol.io/)
- [MCP 总体指南](../docs/MCP_GUIDE.md) — 三种语言 + 部署
- [Node.js MCP 实现](../node-mcp-servers/)
- [Python MCP 实现](../py-mcp-servers/)
- [Java HTTP MCP 实现](../backend/xarch-spring-boot-starter/xarch-mcp/)
- [架构文档](../docs/ARCHITECTURE.md)

---

最后更新：2026-07-01
