# xarch CRM Example

A small customer-relationship-management example that demonstrates how the
**xarch** framework wires up a sales-pipeline domain: customers, contacts,
opportunities, follow-ups, contracts and a tiny sales-analytics layer.

The module is intentionally narrow: a single Spring Boot app, a single
database schema, and a small REST surface. It exists to show idiomatic
patterns - not to ship a production CRM.

---

## Project Brief (项目简介)

A lightweight CRM for sales pipeline management. The five-domain model -
**Customer -> Contact -> Opportunity -> FollowUp -> Contract** - covers the
common sales funnel end-to-end. A standalone analytics surface exposes
funnel snapshots, conversion rates, weighted revenue forecasts and
top-customers lists, all derived on the fly from the same tables.

---

## Features (功能特性)

- Multi-table domain modelling with MyBatis-Flex (`@Table`, `@Id`,
  `@Column` with auto-managed timestamps).
- Soft delete through MyBatis-Flex's `@Column(isLogicDelete = true)`.
- Funnel transitions with derived status / probability
  (`QUALIFICATION -> NEEDS_ANALYSIS -> PROPOSAL -> NEGOTIATION -> WON / LOST`).
- One primary contact per customer enforced at the service layer.
- Customer name + phone uniqueness check at create time.
- Contract number uniqueness check at create time.
- Soft-delete audit fields (`is_deleted`, `create_time`, `update_time`)
  on every entity.
- Service interfaces with `@Transactional(rollbackFor = Exception.class)`
  on all write paths.
- Controllers that return the framework's `ApiResult` / `PageResult`
  and are decorated with `@XarchLog` for audit logging.
- A small sales-analytics surface that aggregates funnel / conversion /
  forecast / top-customers without an OLAP cube.

## Tech stack

- Spring Boot 4.0 / JDK 25
- MyBatis-Flex
- MySQL 8
- xarch-core / xarch-db / xarch-web / xarch-cache

---

## Project layout (模块结构)

```
src/main/java/com/xarch/crm
├── CrmApplication.java
├── entity/        # Customer, Contact, Opportunity, FollowUp, Contract
├── mapper/        # MyBatis-Flex BaseMapper + custom queries
├── service/       # Business interfaces
├── service/impl/  # Business implementations
├── controller/    # REST endpoints
├── dto/           # Records for create / update / query payloads
└── exception/     # CrmException
```

---

## Data model (数据模型)

| Entity      | Table              | Purpose                                                    |
| ----------- | ------------------ | ---------------------------------------------------------- |
| Customer    | `crm_customer`     | Account / lead with lifecycle (LEAD/PROSPECT/CUSTOMER/LOST) |
| Contact     | `crm_contact`      | People at a customer; one of them flagged primary         |
| Opportunity | `crm_opportunity`  | Sales deal in a funnel stage with probability              |
| FollowUp    | `crm_follow_up`    | Interaction log stamped on a customer (and optional opp)   |
| Contract    | `crm_contract`     | Signed contract with status lifecycle and unique number    |

All timestamp columns are `BIGINT` epoch milliseconds.

---

## API endpoints (API 文档)

All endpoints are prefixed with `/api`. Responses are wrapped in
`ApiResult<T>`; paginated responses use `PageResult<T>`.

### Customer `/api/customers`

| Method | Path                | Description                                |
| ------ | ------------------- | ------------------------------------------ |
| GET    | `/`                 | Paginated search (`name`, `type`, `level`, `ownerId`, `pageNum`, `pageSize`) |
| GET    | `/{id}`             | Get a single customer                      |
| POST   | `/`                 | Create a customer                          |
| PUT    | `/{id}`             | Update a customer                          |
| DELETE | `/{id}`             | Soft delete                                |
| POST   | `/{id}/assign`      | Reassign owner (body `{ownerId}`)          |
| POST   | `/{id}/convert`     | Promote to CUSTOMER                        |
| POST   | `/{id}/lose`        | Mark LOST (body `{reason}`)                |

### Contact `/api/contacts`

| Method | Path            | Description                                            |
| ------ | --------------- | ------------------------------------------------------ |
| GET    | `/?customerId=` | List contacts for a customer                           |
| GET    | `/{id}`         | Get a single contact                                   |
| POST   | `/`             | Create                                                 |
| PUT    | `/{id}`         | Update                                                 |
| DELETE | `/{id}`         | Soft delete                                            |
| POST   | `/{id}/primary` | Promote to primary contact for the same customer       |

### Opportunity `/api/opportunities`

| Method | Path                            | Description                            |
| ------ | ------------------------------- | -------------------------------------- |
| GET    | `/`                             | Paginated search                       |
| GET    | `/{id}`                         | Detail                                 |
| POST   | `/`                             | Create                                 |
| PUT    | `/{id}`                         | Update                                 |
| DELETE | `/{id}`                         | Soft delete                            |
| POST   | `/{id}/stage`                   | Change stage (body `{stage}`)          |
| GET    | `/by-customer/{customerId}`     | All opportunities for a customer       |

### FollowUp `/api/follow-ups`

| Method | Path                                    | Description                            |
| ------ | --------------------------------------- | -------------------------------------- |
| GET    | `/?customerId=&opportunityId=`          | List by customer or opportunity        |
| GET    | `/{id}`                                 | Detail                                 |
| POST   | `/`                                     | Create                                 |
| PUT    | `/{id}`                                 | Update                                 |
| DELETE | `/{id}`                                 | Soft delete                            |
| GET    | `/schedule?date=<epochMillis>`          | Find due follow-ups                    |

### Contract `/api/contracts`

| Method | Path                          | Description                            |
| ------ | ----------------------------- | -------------------------------------- |
| GET    | `/`                           | Paginated search                       |
| GET    | `/{id}`                       | Detail                                 |
| POST   | `/`                           | Create                                 |
| PUT    | `/{id}`                       | Update                                 |
| DELETE | `/{id}`                       | Soft delete                            |
| POST   | `/{id}/terminate`             | Set status=TERMINATED                  |

### SalesAnalytics `/api/analytics`

| Method | Path                              | Description                                |
| ------ | --------------------------------- | ------------------------------------------ |
| GET    | `/funnel`                         | `{stage -> (count, amount)}`               |
| GET    | `/conversion`                     | Stage-to-stage conversion rates            |
| GET    | `/forecast`                       | Weighted revenue forecast                  |
| GET    | `/pipeline`                       | Pipeline summary (same shape as funnel)    |
| GET    | `/top-customers?limit=10`         | Top customers by ACTIVE contract value     |

---

## Database initialisation (数据库初始化)

Schema is provided in `src/main/resources/db/init.sql`. Run it once:

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

The schema creates the `xarch_crm` database with five tables and the
indexes needed by the API (`customer.name`, `opportunity.stage`,
`contract.contract_no` UNIQUE, etc.).

---

## Running the app (启动方式)

The app listens on port **8083** (see `application.yml`).

```bash
./gradlew :examples:crm:bootRun
```

A quick smoke test once it is up:

```bash
# create a lead
curl -X POST 'http://localhost:8083/api/customers' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Acme Corp","type":"LEAD","contactName":"Jane","contactPhone":"13800000000","level":"A"}'

# funnel snapshot
curl 'http://localhost:8083/api/analytics/funnel'
```

---

## Tests

A pure-unit test for the analytics service lives in
`src/test/java/com/xarch/crm/SalesAnalyticsServiceTest.java`. It avoids
Spring so it can run anywhere; the mappers are replaced with Mockito
stubs:

```bash
./gradlew :examples:crm:test
```

---

## Notes

- The example deliberately has **no** security wiring. The `ownerId` /
  `userId` fields are placeholders for `LoginUser`-style resolution that
  lives in the larger admin module.
- Soft delete is wired on every table via MyBatis-Flex's logic-delete
  configuration (`is_deleted` column).
- The forecast is the simple weighted sum `amount * probability / 100`
  over all OPEN opportunities - no discounting, no time weighting.

---

## Comparison with `xarch-example` (与 xarch-example 的对比)

| Area             | `xarch-example` (CMS)                 | `xarch-crm`                                       |
| ---------------- | ------------------------------------- | ------------------------------------------------- |
| Domain           | Articles, categories, tags, comments  | Customers, contacts, opportunities, contracts     |
| Pattern          | Single dominant aggregate (Article)   | Multi-aggregate sales pipeline + analytics        |
| Lifecycle states | DRAFT / PUBLISHED / ARCHIVED          | Customer LOST, Opportunity WON/LOST, Contract     |
| Cross-aggregate  | Tag join (ArticleTag)                 | Stage transitions (WON -> status/probability)     |
| Analytics        | None                                  | Funnel, conversion, forecast, top-customers       |
| Uniqueness rules | None at the service layer             | (name + phone) and (contract_no) enforced         |
| Soft delete      | Article + Comment                     | All five entities                                 |