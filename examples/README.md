# xarch 使用案例集

本文档展示了使用 xarch 框架构建的实际项目案例。

## 案例列表

| 案例 | 描述 | 技术栈 |
|------|------|--------|
| [OA 办公系统](./oa-system/README.md) | 企业办公自动化系统 | Spring Boot + Vue 3 |
| [CRM 客户管理系统](./crm-system/README.md) | 客户关系管理系统 | Spring Boot + Vue 3 |
| [CMS 内容管理系统](./cms-system/README.md) | 网站内容管理系统 | Spring Boot + Vue 3 |

---

## 快速开始

### 1. OA 办公系统

```bash
cd examples/oa-system
docker-compose up -d
# 访问 http://localhost:8080
```

### 2. CRM 客户管理

```bash
cd examples/crm-system
docker-compose up -d
# 访问 http://localhost:8080
```

### 3. CMS 内容管理

```bash
cd examples/cms-system
docker-compose up -d
# 访问 http://localhost:8080
```

---

## 共同特性

所有案例均基于 xarch 框架，具备以下特性：

- 用户管理（CRUD、角色分配）
- 权限管理（菜单、按钮权限）
- 日志管理（登录日志、操作日志）
- 文件管理（多存储策略）
- 消息通知
- 系统配置
- 审计功能
- API 文档（Swagger/Knife4j）