# xarch - AI-Enabled Enterprise Backend Framework

> xarch 是 AI 时代企业级后台管理项目规范，基于 Spring Boot 4.0 + Vue 3 + MyBatis Plus 构建，为 AI 原生企业应用提供开箱即用的后台管理解决方案。

---

## 核心定位

xarch 不仅是一个框架，更是一套 **AI 时代企业后台管理的标准规范**：

- **AI-First Architecture** - 内置 AI 能力集成接口，支持智能辅助、内容生成、语义理解等 AI 功能
- **Enterprise-Grade** - 面向生产环境设计，提供完整的企业级功能：权限管理、操作审计、数据可视化
- **Modular Design** - 采用 Spring Boot Starter 架构，可按需引入，灵活组合
- **Convention Over Configuration** - 约定优于配置，极简开发体验

---

## 为什么选择 xarch？

| 特性 | xarch | 传统方案 |
|------|-------|---------|
| AI 能力集成 | 内置 AI 服务接口，开箱即用 | 需自行集成，复杂度高 |
| 开发效率 | Starter 按需引入，5 分钟启动 | 搭建繁琐，重复造轮子 |
| 代码规范 | 统一分包、命名、架构规范 | 无统一标准，质量参差 |
| 可维护性 | 分层清晰，模块解耦 | 容易形成巨石应用 |
| 测试覆盖 | 100% 控制器单元测试 | 缺乏测试，回归风险高 |

---

## 项目结构

```
xarch/
├── backend/                                    # Spring Boot 后端 (Gradle)
│   ├── xarch-bom/                              # Bill of Materials 版本管理
│   ├── xarch-core-spring-boot-starter/        # 核心模块：工具类、注解、实体基类
│   ├── xarch-db-spring-boot-starter/          # 数据库模块：MyBatis Plus、Druid 连接池
│   ├── xarch-web-spring-boot-starter/         # Web 模块：REST API、Swagger、Sa-Token 认证
│   ├── xarch-cache-spring-boot-starter/       # 缓存模块：Redis、Redisson 分布式锁
│   └── xarch-example/                          # 示例应用（22 个控制器）
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
├── docker-compose.yml                          # Docker 编排
└── init.sql                                    # 数据库初始化脚本
```

---

## 技术栈

### Backend

| 分类 | 技术 | 说明 |
|------|------|------|
| **Runtime** | Java 25 / Spring Boot 4.0 | 最新 LTS 版本 |
| **Build** | Gradle (Kotlin DSL) | 现代构建工具 |
| **ORM** | MyBatis Plus 3.5+ | 简化 CRUD 操作 |
| **Database** | MySQL 8.0 / PostgreSQL | 多数据库支持 |
| **Connection** | Druid | 监控型连接池 |
| **Cache** | Redis 7 + Redisson | 分布式缓存与锁 |
| **Auth** | Sa-Token (JWT) | 无状态认证 |
| **API Docs** | Knife4j (Swagger 3.0) | API 文档生成 |
| **Pagination** | PageHelper | 分页插件 |

### Frontend

| 分类 | 技术 | 说明 |
|------|------|------|
| **Framework** | Vue 3.5 + Vite 6 | 现代化前端框架 |
| **UI Library** | Element Plus | 企业级组件库 |
| **State** | Pinia | 状态管理 |
| **Language** | TypeScript | 类型安全 |
| **HTTP** | Axios | HTTP 请求 |

---

## 模块详解

### xarch-core-spring-boot-starter

核心基础模块，提供通用工具和注解。

**注解：**
- `@XarchLog` - 操作日志记录
- `@Debounce` - 防重复提交
- `@NotZero` - 参数校验

**工具类：**
- `IdUtil` - ID 生成器
- `JsonUtil` - JSON 序列化
- `ResultUtil` - 响应构建

**实体基类：**
- `BaseEntity` - 基础实体，含创建时间、更新时间
- `PageQuery` - 分页查询
- `LoginUser` - 登录用户信息

### xarch-db-spring-boot-starter

数据访问层模块，封装数据库操作。

**特性：**
- Druid 连接池自动配置
- MyBatis Plus 增强 CRUD
- PageHelper 分页插件
- 多数据源支持

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123
```

### xarch-web-spring-boot-starter

Web 服务层模块，提供 REST API 能力。

**特性：**
- Knife4j API 文档 (`/doc.html`)
- Sa-Token 认证鉴权
- CORS 跨域配置
- 全局异常处理
- AOP 操作审计
- 验证码生成

**认证接口：**
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/captcha` - 获取验证码

### xarch-cache-spring-boot-starter

缓存服务模块，提供 Redis 能力。

**特性：**
- RedisTemplate 自动配置
- Redisson 分布式锁
- 缓存键前缀管理
- TTL 过期策略

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
    implementation("com.xarch:xarch-db-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-web-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-cache-spring-boot-starter:1.0.0")
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
| `SysConfigController` | `/system/config/*` | 参数配置（新版） |

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

  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123

  redis:
    host: localhost
    port: 6379
    password:
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

22 个控制器全部配置单元测试：

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
... 共 22 个测试类
```

---

## 开发规范

### 包命名规范

```
com.xarch.starter.*   # 框架 Starter 模块
com.xarch.example.*    # 业务应用模块
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

**xarch - 让企业后台开发更简单，让 AI 集成更容易。**