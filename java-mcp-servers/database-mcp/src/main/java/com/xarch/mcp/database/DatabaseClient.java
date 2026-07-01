package com.xarch.mcp.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fake {@code JDBC}-style client used by the database MCP server.
 *
 * <p>Deliberately <strong>driver-free</strong>: there is no real database
 * connection. The shape of the API (testConnection / query / update /
 * listTables / describeTable / listIndexes / getSchema) matches what a
 * thin wrapper around a JDBC {@code Connection} would look like, so the
 * tools can stay the same when this class is swapped for a real driver.
 *
 * <p>Behaviour is deterministic for known tables and tables that match the
 * convention {@code CREATE TABLE users/orders/products/...} so tests have
 * something meaningful to assert against. Unknown tables still return
 * sane defaults — empty rows, generic column metadata — rather than
 * throwing, because the goal is to keep the MCP server wireable from a
 * fresh checkout.
 *
 * <h2>Swapping in a real JDBC driver</h2>
 * <ol>
 *   <li>Add the driver to {@code build.gradle.kts} (mysql-connector-j,
 *       postgresql, etc.)</li>
 *   <li>Open a {@code Connection} in {@link #testConnection()} and cache
 *       it on a field</li>
 *   <li>Replace the bodies of {@link #query}, {@link #update},
 *       {@link #listTables}, {@link #describeTable}, {@link #listIndexes}
 *       and {@link #getSchema} with real JDBC calls</li>
 * </ol>
 * Nothing else in the codebase needs to change — the
 * {@link tools.DatabaseTools} only depends on this interface shape.
 */
public class DatabaseClient {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClient.class);

    private final DatabaseConfig config;
    private boolean connected = false;

    public DatabaseClient(DatabaseConfig config) {
        this.config = config;
    }

    /**
     * Pretend to open a connection. Returns {@code true} when the config
     * is "configured enough" to be useful — host and database set.
     */
    public synchronized boolean testConnection() {
        if (!config.isConfigured()) {
            log.warn("testConnection called with unconfigured DatabaseConfig");
            connected = false;
            return false;
        }
        connected = true;
        log.info("fake JDBC connection established to {}/{}",
                config.getHost() + ":" + config.getPort(), config.getDatabase());
        return true;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Execute a SELECT-style query.
     *
     * <p>Returns a small set of mock rows. The shape is inferred from the
     * {@code FROM} clause when possible (e.g. {@code SELECT * FROM users}
     * yields rows with user-shaped columns). Unrecognized statements
     * return a generic two-column row set so callers always have
     * something iterable.
     *
     * @param sql    the SQL string (only the keyword/table hint matters)
     * @param params bound parameters (ignored by the fake, accepted for
     *               API compatibility)
     */
    public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
        requireConnected();
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("query requires a non-empty SQL string");
        }
        String lower = sql.toLowerCase();
        if (lower.contains("users"))      return sampleRows("users", 3);
        if (lower.contains("orders"))     return sampleRows("orders", 2);
        if (lower.contains("products"))   return sampleRows("products", 2);
        if (lower.contains("audit_log"))  return sampleRows("audit_log", 3);
        if (lower.contains("files"))      return sampleRows("files", 2);
        // Generic fallback
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("value", "row-" + i);
            rows.add(row);
        }
        return rows;
    }

    /**
     * Pretend to run an INSERT/UPDATE/DELETE.
     *
     * @return the number of rows "affected" — a deterministic 1-5 value
     *         derived from the SQL hash so tests are stable
     */
    public int update(String sql, Map<String, Object> params) {
        requireConnected();
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("update requires a non-empty SQL string");
        }
        // Deterministic per-statement: hash → 1..5
        int hash = Math.abs(sql.hashCode());
        int affected = (hash % 5) + 1;
        log.info("fake UPDATE affected {} rows for: {}", affected, abbreviate(sql));
        return affected;
    }

    /** Names of all "tables" the fake knows about. */
    public List<String> listTables() {
        requireConnected();
        return new ArrayList<>(List.of("users", "orders", "products", "audit_log", "files"));
    }

    /**
     * Column metadata for a known table. The order matches the canonical
     * {@code CREATE TABLE} statements returned by {@link #getSchema()}.
     */
    public List<Map<String, Object>> describeTable(String name) {
        requireConnected();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("describeTable requires a non-empty table name");
        }
        return switch (name.toLowerCase()) {
            case "users"     -> usersColumns();
            case "orders"    -> ordersColumns();
            case "products"  -> productsColumns();
            case "audit_log" -> auditLogColumns();
            case "files"     -> filesColumns();
            default -> {
                log.warn("describeTable called for unknown table: {}", name);
                yield genericColumns();
            }
        };
    }

    /** Indexes for one table, or all tables if {@code tableName} is {@code null}/blank. */
    public List<Map<String, Object>> listIndexes(String tableName) {
        requireConnected();
        List<Map<String, Object>> out = new ArrayList<>();
        if (tableName == null || tableName.isBlank()) {
            for (String t : listTables()) out.addAll(indexesFor(t));
            return out;
        }
        return indexesFor(tableName.toLowerCase());
    }

    /** DDL statements for the known fake tables. */
    public List<String> getSchema() {
        requireConnected();
        return new ArrayList<>(List.of(
                "CREATE TABLE users ("
                        + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                        + "username VARCHAR(64) NOT NULL UNIQUE, "
                        + "email VARCHAR(255) NOT NULL, "
                        + "password_hash VARCHAR(255) NOT NULL, "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ");",
                "CREATE TABLE orders ("
                        + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                        + "user_id BIGINT NOT NULL, "
                        + "total DECIMAL(12,2) NOT NULL, "
                        + "status VARCHAR(32) NOT NULL DEFAULT 'pending', "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY (user_id) REFERENCES users(id)"
                        + ");",
                "CREATE TABLE products ("
                        + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                        + "sku VARCHAR(64) NOT NULL UNIQUE, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "price DECIMAL(10,2) NOT NULL, "
                        + "stock INT NOT NULL DEFAULT 0, "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ");",
                "CREATE TABLE audit_log ("
                        + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                        + "actor VARCHAR(64) NOT NULL, "
                        + "action VARCHAR(64) NOT NULL, "
                        + "entity VARCHAR(64) NOT NULL, "
                        + "entity_id BIGINT, "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ");",
                "CREATE TABLE files ("
                        + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                        + "owner_id BIGINT NOT NULL, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "content_type VARCHAR(128), "
                        + "size_bytes BIGINT NOT NULL DEFAULT 0, "
                        + "storage_key VARCHAR(512) NOT NULL, "
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY (owner_id) REFERENCES users(id)"
                        + ");"
        ));
    }

    // ---- helpers ------------------------------------------------------

    private void requireConnected() {
        if (!connected) {
            throw new IllegalStateException(
                    "Database not configured. Call the 'configure' tool first.");
        }
    }

    private static String abbreviate(String s) {
        return s.length() <= 80 ? s : s.substring(0, 77) + "...";
    }

    private List<Map<String, Object>> sampleRows(String table, int n) {
        return switch (table) {
            case "users" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                String[][] data = {
                        {"1", "alice",  "alice@example.com",  "2024-01-01T00:00:00Z"},
                        {"2", "bob",    "bob@example.com",    "2024-01-02T00:00:00Z"},
                        {"3", "carol",  "carol@example.com",  "2024-01-03T00:00:00Z"}
                };
                for (int i = 0; i < Math.min(n, data.length); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", Long.parseLong(data[i][0]));
                    row.put("username", data[i][1]);
                    row.put("email", data[i][2]);
                    row.put("created_at", data[i][3]);
                    rows.add(row);
                }
                yield rows;
            }
            case "orders" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                Object[][] data = {
                        {1L, 1L,  99.50, "paid"},
                        {2L, 2L, 245.00, "pending"}
                };
                for (int i = 0; i < Math.min(n, data.length); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", data[i][0]);
                    row.put("user_id", data[i][1]);
                    row.put("total", data[i][2]);
                    row.put("status", data[i][3]);
                    rows.add(row);
                }
                yield rows;
            }
            case "products" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                Object[][] data = {
                        {1L, "SKU-001", "Widget",  9.99, 100},
                        {2L, "SKU-002", "Gadget", 19.99,  50}
                };
                for (int i = 0; i < Math.min(n, data.length); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", data[i][0]);
                    row.put("sku", data[i][1]);
                    row.put("name", data[i][2]);
                    row.put("price", data[i][3]);
                    row.put("stock", data[i][4]);
                    rows.add(row);
                }
                yield rows;
            }
            case "audit_log" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                String[][] data = {
                        {"1", "alice", "CREATE", "users", "1"},
                        {"2", "bob",   "UPDATE", "orders", "2"},
                        {"3", "carol", "DELETE", "products", "5"}
                };
                for (int i = 0; i < Math.min(n, data.length); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", Long.parseLong(data[i][0]));
                    row.put("actor", data[i][1]);
                    row.put("action", data[i][2]);
                    row.put("entity", data[i][3]);
                    row.put("entity_id", Long.parseLong(data[i][4]));
                    row.put("created_at", "2024-02-0" + (i + 1) + "T00:00:00Z");
                    rows.add(row);
                }
                yield rows;
            }
            case "files" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                Object[][] data = {
                        {1L, 1L, "avatar.png",   "image/png", 2048L,  "s3://bucket/avatar-1.png"},
                        {2L, 2L, "invoice.pdf",  "application/pdf", 51200L, "s3://bucket/invoice-2.pdf"}
                };
                for (int i = 0; i < Math.min(n, data.length); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", data[i][0]);
                    row.put("owner_id", data[i][1]);
                    row.put("name", data[i][2]);
                    row.put("content_type", data[i][3]);
                    row.put("size_bytes", data[i][4]);
                    row.put("storage_key", data[i][5]);
                    rows.add(row);
                }
                yield rows;
            }
            default -> {
                // Should not happen — query() only forwards known names.
                List<Map<String, Object>> rows = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", (long) i);
                    row.put("value", table + "-" + i);
                    rows.add(row);
                }
                yield rows;
            }
        };
    }

    private List<Map<String, Object>> usersColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",            "BIGINT",    false, null,                                "Primary key"));
        cols.add(col("username",      "VARCHAR(64)", false, null,                             "Unique username"));
        cols.add(col("email",         "VARCHAR(255)", false, null,                            "Login + contact email"));
        cols.add(col("password_hash", "VARCHAR(255)", false, null,                            "Bcrypt hash, never plaintext"));
        cols.add(col("created_at",    "TIMESTAMP",   false, "CURRENT_TIMESTAMP",               "Row creation time"));
        cols.add(col("updated_at",    "TIMESTAMP",   false, "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
                "Last update time"));
        return cols;
    }

    private List<Map<String, Object>> ordersColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",         "BIGINT",        false, null,                  "Primary key"));
        cols.add(col("user_id",    "BIGINT",        false, null,                  "FK -> users.id"));
        cols.add(col("total",      "DECIMAL(12,2)", false, null,                  "Order total in account currency"));
        cols.add(col("status",     "VARCHAR(32)",   false, "'pending'",            "pending|paid|cancelled|refunded"));
        cols.add(col("created_at", "TIMESTAMP",     false, "CURRENT_TIMESTAMP",   "Order creation time"));
        return cols;
    }

    private List<Map<String, Object>> productsColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",         "BIGINT",        false, null,                  "Primary key"));
        cols.add(col("sku",        "VARCHAR(64)",   false, null,                  "Stock keeping unit"));
        cols.add(col("name",       "VARCHAR(255)",  false, null,                  "Display name"));
        cols.add(col("price",      "DECIMAL(10,2)", false, null,                  "Unit price"));
        cols.add(col("stock",      "INT",           false, "0",                   "Units in stock"));
        cols.add(col("created_at", "TIMESTAMP",     false, "CURRENT_TIMESTAMP",   "Listing time"));
        return cols;
    }

    private List<Map<String, Object>> auditLogColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",         "BIGINT",      false, null,                "Primary key"));
        cols.add(col("actor",      "VARCHAR(64)", false, null,                "User/service that acted"));
        cols.add(col("action",     "VARCHAR(64)", false, null,                "CREATE|UPDATE|DELETE|..."));
        cols.add(col("entity",     "VARCHAR(64)", false, null,                "Affected table"));
        cols.add(col("entity_id",  "BIGINT",      true,  null,                "Affected row, may be null"));
        cols.add(col("created_at", "TIMESTAMP",   false, "CURRENT_TIMESTAMP", "When the action happened"));
        return cols;
    }

    private List<Map<String, Object>> filesColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",           "BIGINT",       false, null,                "Primary key"));
        cols.add(col("owner_id",     "BIGINT",       false, null,                "FK -> users.id"));
        cols.add(col("name",         "VARCHAR(255)", false, null,                "Original file name"));
        cols.add(col("content_type", "VARCHAR(128)", true,  null,                "MIME type"));
        cols.add(col("size_bytes",   "BIGINT",       false, "0",                 "File size"));
        cols.add(col("storage_key",  "VARCHAR(512)", false, null,                "Backend storage URI"));
        cols.add(col("created_at",   "TIMESTAMP",    false, "CURRENT_TIMESTAMP", "Upload time"));
        return cols;
    }

    private List<Map<String, Object>> genericColumns() {
        List<Map<String, Object>> cols = new ArrayList<>();
        cols.add(col("id",   "BIGINT",    false, null, "Primary key (guessed)"));
        cols.add(col("name", "VARCHAR",   true,  null, "Name (guessed)"));
        return cols;
    }

    private static Map<String, Object> col(String name, String type, boolean nullable,
                                            Object defaultValue, String comment) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("type", type);
        m.put("nullable", nullable);
        m.put("default", defaultValue);
        m.put("comment", comment);
        return m;
    }

    private List<Map<String, Object>> indexesFor(String table) {
        return switch (table) {
            case "users" -> List.of(
                    index(table, "PRIMARY", "id", true,  "BTREE"),
                    index(table, "uk_users_username", "username", false, "BTREE"),
                    index(table, "idx_users_email", "email", false, "BTREE")
            );
            case "orders" -> List.of(
                    index(table, "PRIMARY", "id", true,  "BTREE"),
                    index(table, "idx_orders_user_id", "user_id", false, "BTREE"),
                    index(table, "idx_orders_status_created", "status, created_at", false, "BTREE")
            );
            case "products" -> List.of(
                    index(table, "PRIMARY", "id", true,  "BTREE"),
                    index(table, "uk_products_sku", "sku", false, "BTREE")
            );
            case "audit_log" -> List.of(
                    index(table, "PRIMARY", "id", true,  "BTREE"),
                    index(table, "idx_audit_actor_created", "actor, created_at", false, "BTREE"),
                    index(table, "idx_audit_entity", "entity, entity_id", false, "BTREE")
            );
            case "files" -> List.of(
                    index(table, "PRIMARY", "id", true,  "BTREE"),
                    index(table, "idx_files_owner_id", "owner_id", false, "BTREE")
            );
            default -> List.of(index(table, "PRIMARY", "id", true, "BTREE"));
        };
    }

    private static Map<String, Object> index(String table, String name, String columns,
                                             boolean unique, String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("table", table);
        m.put("name", name);
        m.put("columns", columns);
        m.put("unique", unique);
        m.put("type", type);
        return m;
    }

    // Suppress unused-warning on ThreadLocalRandom import — handy when
    // callers want stochastic update counts in future variants.
    @SuppressWarnings("unused")
    private static int randomAffected() {
        return ThreadLocalRandom.current().nextInt(1, 6);
    }
}
