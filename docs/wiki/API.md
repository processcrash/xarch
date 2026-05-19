# API 参考

## 基础信息

- **Base URL**: `http://localhost:8080`
- **API 文档**: `http://localhost:8080/doc.html`
- **认证方式**: Bearer Token (JWT)

## 统一响应格式

```json
{
  "code": "0000",
  "msg": "success",
  "data": { ... },
  "timestamp": 1716038400000
}
```

### 响应码规范

| 响应码 | 说明 |
|--------|------|
| `0000` | 成功 |
| `1001` | 参数错误 |
| `1002` | 业务异常 |
| `1003` | 认证失败 |
| `1004` | 资源未找到 |
| `1005` | 系统错误 |

---

## 系统管理

### 用户管理 `/system/user`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/user/page` | 分页查询用户 |
| GET | `/system/user/{id}` | 获取用户详情 |
| POST | `/system/user` | 创建用户 |
| PUT | `/system/user` | 更新用户 |
| DELETE | `/system/user/{id}` | 删除用户 |
| PUT | `/system/user/role` | 分配角色 |

### 角色管理 `/system/role`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/role/page` | 分页查询角色 |
| GET | `/system/role/{id}` | 获取角色详情 |
| POST | `/system/role` | 创建角色 |
| PUT | `/system/role` | 更新角色 |
| DELETE | `/system/role/{id}` | 删除角色 |
| PUT | `/system/role/menu` | 分配菜单权限 |

### 菜单管理 `/system/menu`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/menu/page` | 分页查询菜单 |
| GET | `/system/menu/{id}` | 获取菜单详情 |
| POST | `/system/menu` | 创建菜单 |
| PUT | `/system/menu` | 更新菜单 |
| DELETE | `/system/menu/{id}` | 删除菜单 |
| GET | `/system/menu/tree` | 获取菜单树 |

### 部门管理 `/system/dept`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/dept/page` | 分页查询部门 |
| GET | `/system/dept/tree` | 获取部门树 |
| GET | `/system/dept/{id}` | 获取部门详情 |
| POST | `/system/dept` | 创建部门 |
| PUT | `/system/dept` | 更新部门 |
| DELETE | `/system/dept/{id}` | 删除部门 |

### 岗位管理 `/system/post`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/post/page` | 分页查询岗位 |
| GET | `/system/post/{id}` | 获取岗位详情 |
| POST | `/system/post` | 创建岗位 |
| PUT | `/system/post` | 更新岗位 |
| DELETE | `/system/post/{id}` | 删除岗位 |

### 通知公告 `/system/notice`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/notice/page` | 分页查询公告 |
| GET | `/system/notice/{id}` | 获取公告详情 |
| POST | `/system/notice` | 创建公告 |
| PUT | `/system/notice` | 更新公告 |
| DELETE | `/system/notice/{id}` | 删除公告 |

---

## 系统配置

### 字典管理 `/system/dict`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/dict/type/page` | 分页查询字典类型 |
| GET | `/system/dict/type/{id}` | 获取字典类型详情 |
| POST | `/system/dict/type` | 创建字典类型 |
| PUT | `/system/dict/type` | 更新字典类型 |
| DELETE | `/system/dict/type/{id}` | 删除字典类型 |
| GET | `/system/dict/data/{typeCode}` | 获取字典数据 |

### 参数配置 `/system/config`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/system/config/page` | 分页查询参数 |
| GET | `/system/config/{id}` | 获取参数详情 |
| POST | `/system/config` | 创建参数 |
| PUT | `/system/config` | 更新参数 |
| DELETE | `/system/config/{id}` | 删除参数 |

---

## 日志管理

### 登录日志 `/monitor/logininfor`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/logininfor/page` | 分页查询登录日志 |
| GET | `/monitor/logininfor/export` | 导出登录日志 |
| DELETE | `/monitor/logininfor/clean` | 清理登录日志 |

### 操作日志 `/monitor/operlog`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/operlog/page` | 分页查询操作日志 |
| GET | `/monitor/operlog/{id}` | 获取操作日志详情 |
| DELETE | `/monitor/operlog/{id}` | 删除操作日志 |
| DELETE | `/monitor/operlog/clean` | 清理操作日志 |
| GET | `/monitor/operlog/export` | 导出操作日志 |

---

## 监控管理

### 服务器监控 `/monitor/server`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/server` | 获取服务器状态 |

### 缓存监控 `/monitor/cache`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/cache` | 获取缓存状态 |

### 在线用户 `/monitor/online`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/online/page` | 分页查询在线用户 |
| DELETE | `/monitor/online/{id}` | 强制下线 |
| DELETE | `/monitor/online/batch` | 批量下线 |

### 定时任务 `/monitor/job`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/job/page` | 分页查询任务 |
| GET | `/monitor/job/{id}` | 获取任务详情 |
| POST | `/monitor/job` | 创建任务 |
| PUT | `/monitor/job` | 更新任务 |
| DELETE | `/monitor/job/{id}` | 删除任务 |
| POST | `/monitor/job/run/{id}` | 执行一次任务 |
| PUT | `/monitor/job/{id}/change-status` | 启停任务 |

### 任务日志 `/monitor/jobLog`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/jobLog/page` | 分页查询任务日志 |
| DELETE | `/monitor/jobLog/clean` | 清理任务日志 |

---

## MCP Server API

### Database MCP `/mcp/database`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/mcp/database/tools/query_execute` | 执行查询 |
| POST | `/mcp/database/tools/execute_update` | 执行更新 |
| POST | `/mcp/database/tools/schema_get` | 获取架构 |
| POST | `/mcp/database/tools/table_list` | 列出表 |
| POST | `/mcp/database/tools/table_describe` | 表结构 |
| POST | `/mcp/database/tools/health` | 健康检查 |

### Knowledge MCP `/mcp/knowledge`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/mcp/knowledge/tools/kb_index_document` | 索引文档 |
| POST | `/mcp/knowledge/tools/kb_index_file` | 索引文件 |
| POST | `/mcp/knowledge/tools/kb_search` | 搜索 |
| POST | `/mcp/knowledge/tools/kb_get_document` | 获取文档 |
| POST | `/mcp/knowledge/tools/kb_delete` | 删除文档 |
| POST | `/mcp/knowledge/tools/kb_list` | 列出文档 |
| POST | `/mcp/knowledge/tools/health` | 健康检查 |

### Filesystem MCP `/mcp/filesystem`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/mcp/filesystem/tools/list_directory` | 列目录 |
| POST | `/mcp/filesystem/tools/read_file` | 读文件 |
| POST | `/mcp/filesystem/tools/write_file` | 写文件 |
| POST | `/mcp/filesystem/tools/delete` | 删除 |
| POST | `/mcp/filesystem/tools/create_directory` | 创建目录 |
| POST | `/mcp/filesystem/tools/search_files` | 搜索文件 |
| POST | `/mcp/filesystem/tools/get_file_info` | 文件信息 |
| POST | `/mcp/filesystem/tools/health` | 健康检查 |

---

## 文件管理 `/file`

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/file/page` | 分页查询文件 |
| GET | `/file/{id}` | 文件详情 |
| POST | `/file/upload` | 上传文件 |
| GET | `/file/download/{id}` | 下载文件 |
| GET | `/file/preview/{id}` | 预览文件 |
| DELETE | `/file/{id}` | 删除文件 |
| GET | `/file/stats` | 存储统计 |
| GET | `/file/storage/configs` | 存储配置列表 |
| POST | `/file/storage/config` | 创建存储配置 |
| PUT | `/file/storage/config` | 更新存储配置 |
| DELETE | `/file/storage/config/{id}` | 删除存储配置 |
| POST | `/file/storage/config/{id}/test` | 测试连接 |

---

## Excel 操作 `/excel`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/excel/import` | 导入 Excel |
| GET | `/excel/export` | 导出 Excel |

---

## 通用操作 `/common`

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/common/upload` | 上传文件 |
| GET | `/common/download/{fileId}` | 下载文件 |