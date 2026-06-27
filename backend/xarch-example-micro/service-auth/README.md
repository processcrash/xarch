# service-auth

Authentication & user micro-service.

| Property | Value |
|---|---|
| Port | 9001 |
| Spring application name | `xarch-service-auth` |
| Database tables (owned) | `xarch_auth_user`, `xarch_auth_login_log` |
| Depends on | `:common`, `xarch-*` starters, Nacos |

## Controllers

| Controller | Path | Responsibility |
|---|---|---|
| `AuthController`  | `/api/auth`       | Captcha, login, logout |
| `UserController`  | `/api/users`      | User CRUD + role assignment |

## Cross-service calls (exported)

`UserFeignClient` — `GET /api/users/{id}` is exposed for peer services
that need to resolve a user name from an id.

## Build & run

```bash
./gradlew :service-auth:bootRun
```

Or via Docker Compose once the image is built.