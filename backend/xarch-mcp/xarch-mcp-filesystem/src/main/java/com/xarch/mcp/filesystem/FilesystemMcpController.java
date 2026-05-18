package com.xarch.mcp.filesystem;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Filesystem MCP Server Controller
 * Provides secure file operations via MCP protocol
 */
@RestController
@RequestMapping("/mcp/filesystem")
public class FilesystemMcpController {

    private final FileOperations fileOps = new FileOperations();

    /**
     * Health check
     */
    @GetMapping("/health")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
            "status", "UP",
            "service", "filesystem-mcp",
            "version", "1.0.0"
        ));
    }

    /**
     * List available tools
     */
    @GetMapping("/tools")
    public ApiResult<List<Map<String, String>>> tools() {
        return ApiResult.success(List.of(
            Map.of("name", "list_directory", "description", "List directory contents"),
            Map.of("name", "read_file", "description", "Read file content"),
            Map.of("name", "write_file", "description", "Write content to file"),
            Map.of("name", "delete", "description", "Delete file or directory"),
            Map.of("name", "create_directory", "description", "Create directory"),
            Map.of("name", "search_files", "description", "Search files by pattern"),
            Map.of("name", "get_file_info", "description", "Get file information"),
            Map.of("name", "copy_file", "description", "Copy file"),
            Map.of("name", "move_file", "description", "Move file")
        ));
    }

    /**
     * List directory
     */
    @PostMapping("/tools/list_directory")
    public ApiResult<List<FileOperations.FileInfo>> listDirectory(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.getOrDefault("path", "/");
            boolean recursive = (boolean) params.getOrDefault("recursive", false);

            List<FileOperations.FileInfo> files = fileOps.listDirectory(path, recursive);
            return ApiResult.success(files);
        } catch (Exception e) {
            return ApiResult.error("List directory failed: " + e.getMessage());
        }
    }

    /**
     * Read file
     */
    @PostMapping("/tools/read_file")
    public ApiResult<Map<String, Object>> readFile(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            if (path == null || path.isEmpty()) {
                return ApiResult.error("Path is required");
            }

            int maxLines = (int) params.getOrDefault("maxLines", 0);
            String content;

            if (maxLines > 0) {
                List<String> lines = fileOps.readFileLines(path, maxLines);
                content = String.join("\n", lines);
            } else {
                content = fileOps.readFile(path);
            }

            return ApiResult.success(Map.of(
                "path", path,
                "content", content,
                "size", content.length()
            ));
        } catch (Exception e) {
            return ApiResult.error("Read file failed: " + e.getMessage());
        }
    }

    /**
     * Write file
     */
    @PostMapping("/tools/write_file")
    public ApiResult<Map<String, Object>> writeFile(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            String content = (String) params.get("content");

            if (path == null || path.isEmpty()) {
                return ApiResult.error("Path is required");
            }
            if (content == null) {
                return ApiResult.error("Content is required");
            }

            boolean success = fileOps.writeFile(path, content);

            return ApiResult.success(Map.of(
                "path", path,
                "success", success,
                "size", content.length()
            ));
        } catch (Exception e) {
            return ApiResult.error("Write file failed: " + e.getMessage());
        }
    }

    /**
     * Delete file or directory
     */
    @PostMapping("/tools/delete")
    public ApiResult<Map<String, Object>> delete(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            if (path == null || path.isEmpty()) {
                return ApiResult.error("Path is required");
            }

            boolean success = fileOps.delete(path);

            return ApiResult.success(Map.of(
                "path", path,
                "success", success
            ));
        } catch (Exception e) {
            return ApiResult.error("Delete failed: " + e.getMessage());
        }
    }

    /**
     * Create directory
     */
    @PostMapping("/tools/create_directory")
    public ApiResult<Map<String, Object>> createDirectory(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            if (path == null || path.isEmpty()) {
                return ApiResult.error("Path is required");
            }

            boolean success = fileOps.createDirectory(path);

            return ApiResult.success(Map.of(
                "path", path,
                "success", success
            ));
        } catch (Exception e) {
            return ApiResult.error("Create directory failed: " + e.getMessage());
        }
    }

    /**
     * Search files
     */
    @PostMapping("/tools/search_files")
    public ApiResult<List<FileOperations.FileInfo>> searchFiles(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.getOrDefault("path", "/");
            String pattern = (String) params.getOrDefault("pattern", "*");
            boolean recursive = (boolean) params.getOrDefault("recursive", true);

            List<FileOperations.FileInfo> files = fileOps.searchFiles(path, pattern, recursive);

            return ApiResult.success(files);
        } catch (Exception e) {
            return ApiResult.error("Search files failed: " + e.getMessage());
        }
    }

    /**
     * Get file info
     */
    @PostMapping("/tools/get_file_info")
    public ApiResult<FileOperations.FileInfo> getFileInfo(@RequestBody Map<String, Object> params) {
        try {
            String path = (String) params.get("path");
            if (path == null || path.isEmpty()) {
                return ApiResult.error("Path is required");
            }

            FileOperations.FileInfo info = fileOps.getFileInfo(path);
            return ApiResult.success(info);
        } catch (Exception e) {
            return ApiResult.error("Get file info failed: " + e.getMessage());
        }
    }

    /**
     * Copy file
     */
    @PostMapping("/tools/copy_file")
    public ApiResult<Map<String, Object>> copyFile(@RequestBody Map<String, Object> params) {
        try {
            String source = (String) params.get("source");
            String destination = (String) params.get("destination");

            if (source == null || destination == null) {
                return ApiResult.error("Source and destination are required");
            }

            boolean success = fileOps.copyFile(source, destination);

            return ApiResult.success(Map.of(
                "source", source,
                "destination", destination,
                "success", success
            ));
        } catch (Exception e) {
            return ApiResult.error("Copy file failed: " + e.getMessage());
        }
    }

    /**
     * Move file
     */
    @PostMapping("/tools/move_file")
    public ApiResult<Map<String, Object>> moveFile(@RequestBody Map<String, Object> params) {
        try {
            String source = (String) params.get("source");
            String destination = (String) params.get("destination");

            if (source == null || destination == null) {
                return ApiResult.error("Source and destination are required");
            }

            boolean success = fileOps.moveFile(source, destination);

            return ApiResult.success(Map.of(
                "source", source,
                "destination", destination,
                "success", success
            ));
        } catch (Exception e) {
            return ApiResult.error("Move file failed: " + e.getMessage());
        }
    }
}