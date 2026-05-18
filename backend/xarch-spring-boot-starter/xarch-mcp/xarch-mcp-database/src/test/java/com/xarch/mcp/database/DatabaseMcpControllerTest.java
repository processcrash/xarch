package com.xarch.mcp.database;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xarch.starter.core.result.ApiResult;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DatabaseMcpController unit tests
 */
@SpringBootTest
class DatabaseMcpControllerTest {

    @Autowired
    private DatabaseMcpController controller;

    @Test
    void testHealth() {
        ApiResult<Map<String, Object>> result = controller.health();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertEquals("UP", result.getData().get("status"));
    }

    @Test
    void testTools() {
        ApiResult<List<Map<String, String>>> result = controller.tools();
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().size() >= 5);
    }

    @Test
    void testTableList() {
        // Test with invalid database - should return error but not crash
        var result = controller.listTables(Map.of("database", "nonexistent"));
        assertNotNull(result);
    }

    @Test
    void testExecuteQueryWithEmptySql() {
        var result = controller.executeQuery(Map.of("sql", ""));
        assertNotNull(result);
        // Should return error for empty SQL
        assertEquals("1002", result.getCode());
    }

    @Test
    void testExecuteQueryWithNullSql() {
        var result = controller.executeQuery(Map.of());
        assertNotNull(result);
        // Should return error for null SQL
        assertEquals("1002", result.getCode());
    }

    @Test
    void testConfigure() {
        var result = controller.configure(Map.of(
            "type", "mysql",
            "host", "localhost",
            "port", 3306,
            "database", "test",
            "username", "root",
            "password", "test"
        ));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }
}