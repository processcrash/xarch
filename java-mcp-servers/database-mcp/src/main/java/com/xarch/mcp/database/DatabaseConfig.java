package com.xarch.mcp.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory database connection configuration held by the MCP server.
 *
 * <p>Mirrors the {@code DatabaseConfig} shape used by the Node.js sibling at
 * {@code node-mcp-servers/database-mcp/src/database.ts}. The password is
 * kept in-memory only; the {@link #maskedPassword()} helper is used by
 * tools, resources, and {@code health} responses so secrets never leak
 * into the MCP wire format.
 *
 * <p>This class is intentionally simple — the runtime is single-instance
 * and stdio-bound, so there is no concurrency beyond what the JVM gives
 * us on a single thread.
 */
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private String type = "mysql";
    private String host = "localhost";
    private int port = 3306;
    private String database = "";
    private String username = "";
    private String password = "";
    private boolean ssl = false;

    /** Reset all fields to defaults. Used by the {@code configure} tool. */
    public void reset() {
        this.type = "mysql";
        this.host = "localhost";
        this.port = 3306;
        this.database = "";
        this.username = "";
        this.password = "";
        this.ssl = false;
    }

    /**
     * Apply a partial config from a JSON-RPC arguments object. Missing
     * fields keep their current value so callers can update a single
     * setting without re-sending everything.
     *
     * @param args JSON arguments from a {@code tools/call} request
     */
    public void applyFromArgs(Map<String, Object> args) {
        if (args == null) return;
        if (args.get("type") instanceof String s && !s.isBlank()) this.type = s;
        if (args.get("host") instanceof String s && !s.isBlank()) this.host = s;
        Object portRaw = args.get("port");
        if (portRaw instanceof Number n) {
            this.port = n.intValue();
        } else if (portRaw instanceof String s && !s.isBlank()) {
            this.port = Integer.parseInt(s);
        }
        if (args.get("database") instanceof String s) this.database = s;
        if (args.get("username") instanceof String s) this.username = s;
        if (args.get("password") instanceof String s) this.password = s;
        Object sslRaw = args.get("ssl");
        if (sslRaw instanceof Boolean b) this.ssl = b;
    }

    /**
     * @return {@code true} once the minimum fields needed to open a
     *         connection are populated.
     */
    public boolean isConfigured() {
        return host != null && !host.isBlank()
                && database != null && !database.isBlank()
                && username != null;
    }

    /**
     * @return the password with all characters replaced by {@code *}.
     *         Returns {@code "(unset)"} when no password has been stored.
     */
    public String maskedPassword() {
        if (password == null || password.isEmpty()) return "(unset)";
        return "*".repeat(Math.min(password.length(), 8));
    }

    /**
     * @return a JSON-friendly snapshot suitable for inclusion in
     *         {@code config://current} and {@code health} responses.
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("host", host);
        m.put("port", port);
        m.put("database", database);
        m.put("username", username);
        m.put("password", maskedPassword());
        m.put("ssl", ssl);
        m.put("configured", isConfigured());
        return m;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSsl() { return ssl; }
    public void setSsl(boolean ssl) { this.ssl = ssl; }

    /** Log a one-line summary without revealing the password. */
    public void logSummary(String action) {
        log.info("config[{}] type={} {}@{}/{} (ssl={})",
                action, type, username, host + ":" + port, database, ssl);
    }
}
