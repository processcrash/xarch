# 安装指南

本指南将帮助你在本地环境中搭建 xarch 开发环境。

## 环境要求

### 后端环境

| 要求 | 版本 | 说明 |
|------|------|------|
| JDK | 25+ | 推荐使用 Eclipse Temurin 或 Amazon Corretto |
| Gradle | 8.11+ | 或使用项目自带的 gradlew |
| Node.js | 20+ | 用于 MCP Servers (TypeScript) |
| Python | 3.10+ | 用于 Python MCP Servers |

### 数据库

| 数据库 | 版本 | 说明 |
|--------|------|------|
| PostgreSQL | 16+ | 默认数据库 |
| MySQL | 8.0+ | 可选 |
| Redis | 7+ | 缓存与会话 |

### 基础设施

| 组件 | 版本 | 说明 |
|------|------|------|
| Nacos | 3.2+ | 服务注册与配置中心 |
| MinIO | 最新 | 对象存储（可选） |

---

## 环境安装

### 1. 安装 Java 25

```bash
# macOS (使用 Homebrew)
brew install openjdk@25

# Ubuntu/Debian
sudo apt-get install openjdk-25-jdk

# Windows
# 从 https://adoptium.net/ 下载安装
```

验证安装：
```bash
java -version
# openjdk version "25" ...
```

### 2. 安装 Node.js 20+

```bash
# macOS
brew install node@20

# Ubuntu
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# Windows
# 从 https://nodejs.org/ 下载 LTS 版本
```

验证安装：
```bash
node -v  # v20.x.x
npm -v   # 10.x.x
```

### 3. 安装 PostgreSQL 16

```bash
# macOS
brew install postgresql@16
brew services start postgresql@16

# Ubuntu
sudo apt-get install postgresql-16

# Windows
# 从 https://www.postgresql.org/download/ 下载
```

### 4. 安装 Redis

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu
sudo apt-get install redis-server

# Windows (使用 WSL 或 Docker)
docker run -d -p 6379:6379 redis:7
```

---

## 项目安装

### 1. 克隆项目

```bash
git clone https://github.com/processcrash/xarch.git
cd xarch
```

### 2. 初始化数据库

```bash
# 登录 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE xarch;

# 退出
\q

# 执行初始化脚本
psql -U postgres -d xarch -f docs/db/init-postgresql.sql
```

### 3. 构建后端

```bash
cd backend

# 使用 gradlew (推荐)
./gradlew build -x test

# 或全局安装 Gradle 后使用
gradle build -x test
```

### 4. 启动服务

```bash
# 启动示例应用
cd xarch-example
./gradlew bootRun

# 服务地址：http://localhost:8080
# API 文档：http://localhost:8080/doc.html
```

### 5. 前端启动

```bash
cd vue3-admin

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 访问地址：http://localhost:3000
```

### 6. MCP Servers 启动 (可选)

**Node.js MCP Servers:**

```bash
cd mcp-servers/database-mcp
npm install
npm run dev

cd ../knowledge-mcp
npm install
npm run dev

cd ../filesystem-mcp
npm install
npm run dev
```

**Python MCP Servers:**

```bash
cd mcp-servers/python

# 数据库 MCP
python -m database_mcp

# 知识库 MCP
python -m knowledge_mcp

# 文件系统 MCP
python -m filesystem_mcp
```

---

## Docker 快速启动

```bash
# 启动所有服务
docker-compose up -d

# 访问点：
# - 前端：http://localhost
# - 后端 API：http://localhost/api
# - API 文档：http://localhost:8080/doc.html
```

---

## 常见问题

### Q: 启动时报错 "端口已被占用"

检查并关闭占用端口的进程：

```bash
# macOS/Linux
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Q: 数据库连接失败

1. 检查 PostgreSQL 服务是否启动
2. 验证用户名密码
3. 检查 `application.yml` 配置

### Q: Gradle 构建失败

```bash
# 清理缓存
./gradlew clean

# 重新构建
./gradlew build --refresh-dependencies
```

---

## 下一步

- [架构设计](ARCHITECTURE.md) - 了解系统架构
- [开发规范](DEVELOPMENT.md) - 掌握开发标准
- [部署手册](DEPLOYMENT.md) - 了解生产部署