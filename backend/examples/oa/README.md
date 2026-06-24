# xarch OA Example

A small Office Automation example that demonstrates a workflow / approval
engine on top of the **xarch** framework.

The point of the example is to show how a *generic*, JSON-driven approval
engine can be wired into two concrete business flows - leave requests
and expense reports - without dragging in a heavyweight BPM suite.

---

## What it shows

- A **generic workflow engine** (`WorkflowEngine`) that drives a
  business row through a graph of approval nodes and edges.
- Two business flows that share the engine:
  - **Leave requests** - applicant creates a draft, submits, manager
    approves, HR approves.
  - **Expense reports** - applicant creates a draft with line items,
    submits, manager approves, finance approves.
- An **approval record audit log** that captures every approver action
  (approve / reject / transfer) with optional comments.
- JSON-encoded detail columns (`attachments`, `items`, `definition`)
  showing how to keep a relational schema flat while still preserving
  structured data.
- `@Transactional` boundaries on multi-step writes (state transition +
  workflow pointer + approval record).
- `ServiceImpl` plus interface pattern, controllers decorated with
  `@XarchLog`.

## Tech stack

- Spring Boot 4.0 / JDK 25
- MyBatis-Flex
- MySQL 8
- xarch-core / xarch-db / xarch-web / xarch-cache

## Project layout

```
src/main/java/com/xarch/oa
├── OaApplication.java
├── entity/        # LeaveRequest, ExpenseReport, ExpenseItem, ApprovalRecord, Workflow
├── mapper/
├── service/
├── service/impl/
├── controller/
├── dto/
├── exception/
└── workflow/      # WorkflowEngine, WorkflowDefinition
```

## Database

Schema is provided in `src/main/resources/db/init.sql`. Run it once:

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

The schema includes:

| Table                  | Purpose                                                 |
| ---------------------- | ------------------------------------------------------- |
| `oa_leave_request`     | Leave requests with workflow state                      |
| `oa_expense_report`    | Expense reports with JSON line items                    |
| `oa_approval_record`   | Audit log of approver actions                           |
| `oa_workflow`          | JSON-driven workflow definitions                        |

Two seed workflows are inserted on first run - one for `LEAVE`, one
for `EXPENSE`. Both walk manager -> HR/finance and accept approve /
reject at every step.

## API quick start

Start the app on port **8082** (see `application.yml`):

```bash
./gradlew :examples:oa:bootRun
```

Create a leave request:

```bash
curl -X POST 'http://localhost:8082/api/leave-requests?userId=1' \
  -H 'Content-Type: application/json' \
  -d '{"type":"ANNUAL","startDate":1735689600000,"endDate":1736380800000,"reason":"Trip","attachments":[]}'
```

Submit it for approval:

```bash
curl -X PUT 'http://localhost:8082/api/leave-requests/1/submit?userId=1'
```

Approve as the manager:

```bash
curl -X PUT 'http://localhost:8082/api/leave-requests/1/act' \
  -H 'Content-Type: application/json' \
  -d '{"action":"APPROVE","approverId":101,"approverName":"Alice","comment":"ok"}'
```

Read the approval history:

```bash
curl 'http://localhost:8082/api/approvals/history?businessType=LEAVE&businessId=1'
```

## Tests

A pure-unit test for the workflow engine lives in
`src/test/java/com/xarch/oa/WorkflowEngineTest.java`. It avoids Spring:

```bash
./gradlew :examples:oa:test
```

## Notes

- The workflow graph is intentionally tiny (manager -> HR/finance).
  Extending it is a JSON edit, not a code change.
- The engine's "parallel" semantics are implemented as "all approvers
  on the current node must approve before the engine advances". This
  matches the most common case in real organisations.
- No security wiring. The `userId` / `approverId` query parameters are
  placeholders for the `LoginUser` resolution that lives in the larger
  admin module.
