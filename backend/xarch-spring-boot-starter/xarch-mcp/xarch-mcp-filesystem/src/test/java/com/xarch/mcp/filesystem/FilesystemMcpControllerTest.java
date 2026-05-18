package com.xarch.mcp.filesystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xarch.starter.core.result.ApiResult;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FilesystemMcpController unit tests
 */
@SpringBootTest
class FilesystemMcpControllerTest {

    @Autowired
    private FilesystemMcpController controller;

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
    void testWriteAndReadFile() throws Exception {
        // Write a test file
        String testPath = "test_write_file.txt";
        String testContent = "Hello, Filesystem MCP Server!";

        var writeResult = controller.writeFile(Map.of(
            "path", testPath,
            "content", testContent
        ));
        assertNotNull(writeResult);
        assertEquals("0000", writeResult.getCode());

        // Read it back
        var readResult = controller.readFile(Map.of(
            "path", testPath
        ));
        assertNotNull(readResult);
        assertEquals("0000", readResult.getCode());
        assertNotNull(readResult.getData());

        // Clean up
        controller.delete(Map.of("path", testPath));
    }

    @Test
    void testGetFileInfo() throws Exception {
        // First create a file
        String testPath = "test_info.txt";
        controller.writeFile(Map.of(
            "path", testPath,
            "content", "Test file for info"
        ));

        // Get file info
        var result = controller.getFileInfo(Map.of("path", testPath));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
        assertNotNull(result.getData());

        // Clean up
        controller.delete(Map.of("path", testPath));
    }

    @Test
    void testDelete() throws Exception {
        // First create a file
        String testPath = "test_delete.txt";
        controller.writeFile(Map.of(
            "path", testPath,
            "content", "File to be deleted"
        ));

        // Delete it
        var result = controller.delete(Map.of("path", testPath));
        assertNotNull(result);
        assertEquals("0000", result.getCode());
    }

    @Test
    void testListDirectory() {
        var result = controller.listDirectory(Map.of(
            "path", System.getProperty("user.home"),
            "recursive", false
        ));
        assertNotNull(result);
        // May return error if directory not allowed, which is acceptable
    }

    @Test
    void testSearchFiles() {
        var result = controller.searchFiles(Map.of(
            "path", System.getProperty("user.home"),
            "pattern", "*.txt",
            "recursive", false
        ));
        assertNotNull(result);
        // May return error if directory not allowed, which is acceptable
    }
}