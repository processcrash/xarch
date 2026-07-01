# xarch Issue 跟踪工作流

> **核心原则：先沟通清楚，再写代码。** "Discuss before code" 模式可避免大量返工，让路线图保持一致。

本文档面向 **维护者**（负责分流与回复）和 **贡献者**（提 issue / PR 的人），说明 xarch 项目从 issue 创建到关闭的完整生命周期。

---

## 1. 入口与分类

| 类型 | 适用场景 | 模板 | 标签 |
|------|----------|------|------|
| 🐛 **Bug Report** | 代码缺陷、回归、性能问题 | `.github/ISSUE_TEMPLATE/bug_report.yml` | `type: bug` |
| 💡 **Feature Request** | 新功能、改进、API 增强 | `feature_request.yml` | `type: feature` |
| 🔌 **MCP Server Proposal** | 提议新 MCP server 或 tool | `mcp_server.yml` | `type: feature` + `area: mcp` |
| ❓ **Question / Help** | 使用问题 | `question.yml` | `type: question` |
| 💬 **Discussions** | 开放式问答、想法、Show & Tell | [github.com/processcrash/xarch/discussions](https://github.com/processcrash/xarch/discussions) | — |
| 🔒 **Security** | 私密披露 | security@xarch.example | — |

> 配置 `config.yml` 关闭了"空白 issue"选项，强制使用模板，避免不完整报告。

---

## 2. 状态机

每个 issue 进入后会被打上 `status:` 前缀的状态标签：

```
┌──────────────────┐
│  needs-triage    │  ← 新 issue 进来
└────────┬─────────┘
         │ 维护者 3 天内回复
         ▼
┌────────────────────────────────────────────┐
│ confirmed (bug) │ needs-design │ roadmap   │
│ needs-info      │ duplicate    │ wontfix   │
└────┬───────────┴────┬─────────┴────────┬───┘
     │                │                  │
     │ PR opened      │ 讨论 + RFC       │ 关闭
     ▼                ▼                  ▼
┌──────────────────┐  ┌────────────────┐
│ ready-for-pr     │  │  design-rfc    │
└────┬─────────────┘  └────────────────┘
     │ PR merged
     ▼
  done / closed
```

| 状态 | 含义 | 维护者动作 |
|------|------|-----------|
| `status: needs-triage` | 刚创建，等待分类 | 7 天内首响应 |
| `status: needs-info` | 需要作者补充 | 提问并设 `awaiting-response` |
| `status: needs-design` | L/XL 改动，需要设计讨论 | 转为 Discussion 或开 RFC |
| `status: design-rfc` | RFC 阶段 | 标记 reviewers，1-2 周讨论 |
| `status: confirmed` | Bug 确认存在 | 打优先级标签 |
| `status: ready-for-pr` | 可以动手实现 | 移除 `needs-triage` |
| `status: in-progress` | 已有相关 PR | 链接 PR |
| `status: roadmap` | 纳入路线图 | 关联 milestone |
| `status: wontfix` | 不做 | 给出原因，礼貌关闭 |
| `status: duplicate` | 重复 | 链接原 issue，关闭 |

---

## 3. 优先级矩阵（仅对 bug）

| 标签 | 触发条件 | 修复 SLA |
|------|----------|----------|
| `priority: P0` | 阻塞性：登录失败、数据丢失、安全漏洞、CI 整体挂掉 | 24 小时 |
| `priority: P1` | 严重：主要功能失效、性能严重下降、数据不一致 | 1 周 |
| `priority: P2` | 一般：边缘场景失效、UI 小问题、文档错误 | 1 个月 |
| `priority: P3` | 锦上添花：错别字、风格不一致 | best-effort |

---

## 4. 维护者分流 Playbook

### 4.1 首响应（7 天内）

打开 `is:open label:"status: needs-triage"`，按以下顺序处理：

1. **判断类别**：bug / feature / question / MCP
2. **判断重复**：用 GitHub 搜索相似关键词；如有重复，关闭并链接
3. **判断完整性**：必填字段是否齐全
   - 不完整 → 评论"请补充 X/Y/Z"并加 `status: needs-info` + `awaiting-response`（7 天后无回应自动关闭）
4. **判断范围**：
   - XS/S 改动 → 直接 `status: ready-for-pr`
   - M 改动 → 加 `status: ready-for-pr`，设 milestone
   - L/XL 改动 → 转 Discussion 写 RFC，等设计冻结后再开 issue
5. **判断优先级**（仅 bug）：按上表打标签

### 4.2 每周例行（周一）

- 清理 `status: needs-triage` 超过 7 天的 issue
- 关闭 30 天无活动的 `awaiting-response` issue（礼貌地通知作者）
- 检查 `status: wontfix` 是否有 PR 尝试做（不主动 merge，但允许 fork）
- 拉取 stale bot 自动提醒（启用 GitHub ProBot）

### 4.3 每月底

- 把 `status: roadmap` 整理到下个 milestone
- 在 Discussions 发布月度 issue 摘要
- 关闭无 milestone 的 `status: in-progress` issue（如 PR 仍活跃可保留）

---

## 5. 贡献者 Playbook

### 5.1 提 issue 之前

1. **搜索**：用 GitHub 搜索 + 标签过滤（`is:open label:bug` 等）
2. **看 Discussions**：可能是设计讨论而非 bug
3. **用模板**：模板里有"自检"清单，先过一遍

### 5.2 写 issue

- **Bug 报告**：必填"现象 / 期望 / 复现步骤 / 版本 / 环境 / 日志"
- **Feature**：必填"问题 / 方案 / 范围 / 是否愿意实现"
- **Question**：必填"问题描述"，先看文档和 Discussions

### 5.3 跟进

- 维护者问问题 → 7 天内回复，否则会被关闭
- `status: needs-info` → 7 天无回应自动标记为 stale
- 想撤回 → 评论 "Closing in favor of ..." 或直接 close

### 5.4 从 issue 到 PR

```
issue 状态: status: ready-for-pr
  → fork → 创建分支
  → 在 PR 描述里写 "Closes #123" 或 "Fixes #456"
  → CI 全绿 + 1 位 reviewer 批准 → squash merge
  → issue 自动关闭
```

> ⚠️ **不要绕过 issue 直接开 PR**：M 以上的改动如果没有对应 issue，会被要求先创建 issue。

---

## 6. 标签体系

### 6.1 类别（`type:`）

- `type: bug` — 缺陷
- `type: feature` — 新功能或改进
- `type: question` — 使用问题
- `type: docs` — 纯文档改动
- `type: chore` — 维护性改动（依赖、CI、配置）

### 6.2 区域（`area:`）

- `area: backend`
- `area: frontend`
- `area: mcp`
- `area: k8s`
- `area: docs`
- `area: deps`
- `area: ci`

### 6.3 优先级（`priority:`）

- `priority: P0` / `P1` / `P2` / `P3`（仅 bug）

### 6.4 状态（`status:`）

见第 2 节。

### 6.5 友好标签

- `good first issue` — 新手友好（修复 30-300 LOC + 有清晰复现）
- `help wanted` — 维护者欢迎外部贡献
- `discussion` — 主要是讨论，不需要立即行动
- `wontfix` — 不会做（不删，可作历史记录）

---

## 7. 自动机器人配置

### 7.1 GitHub 内置

- **ProBot Stale**：30 天无活动自动标记 stale，14 天后自动关闭
- 配置文件：`.github/settings.yml`（如启用 Settings sync app）

### 7.2 自定义 action

`.github/workflows/triage.yml`（维护者可选启用）：

- 自动给新 issue 打 `status: needs-triage`
- 7 天无活动 + `awaiting-response` → 关闭
- PR 关联到 issue → 自动同步状态

---

## 8. 关键规则

1. **永不删除 issue**：即使关闭也保留，便于历史检索
2. **关闭要给理由**：`status: wontfix` 关闭时一定要写"为什么不修"
3. **善意推定**：用户报告 bug 是出于好意，先感谢，再说细节
4. **不要承诺时间线**：`status: roadmap` 表示"想做"，不是"什么时候做"
5. **隐私**：日志/截图里可能含 token、邮箱，先打码
6. **中文友好**：可用中文回复，技术术语中英混用即可

---

## 9. 工具与命令

```bash
# 列出所有 needs-triage issue（维护者）
gh issue list --label "status: needs-triage" --limit 100

# 批量关闭 stale issue
gh issue list --label "stale" --state open --json number --jq '.[].number' \
  | xargs -I {} gh issue close {} --comment "Closing due to 30+ days of inactivity. Feel free to reopen if still relevant."

# 给所有 needs-triage 加 milestone
gh issue list --label "status: needs-triage" --limit 100 --json number \
  | jq -r '.[].number' \
  | xargs -I {} gh issue edit {} --add-project "Backlog"

# 看 issue 流量趋势
gh api graphql -f query='{repository(owner:"processcrash",name:"xarch"){issues(states:OPEN){totalCount}}}'
```

---

## 10. 关键链接

- 📋 [Issue 列表](https://github.com/processcrash/xarch/issues)
- 💬 [Discussions](https://github.com/processcrash/xarch/discussions)
- 🔒 [SECURITY.md](SECURITY.md) — 私密披露流程
- 🤝 [CONTRIBUTING.md](CONTRIBUTING.md) — 完整贡献指南
- 📜 [CHANGELOG.md](CHANGELOG.md) — 变更日志
- 🏷️ [Labels](https://github.com/processcrash/xarch/labels) — 完整标签清单

---

**最后更新**：2026-07-01
**维护者**：@processcrash 团队 + 活跃贡献者
