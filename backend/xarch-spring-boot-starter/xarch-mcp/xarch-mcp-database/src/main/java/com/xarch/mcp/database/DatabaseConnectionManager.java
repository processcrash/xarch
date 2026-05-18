package com.xarch.mcp.database;

import java.sql.*;
import java.util.*;

/**
 * Database Connection Manager
 * Supports MySQL, PostgreSQL, MongoDB, SQL Server
 */
public class DatabaseConnectionManager {

    private static final Map<String, String> DRIVER_MAP = new HashMap<>();
    private static final Map<String, ConnectionPool> POOLS = new HashMap<>();

    static {
        DRIVER_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
        DRIVER_MAP.put("postgresql", "org.postgresql.Driver");
        DRIVER_MAP.put("mongodb", "mongodb");
        DRIVER_MAP.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    public static class ConnectionConfig {
        private String type;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;

        public ConnectionConfig() {}

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

        public String getConnectionUrl() {
            return switch (type.toLowerCase()) {
                case "mysql" -> String.format("jdbc:mysql://%s:%d/%s", host, port, database);
                case "postgresql" -> String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                case "sqlserver" -> String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, database);
                default -> "";
            };
        }
    }

    public static class ConnectionPool {
        private Connection connection;
        private long lastUsed;
        private int maxIdleTime = 300000;

        public ConnectionPool(Connection connection) {
            this.connection = connection;
            this.lastUsed = System.currentTimeMillis();
        }

        public Connection getConnection() {
            lastUsed = System.currentTimeMillis();
            return connection;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastUsed > maxIdleTime;
        }
    }

    /**
     * Get or create a database connection
     */
    public static Connection getConnection(ConnectionConfig config) throws Exception {
        String poolKey = config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();

        if (POOLS.containsKey(poolKey)) {
            ConnectionPool pool = POOLS.get(poolKey);
            if (!pool.isExpired()) {
                return pool.getConnection();
            }
        }

        Class.forName(DRIVER_MAP.get(config.getType().toLowerCase()));
        Connection conn = DriverManager.getConnection(
            config.getConnectionUrl(),
            config.getUsername(),
            config.getPassword()
        );
        POOLS.put(poolKey, new ConnectionPool(conn));
        return conn;
    }

    /**
     * Execute SQL query and return results
     */
    public static QueryResult executeQuery(String sql, ConnectionConfig config) throws Exception {
        Connection conn = getConnection(config);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }

        rs.close();
        stmt.close();

        return new QueryResult(rows, columnCount);
    }

    /**
     * Execute SQL update (INSERT, UPDATE, DELETE)
     */
    public static int executeUpdate(String sql, ConnectionConfig config) throws Exception {
        Connection conn = getConnection(config);
        Statement stmt = conn.createStatement();
        int affected = stmt.executeUpdate(sql);
        stmt.close();
        return affected;
    }

    public static class QueryResult {
        private List<Map<String, Object>> rows;
        private int columnCount;

        public QueryResult(List<Map<String, Object>> rows, int columnCount) {
            this.rows = rows;
            this.columnCount = columnCount;
        }

        public List<Map<String, Object>> getRows() { return rows; }
        public int getColumnCount() { return columnCount; }
        public int getRowCount() { return rows.size(); }
    }
}