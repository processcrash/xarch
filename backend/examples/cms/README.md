# xarch CMS Example

A content management example that demonstrates how the **xarch** framework
fits together around a single, coherent domain: articles, categories, tags
and threaded comments.

The module is intentionally narrow: a single Spring Boot app, a single
database schema, and a small REST surface. It exists to show idiomatic
patterns - not to ship a production CMS.

---

## What it shows

- Multi-table entity modelling with MyBatis-Flex (`@Table`, `@Id`,
  `@Column` with auto-managed timestamps).
- Soft delete through MyBatis-Flex's `@Column(isLogicDelete = true)`.
- Many-to-many relations via an explicit join entity (`ArticleTag`).
- Tree assembly in plain Java (categories) without pulling in a recursive
  CTE or a third-party tree library.
- Service interfaces with `ServiceImpl`-style implementations, plus
  `@Transactional` boundaries on multi-step writes.
- Controllers that return the framework's `ApiResult` / `PageResult` and
  are decorated with `@XarchLog` for audit logging.
- A small MyBatis XML mapper for the queries that the QueryWrapper DSL
  cannot express cleanly (atomic counter increments, multi-table joins).

## Tech stack

- Spring Boot 4.0 / JDK 25
- MyBatis-Flex
- MySQL 8
- xarch-core / xarch-db / xarch-web / xarch-cache

## Project layout

```
src/main/java/com/xarch/cms
├── CmsApplication.java
├── entity/        # Article, Category, Tag, ArticleTag, Comment
├── mapper/        # MyBatis-Flex BaseMapper + custom queries
├── service/       # Business interfaces
├── service/impl/  # Business implementations
├── controller/    # REST endpoints
├── dto/           # Records for create / update / query payloads
└── exception/     # CmsException
```

## Database

Schema is provided in `src/main/resources/db/init.sql`. Run it once:

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

The schema includes:

| Table          | Purpose                                                   |
| -------------- | --------------------------------------------------------- |
| `cms_article`  | Articles with DRAFT/PUBLISHED/ARCHIVED lifecycle         |
| `cms_category` | Tree of categories (parent_id, sort_order)                |
| `cms_tag`      | Flat list of tags                                         |
| `cms_article_tag` | Join table                                          |
| `cms_comment`  | Comments with one level of nesting via parent_id          |

All timestamp columns are `BIGINT` epoch milliseconds. Indexes are added
on the columns the API filters and sorts by (status, category_id,
author_id, create_time, is_deleted, etc.).

## API quick start

Start the app on port **8081** (see `application.yml`):

```bash
./gradlew :examples:cms:bootRun
```

List published articles:

```bash
curl 'http://localhost:8081/api/articles?status=PUBLISHED&pageNum=1&pageSize=10'
```

Create a draft:

```bash
curl -X POST 'http://localhost:8081/api/articles?authorId=1' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hello","content":"Body","summary":"Hi","categoryId":1,"tagIds":[1,2]}'
```

Publish it:

```bash
curl -X PUT 'http://localhost:8081/api/articles/1/publish'
```

Get a category tree:

```bash
curl 'http://localhost:8081/api/categories/tree'
```

## Tests

A pure-unit test for the article service lives in
`src/test/java/com/xarch/cms/ArticleServiceTest.java`. It avoids Spring so
it can run in any environment:

```bash
./gradlew :examples:cms:test
```

## Notes

- The example deliberately has **no** security wiring. The `authorId` /
  `userId` query parameters are placeholders for `LoginUser`-style
  resolution that lives in the larger admin module.
- The article body is stored as raw `MEDIUMTEXT`; no rendering pipeline
  is included.
- Soft delete is wired on `cms_article` and `cms_comment`. Categories and
  tags are hard-deleted because the tree rebuild re-attaches children
  manually first.
