# service-message

Messaging micro-service — notifications and OAuth/SSO client management.

| Property | Value |
|---|---|
| Port | 9006 |
| Spring application name | `xarch-service-message` |
| Database tables (owned) | `xarch_message_message`, `_client` |
| Depends on | `:common`, `xarch-*` starters, Nacos, WebSocket |

## Controllers

| Controller | Path | Responsibility |
|---|---|---|
| `MessageController` | `/api/messages` | Message CRUD + unread count + todo list |
| `ClientController`  | `/api/clients`  | OAuth/SSO client CRUD |

## Build & run

```bash
./gradlew :service-message:bootRun
```