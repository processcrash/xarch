# xarch 开发任务

## 已完成

### 项目结构
- [x] 前后端分离结构
- [x] Gradle 构建系统（JDK 25, Spring Boot 4.0）
- [x] 使用 Gradle 管理依赖
- [x] 模块结构重组（xarch-spring-boot-starter/、xarch-spring-cloud/）

### 后端 Starter 模块 (com.xarch.starter.*)
- [x] xarch-core-spring-boot-starter: 核心模块（ApiResult, ResultCode, Exception, Utils, 注解）
- [x] xarch-db-spring-boot-starter: 数据库模块（MyBatis Plus, Druid, PageHelper）
- [x] xarch-web-spring-boot-starter: Web模块（Sa-Token, Knife4j, Exception Handler）
- [x] xarch-cache-spring-boot-starter: 缓存模块（Redis/Redisson）

### Spring Cloud 模块 (com.xarch.cloud.*)
- [x] xarch-cloud-starter-nacos: Nacos 3.2 服务注册，MCP 服务自动注册
- [x] xarch-cloud-starter-gateway: Spring Cloud Gateway 路由配置
- [x] xarch-cloud-starter-mcp: MCP 协议核心定义

### MCP Server 模块 (com.xarch.mcp.*)
- [x] xarch-mcp-database: 数据库 MCP Server（MySQL/PostgreSQL/MongoDB/SQL Server）
- [x] xarch-mcp-knowledge: 知识库 MCP Server（RAG 语义搜索）
- [x] xarch-mcp-filesystem: 文件系统 MCP Server（安全文件操作）
- [x] @McpServer 注解实现 Nacos MCP 服务注册

### 功能模块 (xarch-example - 23 个控制器)
- [x] 用户管理 (UserController) - CRUD + 分页 + 角色分配
- [x] 角色管理 (RoleController) - CRUD + 分页 + 菜单分配 + 数据权限
- [x] 菜单管理 (MenuController) - CRUD + 树形结构
- [x] 部门管理 (DeptController) - CRUD + 树形结构
- [x] 岗位管理 (PostController) - CRUD + 分页
- [x] 通知公告 (NoticeController) - CRUD + 分页
- [x] 字典管理 (DictController) - CRUD + 数据维护
- [x] 系统配置 (ConfigController) - CRUD + 分页
- [x] 登录日志 (LoginLogController) - 分页查询
- [x] 操作日志 (OpLogController) - 分页查询
- [x] 认证 (AuthController) - 登录/登出/验证码/当前用户
- [x] 服务器监控 (SysServerController) - CPU/内存/JVM
- [x] 缓存监控 (SysCacheController) - Redis 状态
- [x] 在线用户 (SysUserOnlineController) - 会话管理
- [x] 定时任务 (SysJobController) - 调度管理
- [x] 任务日志 (SysJobLogController) - 执行记录
- [x] 验证码 (CaptchaController) - 图形验证
- [x] 客户端管理 (ClientController) - OAuth 客户端
- [x] 消息中心 (MessageController) - 站内消息
- [x] 资源管理 (ResourceController) - 文件资源
- [x] 临时文件 (TempFileController) - 上传管理
- [x] 通用操作 (CommonController) - 文件上传下载
- [x] Excel 导入导出 (ExcelController) - 用户导入导出

### 单元测试
- [x] DatabaseMcpControllerTest - 数据库 MCP 测试
- [x] KnowledgeMcpControllerTest - 知识库 MCP 测试
- [x] FilesystemMcpControllerTest - 文件系统 MCP 测试
- [x] UserControllerTest - 用户管理测试（含角色分配）
- [x] RoleControllerTest - 角色管理测试（含菜单/部门分配）
- [x] ExcelControllerTest - Excel 导入导出测试

### 前端 (Vue 3 + Element Plus)
- [x] 登录页面
- [x] 首页（带菜单导航）
- [x] 用户管理页面
- [x] 角色管理页面
- [x] 菜单管理页面
- [x] 部门管理页面
- [x] 字典管理页面
- [x] 系统配置页面
- [x] 日志页面（登录日志 + 操作日志）
- [x] 路由守卫
- [x] API 接口封装

### 数据库
- [x] init.sql 完整数据库初始化脚本（20+ 张表）

### 部署
- [x] Docker + docker-compose
- [x] 后端 Dockerfile (Gradle多阶段构建)
- [x] 前端 Dockerfile (Nginx)
- [x] Kubernetes 部署配置（base/ + overlays/dev/prod）
- [x] Kustomize 环境分离

### 文档
- [x] README.md - 完整项目文档
- [x] REQUIREMENTS.md - 需求规格说明书
- [x] TODO.md - 开发任务跟踪
- [x] k8s/README.md - K8s 部署说明
- [x] API Swagger 文档分组配置

---

## 待完成

### 前端完善
- [ ] 详情页/编辑页分离
- [ ] 表单验证完善
- [ ] 批量操作
- [ ] 高级搜索面板

### 安全性
- [ ] 权限校验完善（@SaCheckPermission 全面启用）
- [ ] 接口限流
- [ ] SQL注入防护（已内置 MyBatis Plus，补充参数校验）
- [ ] XSS防护

### 测试与文档
- [ ] 集成测试
- [ ] 部署文档完善

### Spring Cloud 生产完善
- [ ] Nacos 持久化配置（使用外部 MySQL）
- [ ] Gateway 路由动态配置
- [ ] 服务监控（Spring Cloud Actuator）