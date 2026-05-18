# xarch 需求文档

## 概述

xarch 是一个企业级后台开发框架，包含 Java Spring Boot 后端和 Vue 3 前端。

## 技术栈

### 后端
- Java 25
- Spring Boot 4.0
- MyBatis 3.0 + PageHelper
- MySQL 8.0 / PostgreSQL
- Redis 7 + Redisson
- Knife4j (Swagger 3.0)

### 前端
- Vue 3.5
- Vite 6
- Element Plus
- Pinia
- TypeScript

## 模块设计

### 后端 Starter 模块

遵循 Spring Boot Starter 规范，引入即用：

| 模块 | 描述 | 依赖 |
|------|------|------|
| xarch-common-core | 通用核心模块 | ApiResult, ResultCode, Exception, Utils |
| xarch-db-spring-boot-starter | 数据库模块 | MySQL/PostgreSQL, MyBatis, Druid |
| xarch-web-spring-boot-starter | Web模块 | REST API, Knife4j, Exception Handler |
| xarch-cache-spring-boot-starter | 缓存模块 | Redis, Redisson |

### 包命名规范

```
com.xarch
├── common
│   └── core
│       ├── result      # 统一响应
│       ├── exception   # 异常定义
│       └── util        # 工具类
├── db
│   ├── autoconfigure   # 自动配置
│   ├── datasource      # 数据源
│   └── page           # 分页
├── web
│   ├── autoconfigure   # Web自动配置
│   ├── advice         # 全局异常处理
│   └── config         # Web配置
└── cache
    ├── autoconfigure   # 缓存自动配置
    └── redis          # Redis工具
```

## 配置规范

### 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123
```

### Redis配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
```

## API规范

### 统一响应格式

```json
{
  "code": "200",
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

### 状态码定义

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

## 部署要求

- 支持 Docker 容器化部署
- 支持 docker-compose 一键启动
- 前端 Nginx 反向代理到后端
- 数据库初始化脚本即开即用