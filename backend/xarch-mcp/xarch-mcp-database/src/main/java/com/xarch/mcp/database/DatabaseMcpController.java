package com.xarch.mcp.database;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Database MCP Server Controller
 * Provides tools for database operations via MCP protocol
 */
@RestController
@RequestMapping("/mcp/database")
public class DatabaseMcpController {

    private final DatabaseConnectionManager.ConnectionConfig config = new DatabaseConnectionManager.ConnectionConfig();

    {
        config.setType("mysql");
        config.setHost("localhost");
        config.setPort(3306);
        config.setDatabase("xarch");
        config.setUsername("root");
        config.setPassword("root123");
    }

    /**
     * Health check for database MCP server
     */
    @GetMapping("/health")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
            "status", "UP",
            "service", "database-mcp",
            "version", "1.0.0",
            "supportedDatabases", List.of("mysql", "postgresql", "mongodb", "sqlserver")
        ));
    }

    /**
     * List available tools
     */
    @GetMapping("/tools")
    public ApiResult<List<Map<String, String>>> tools() {
        return ApiResult.success(List.of(
            Map.of("name", "query_execute", "description", "Execute SQL query"),
            Map.of("name", "schema_get", "description", "Get database schema"),
            Map.of("name", "table_list", "description", "List all tables"),
            Map.of("name", "table_describe", "description", "Describe table structure"),
            Map.of("name", "index_list", "description", "List indexes for a table")
        ));
    }

    /**
     * Execute SQL query
     */
    @PostMapping("/tools/query_execute")
    public ApiResult<DatabaseConnectionManager.QueryResult> executeQuery(@RequestBody Map<String, Object> params) {
        try {
            String sql = (String) params.get("sql");
            if (sql == null || sql.isEmpty()) {
                return ApiResult.error("SQL query is required");
            }
            DatabaseConnectionManager.QueryResult result = DatabaseConnectionManager.executeQuery(sql, config);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("Query execution failed: " + e.getMessage());
        }
    }

    /**
     * Get database schema
     */
    @PostMapping("/tools/schema_get")
    public ApiResult<Map<String, Object>> getSchema(@RequestBody Map<String, Object> params) {
        try {
            String database = (String) params.getOrDefault("database", config.getDatabase());
            String schemaSql = "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + database + "'";

            DatabaseConnectionManager.QueryResult result = DatabaseConnectionManager.executeQuery(schemaSql, config);

            return ApiResult.success(Map.of(
                "database", database,
                "tables", result.getRows()
            ));
        } catch (Exception e) {
            return ApiResult.error("Schema retrieval failed: " + e.getMessage());
        }
    }

    /**
     * List tables
     */
    @PostMapping("/tools/table_list")
    public ApiResult<List<Map<String, Object>>> listTables(@RequestBody Map<String, Object> params) {
        try {
            String database = (String) params.getOrDefault("database", config.getDatabase());
            String sql = "SELECT TABLE_NAME, TABLE_TYPE, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + database + "'";

            DatabaseConnectionManager.QueryResult result = DatabaseConnectionManager.executeQuery(sql, config);

            return ApiResult.success(result.getRows());
        } catch (Exception e) {
            return ApiResult.error("Table listing failed: " + e.getMessage());
        }
    }

    /**
     * Describe table structure
     */
    @PostMapping("/tools/table_describe")
    public ApiResult<List<Map<String, Object>>> describeTable(@RequestBody Map<String, Object> params) {
        try {
            String tableName = (String) params.get("tableName");
            if (tableName == null || tableName.isEmpty()) {
                return ApiResult.error("Table name is required");
            }

            String sql = "DESCRIBE " + tableName;
            DatabaseConnectionManager.QueryResult result = DatabaseConnectionManager.executeQuery(sql, config);

            return ApiResult.success(result.getRows());
        } catch (Exception e) {
            return ApiResult.error("Table description failed: " + e.getMessage());
        }
    }

    /**
     * List indexes for a table
     */
    @PostMapping("/tools/index_list")
    public ApiResult<List<Map<String, Object>>> listIndexes(@RequestBody Map<String, Object> params) {
        try {
            String tableName = (String) params.get("tableName");
            String database = (String) params.getOrDefault("database", config.getDatabase());

            if (tableName == null || tableName.isEmpty()) {
                return ApiResult.error("Table name is required");
            }

            String sql = "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = '" + tableName + "'";

            DatabaseConnectionManager.QueryResult result = DatabaseConnectionManager.executeQuery(sql, config);

            return ApiResult.success(result.getRows());
        } catch (Exception e) {
            return ApiResult.error("Index listing failed: " + e.getMessage());
        }
    }

    /**
     * Execute SQL update (INSERT, UPDATE, DELETE)
     */
    @PostMapping("/tools/execute_update")
    public ApiResult<Integer> executeUpdate(@RequestBody Map<String, Object> params) {
        try {
            String sql = (String) params.get("sql");
            if (sql == null || sql.isEmpty()) {
                return ApiResult.error("SQL statement is required");
            }
            int affected = DatabaseConnectionManager.executeUpdate(sql, config);
            return ApiResult.success(affected);
        } catch (Exception e) {
            return ApiResult.error("Update execution failed: " + e.getMessage());
        }
    }

    /**
     * Configure database connection
     */
    @PostMapping("/config")
    public ApiResult<Void> configure(@RequestBody Map<String, Object> configParams) {
        try {
            if (configParams.containsKey("type")) {
                config.setType((String) configParams.get("type"));
            }
            if (configParams.containsKey("host")) {
                config.setHost((String) configParams.get("host"));
            }
            if (configParams.containsKey("port")) {
                config.setPort((Integer) configParams.get("port"));
            }
            if (configParams.containsKey("database")) {
                config.setDatabase((String) configParams.get("database"));
            }
            if (configParams.containsKey("username")) {
                config.setUsername((String) configParams.get("username"));
            }
            if (configParams.containsKey("password")) {
                config.setPassword((String) configParams.get("password"));
            }
            return ApiResult.success(null);
        } catch (Exception e) {
            return ApiResult.error("Configuration failed: " + e.getMessage());
        }
    }
}