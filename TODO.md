# xarch 开发任务

## 已完成

### 项目结构
- [x] 前后端分离结构
- [x] Gradle 构建系统（JDK 25, Spring Boot 4.0）
- [x] 使用 Gradle 管理依赖

### 后端模块 (com.xarch.starter.*)
- [x] xarch-starter-core: 核心模块
  - ApiResult, PageResult - 统一响应
  - XarchException, BusinessException - 异常处理
  - @XarchLog, @Debounce, @NotZero - 注解
  - GlobalConstant, ResponseCode - 常量枚举
  - LoginUser, PageQuery, SelectIdsDTO - 实体
  - IdUtil, JsonUtil, ResultUtil - 工具类

- [x] xarch-starter-db: 数据库模块
  - MyBatis Plus 自动配置
  - PageHelper 分页插件
  - Druid 连接池
  - BaseMapper 通用Mapper

- [x] xarch-starter-web: Web模块
  - Sa-Token 认证（login/logout/captcha）
  - XarchExceptionHandler 全局异常处理
  - XarchLogAspect 操作日志切面
  - XarchSwaggerConfig Knife4j配置

- [x] xarch-starter-cache: 缓存模块
  - Redis/Redisson 占位配置

- [x] xarch-example: 示例应用
  - User CRUD 接口
  - 完整数据库初始化脚本 (init.sql)

### 数据库表
- [x] sys_user - 用户表
- [x] sys_dept - 部门表
- [x] sys_role - 角色表
- [x] sys_menu - 菜单表
- [x] sys_role_menu - 角色菜单关联表
- [x] sys_login_log - 登录日志表
- [x] sys_op_log - 操作日志表
- [x] sys_dict / sys_dict_data - 字典表
- [x] sys_config - 系统配置表

### 前端
- [x] Vue 3 + Vite 6 + Element Plus + Pinia + TypeScript
- [x] 登录页面（对接后端认证）
- [x] 首页（显示用户信息，登出）
- [x] 用户管理页面
- [x] 路由守卫

### 部署
- [x] Docker + docker-compose
- [x] 后端 Dockerfile (Gradle多阶段构建)
- [x] 前端 Dockerfile (Nginx)

## 待完成

### 后端功能
- [ ] 完善角色管理（CRUD，权限分配）
- [ ] 完善菜单管理（CRUD，树形结构）
- [ ] 完善部门管理（CRUD，树形结构）
- [ ] 完善字典管理（CRUD，数据维护）
- [ ] 完善系统配置（CRUD）
- [ ] 登录日志记录
- [ ] 操作日志记录
- [ ] 权限校验（@SaCheckPermission）

### 前端功能
- [ ] 角色管理页面
- [ ] 菜单管理页面
- [ ] 部门管理页面
- [ ] 字典管理页面
- [ ] 系统配置页面
- [ ] 登录日志页面
- [ ] 操作日志页面
- [ ] 完善 CRUD 功能

### 文档
- [ ] 完善 README.md
- [ ] API 接口文档
- [ ] 部署文档