# Frequently Asked Questions

> Common questions about xarch — design rationale, integration recipes,
> performance tuning, and troubleshooting.

If your question is not here, open a GitHub Discussion or read the
relevant deep-dive:

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [MCP_GUIDE.md](MCP_GUIDE.md)
- [DEPLOYMENT.md](DEPLOYMENT.md)
- [API_REFERENCE.md](API_REFERENCE.md)

---

## Table of Contents

1. [Design Rationale](#design-rationale)
2. [Module & Extension Recipes](#module--extension-recipes)
3. [Integration](#integration)
4. [Operations](#operations)
5. [Common Errors](#common-errors)

---

## Design Rationale

### Why JDK 25?

Records, sealed types, and virtual threads let us delete thousands of
lines of boilerplate and ship a more responsive runtime. Virtual
threads in particular mean most controllers can run blocking JDBC /
Redis code without a thread-pool sizing crisis. See
[ARCHITECTURE.md — Concurrency Model](ARCHITECTURE.md#concurrency-model)
for details.

### Why Vue 3 not React?

- **Element Plus** is the most mature enterprise component library
  for Vue; the React equivalent ecosystem is more fragmented.
- **Composition API + `<script setup>`** gives us React-like DX
  inside Vue's template ecosystem.
- The team's experience with `sz-admin` / RuoYi-style admin shells
  is Vue-based; switching to React would force a full rewrite.
- Vue's bundle is ~30% smaller than a comparable React + UI lib
  bundle, which matters for low-bandwidth regions.

### Why MyBatis-Flex instead of MyBatis-Plus?

| Reason | Detail |
|--------|--------|
| Native paginator | No plugin classpath gotchas |
| Fluent API | Composes with `record` and sealed types |
| Multi-dialect | First-class Postgres/MySQL/SQL Server |
| Compile-time safety | Less reflection magic |

Migration guide: see `docs/wiki/DEVELOPMENT.md`.

### Why Sa-Token instead of Spring Security?

Spring Security is excellent but verbose. Sa-Token gives the same
guarantees (JWT, session, RBAC) with one annotation per
controller — `@SaCheckPermission("user:add")` — which is what most
business code wants.

### Why Spring Cloud Gateway instead of Zuul / Nginx-only?

- Dynamic route reload from Nacos — no gateway restart on deploy.
- Java filter ecosystem — we can reuse the same `XarchLog` /
  `RateLimitFilter` filters the application uses.
- Native SSE / WebSocket support for streaming MCP endpoints.

### Why is the example app in `xarch-example`?

To prove the starters work end-to-end and to give new users a
copy-paste starting point. Production deployments typically fork
`xarch-example` and remove the modules they don't need.

---

## Module & Extension Recipes

### How to add a new module?

1. Create `xarch-example-myfeature/` as a new module under
   `backend/xarch-example/`.
2. Add it to `settings.gradle.kts` and the `xarch-example` build
   file.
3. Follow the canonical package layout (`controller`, `service`,
   `mapper`, `entity`, `dto`, `vo`).
4. Add unit tests covering at least the happy path and the
   unauthorized path.
5. Register the new controller paths in the front-end `api/` and
   `router/` files.

### How to add a new starter?

```kotlin
// backend/xarch-myfeature-spring-boot-starter/build.gradle.kts
plugins {
    java
    `java-gradle-plugin`
    id("org.springframework.boot") version "4.0.0"
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
```

Provide:

- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- `XarchMyFeatureProperties` with `@ConfigurationProperties`
- `XarchMyFeatureAutoConfiguration` with `@AutoConfiguration`

### How to add a new MCP tool?

**Java (Spring Boot):**

```java
@McpServer(name = "my-tool", version = "0.1.0")
@Component
public class MyTool implements McpServer {

    @Tool(name = "echo", description = "Echoes input")
    public String echo(@ToolArg("text") String text) {
        return text;
    }
}
```

**Node (TypeScript):**

```typescript
server.setRequestHandler("tools/list", async () => ({
  tools: [{
    name: "echo",
    description: "Echoes input",
    inputSchema: {
      type: "object",
      properties: { text: { type: "string" } },
      required: ["text"],
    },
  }],
}));

server.setRequestHandler("tools/call", async ({ params }) => {
  if (params.name === "echo") {
    return { content: [{ type: "text", text: params.arguments.text }] };
  }
});
```

### How to plug a new vector backend?

Implement `VectorStore` from `xarch-mcp-vector`:

```java
public class QdrantVectorStore implements VectorStore {
    @Override public void upsert(String collection, Vector v) { /* ... */ }
    @Override public List<Match> search(String collection, float[] q, int k) { /* ... */ }
}
```

Register it as a `@Bean` and remove the default in-process store.

### How to add a new storage backend?

Implement `StorageStrategy` and add a record to
`sys_storage_config` so the file manager can route uploads through
it.

---

## Integration

### How to integrate with existing SSO?

Sa-Token supports pluggable session resolvers. Override the JWT
resolver to validate your existing SSO token:

```java
@Configuration
public class SsoAuthConfig {
    @Bean
    public SaTokenResolver ssoResolver(SsoClient sso) {
        return new SaTokenResolver() {
            @Override public SaToken resolve(String token) {
                SsoSession s = sso.verify(token);
                return new SaToken(s.userId(), s.permissions());
            }
        };
    }
}
```

### How to enable HTTPS locally?

Generate a self-signed cert:

```bash
keytool -genkeypair -alias xarch -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore xarch.p12 -validity 365 \
  -storepass changeit -dname "CN=localhost"
```

Then in `application.yml`:

```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:xarch.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: xarch
```

For production, terminate TLS at the ingress and pass
`X-Forwarded-Proto` through to the app.

### How to switch from PostgreSQL to MySQL?

1. Set `spring.datasource.driver-class-name` to
   `com.mysql.cj.jdbc.Driver`.
2. Update the URL, username, password.
3. Replace `docs/db/init-postgresql.sql` with `init-mysql.sql`.
4. Adjust column types where Postgres-specific (`JSONB` → `JSON`,
   `UUID` → `CHAR(36)`).
5. Verify `MyBatis-Flex` dialect detection picks up MySQL (check
   `/actuator/health/db`).

### How to use MyBatis-Flex with an existing MyBatis-Plus codebase?

Both can coexist — they share the same DataSource. Add the Flex
dependency and start writing new mappers; migrate old mappers one at
a time using `BaseMapper` as the bridge. See the migration guide in
`docs/wiki/DEVELOPMENT.md`.

### How to call MCP from a Java service?

```java
@McpClient(name = "database-mcp")
public interface DatabaseMcpClient {
    @McpTool("query_execute")
    QueryResult query(@McpArg("sql") String sql, @McpArg("params") List<Object> params);
}
```

Inject it as a Spring bean. `xarch-cloud-starter-mcp` resolves the
client against the registered Nacos MCP service.

---

## Operations

### Performance tuning

| Lever | Recommendation |
|-------|----------------|
| HikariCP | `maximum-pool-size = (cores * 2) + spindles` |
| Tomcat | `server.tomcat.threads.max = 200` default; raise if blocking I/O dominates |
| JVM | `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0` |
| Cache TTL | Match to actual user-perceived freshness |
| Pagination | Never return > 1000 rows; use cursor pagination for exports |

Enable virtual threads on Tomcat:

```yaml
spring.threads.virtual.enabled: true
```

### Memory tuning

```bash
java -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -XX:MaxGCPauseMillis=200 \
  -jar xarch-example.jar
```

Default container limits are 512Mi / 1Gi for most components —
raise for `xarch-backend` and `xarch-cloud-admin-server`.

### Why are the controllers returning `0000` even on errors?

The global exception handler converts every thrown exception into
an `ApiResult` with a non-`0000` code. If you see `0000` on an
error path, a service is likely returning a successful envelope
without actually doing the work — check the service logs.

### How to enable debug logging?

```yaml
logging:
  level:
    com.xarch: DEBUG
    com.baomidou.mybatisplus: TRACE    # if MyBatis-Plus still present
```

Restart the pod. Logs go to stdout (JSON) which Promtail / Alloy
ship to Loki.

### How to enable slow query log?

Druid exposes slow query metrics automatically:

```yaml
spring.datasource.druid.filter.stat.slow-sql-millis: 1000
spring.datasource.druid.filter.stat.log-slow-sql: true
```

### How to scale Postgres?

- **Vertical**: increase CPU / memory on the existing instance.
- **Read replicas**: enable a read-only datasource, route
  `@Transactional(readOnly = true)` methods through it.
- **Sharding**: out of scope for xarch; use ShardingSphere or
  Citus.

### How to back up and restore?

See [DEPLOYMENT.md — Backup & Disaster Recovery](DEPLOYMENT.md#backup--disaster-recovery).

---

## Common Errors

### `BeanCreationException: No qualifying bean of type 'XarchProperties'`

`xarch-core-spring-boot-starter` is missing. Add the dependency:

```kotlin
implementation("com.xarch:xarch-core-spring-boot-starter:1.0.0")
```

### `SaTokenException: token is null`

The request lacks the `Authorization: Bearer <token>` header, or
the header name is misconfigured. Verify the gateway forwards it.

### `Communications link failure` to MySQL

- Check `bind-address = 0.0.0.0` (not `127.0.0.1`) on the server.
- Verify security group / firewall allows port 3306 from the pod
  CIDR.
- Check `wait_timeout` vs your connection-pool idle timeout.

### `FlywayException: Found non-empty schema(s) without metadata table`

The database has tables but no Flyway history. Either run
`flyway baseline -baselineVersion=1` or drop the tables and let
the init script recreate them.

### `OutOfMemoryError: Metaspace`

Reduce the number of MCP clients or use shared bytecode caches.
In K8s, set `-XX:MaxMetaspaceSize=512m` as a starting point.

### Frontend: `Failed to fetch dynamically imported module`

The Vue bundle hash changed after a deploy; users have stale
chunks. Mitigations:

- Enable `Cache-Control: immutable` on `*.js` / `*.css` assets.
- Configure Nginx to return `index.html` on 404 for SPA routes.

### MCP server: `Tool 'xxx' not found`

The client and server are out of sync. Restart the client so it
re-reads `tools/list`, or upgrade both to the same MCP protocol
version.

### `504 Gateway Timeout` under load

Either upstream is slow (check `/actuator/metrics/http.server.requests`)
or the gateway timeout is too aggressive. Increase:

```yaml
spring.cloud.gateway.httpclient.response-timeout: 30s
```

### `Cannot resolve org.springframework.cloud:spring-cloud-starter-alibaba-nacos-config`

You forgot to add the Spring Cloud Alibaba BOM. Pin it in your
version catalog:

```toml
[libraries]
spring-cloud-alibaba-nacos = { module = "com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config", version = "2025.0.0.0" }
```

---

## Still Stuck?

- Search [existing issues](https://example.com/xarch/issues).
- Open a new issue with the *Question* template.
- Reach the maintainers via `maintainers@xarch.example`.
- See [CONTRIBUTING.md](../CONTRIBUTING.md) for how to propose a
  fix once you've identified it.