# xarch 开发任务

## 已完成

- [x] 项目结构搭建（前后端分离）
- [x] 后端 parent pom 和 BOM
- [x] xarch-common-core 模块
- [x] xarch-db-spring-boot-starter 模块（MySQL/PostgreSQL）
- [x] xarch-web-spring-boot-starter 模块（REST API）
- [x] xarch-cache-spring-boot-starter 模块（Redis）
- [x] xarch-example 示例应用
- [x] Vue 3 前端脚手架
- [x] Docker 部署配置
- [x] 初始化数据库脚本
- [x] Git 初始化提交

### 后端增强

- [x] 添加 MyBatis generic base mapper
- [x] 添加分页插件自动配置
- [x] 添加 SQL 日志打印功能（@XarchLog）
- [x] 完善 Knife4j 配置
- [x] 添加登录/登出 API（/api/auth/login, /api/auth/logout）

### 前端增强

- [x] 添加登录/登出功能
- [x] 添加用户管理页面
- [x] 完善路由守卫
- [x] 添加 auth store (Pinia)
- [x] 添加请求 Loading 效果

### 部署

- [x] 分离 backend 和 vue3-admin 的 Dockerfile
- [x] 完善 docker-compose.yml

## 待完成

### 后端增强

- [ ] 添加 Redis 分布式锁工具
- [ ] 添加缓存注解 @XarchCache
- [ ] 添加多数据源支持
- [ ] 添加权限校验注解 @XarchAuth

### 前端增强

- [ ] 添加角色管理页面
- [ ] 添加权限管理页面
- [ ] 完善菜单导航
- [ ] 添加 CRUD 用户功能

### 测试

- [ ] 单元测试覆盖
- [ ] 集成测试

### 文档

- [ ] API 接口文档完善
- [ ] 部署文档完善