# service-ai

AI micro-service — chat, command audit, MCP, RAG, AI-managed servers, behaviour tracking.

| Property | Value |
|---|---|
| Port | 9005 |
| Spring application name | `xarch-service-ai` |
| Database tables (owned) | `xarch_ai_server`, `_command_audit`, `_command_history`, `_command_session`, `_chat_session`, `_user_behavior` |
| Depends on | `:common`, `xarch-*` starters, Nacos, Spring AI |

## Controllers

| Controller | Path | Status | Responsibility |
|---|---|---|---|
| `ChatController`           | `/ai/chat`             | planned    | LLM chat sessions |
| `CommandAuditController`   | `/ai/audit`            | migrated   | Audit log + approval workflow |
| `McpManagementController`  | `/ai/mcp/management`   | planned    | MCP server registry |
| `McpToolController`        | `/ai/mcp/tools`        | planned    | MCP tool invocation |
| `RagController`            | `/ai/rag`              | planned    | Retrieval-augmented generation |
| `ServerController`         | `/ai/server`           | migrated   | Linux server mgmt + AI agent |
| `SkillController`          | `/ai/skills`           | planned    | User-defined AI skills |
| `TaskController`           | `/ai/tasks`            | planned    | Async AI task tracking |
| `UserBehaviorController`   | `/ai/behaviors`        | migrated   | User behaviour tracking |

## Build & run

```bash
./gradlew :service-ai:bootRun
```