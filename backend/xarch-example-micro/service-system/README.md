# service-system

System management micro-service — RBAC, audit, notices, posts, online users.

| Property | Value |
|---|---|
| Port | 9001 (9002 actually) |
| Spring application name | `xarch-service-system` |
| Database tables (owned) | `xarch_system_role`, `_menu`, `_dept`, `_dict`, `_dict_data`, `_config`, `_op_log`, `_login_log`, `_notice`, `_post`, `_user_online` |
| Depends on | `:common`, `xarch-*` starters, Nacos, Feign clients (auth, file) |

## Controllers

| Controller | Path | Responsibility |
|---|---|---|
| `RoleController`    | `/api/roles`     | Role CRUD + menu/dept assignment |
| `MenuController`    | `/api/menus`     | Menu tree CRUD |
| `DeptController`    | `/api/depts`     | Department tree CRUD |
| `DictController`    | `/api/dicts`     | Dictionary + dict data |
| `ConfigController`  | `/api/configs`   | System config |
| `LogController`     | `/api/logs`      | Operation log + login log |
| `NoticeController`  | `/system/notice` | Notice CRUD |
| `PostController`    | `/system/post`   | Post CRUD |
| `OnlineController`  | `/monitor/online`| Online user list + force logout |

## Build & run

```bash
./gradlew :service-system:bootRun
```