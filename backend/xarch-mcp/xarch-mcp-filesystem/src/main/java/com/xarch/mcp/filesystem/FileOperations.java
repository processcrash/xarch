package com.xarch.mcp.filesystem;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Filesystem MCP Server
 * Provides secure file operations for AI assistants
 */
public class FileOperations {

    private static final String BASE_DIR = System.getProperty("user.home") + "/xarch-mcp-files";
    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "txt", "md", "json", "yaml", "yml", "xml", "csv", "log",
        "java", "kt", "js", "ts", "vue", "html", "css",
        "sql", "sh", "bat", "ps1"
    );

    static {
        // Ensure base directory exists
        new File(BASE_DIR).mkdirs();
    }

    /**
     * List directory contents
     */
    public List<FileInfo> listDirectory(String path, boolean recursive) throws Exception {
        File dir = resolvePath(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory not found: " + path);
        }

        List<FileInfo> results = new ArrayList<>();
        listFilesRecursive(dir, recursive, results, 0, 3);
        return results;
    }

    private void listFilesRecursive(File dir, boolean recursive, List<FileInfo> results, int depth, int maxDepth) {
        if (depth > maxDepth) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            results.add(new FileInfo(file));

            if (recursive && file.isDirectory() && !file.getName().startsWith(".")) {
                listFilesRecursive(file, true, results, depth + 1, maxDepth);
            }
        }
    }

    /**
     * Read file content
     */
    public String readFile(String path) throws Exception {
        File file = resolvePath(path);
        validateFile(file);
        return Files.readString(file.toPath());
    }

    /**
     * Read file as lines
     */
    public List<String> readFileLines(String path, int maxLines) throws Exception {
        File file = resolvePath(path);
        validateFile(file);

        List<String> lines = Files.readAllLines(file.toPath());
        if (maxLines > 0 && lines.size() > maxLines) {
            return lines.subList(0, maxLines);
        }
        return lines;
    }

    /**
     * Write content to file
     */
    public boolean writeFile(String path, String content) throws Exception {
        File file = resolvePath(path);

        // Ensure parent directory exists
        file.getParentFile().mkdirs();

        // Check file size
        if (content.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File content exceeds maximum size: " + MAX_FILE_SIZE);
        }

        Files.writeString(file.toPath(), content);
        return true;
    }

    /**
     * Append content to file
     */
    public boolean appendToFile(String path, String content) throws Exception {
        File file = resolvePath(path);
        Files.writeString(file.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return true;
    }

    /**
     * Delete file or directory
     */
    public boolean delete(String path) throws Exception {
        File file = resolvePath(path);
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            Files.delete(file.toPath());
        }
        return true;
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    /**
     * Create directory
     */
    public boolean createDirectory(String path) throws Exception {
        File dir = resolvePath(path);
        return dir.mkdirs();
    }

    /**
     * Search files by pattern
     */
    public List<FileInfo> searchFiles(String path, String pattern, boolean recursive) throws Exception {
        File dir = resolvePath(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory not found: " + path);
        }

        Pattern regex = Pattern.compile(wildcardToRegex(pattern));
        List<FileInfo> results = new ArrayList<>();
        searchRecursive(dir, recursive, results, regex, 0, 5);
        return results;
    }

    private void searchRecursive(File dir, boolean recursive, List<FileInfo> results, Pattern pattern, int depth, int maxDepth) {
        if (depth > maxDepth) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (pattern.matcher(file.getName()).matches()) {
                results.add(new FileInfo(file));
            }

            if (recursive && file.isDirectory() && !file.getName().startsWith(".")) {
                searchRecursive(file, true, results, pattern, depth + 1, maxDepth);
            }
        }
    }

    /**
     * Get file info
     */
    public FileInfo getFileInfo(String path) throws Exception {
        File file = resolvePath(path);
        return new FileInfo(file);
    }

    /**
     * Copy file
     */
    public boolean copyFile(String source, String destination) throws Exception {
        File src = resolvePath(source);
        File dest = resolvePath(destination);

        if (!src.exists()) {
            throw new FileNotFoundException("Source not found: " + source);
        }

        dest.getParentFile().mkdirs();
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    /**
     * Move file
     */
    public boolean moveFile(String source, String destination) throws Exception {
        File src = resolvePath(source);
        File dest = resolvePath(destination);

        if (!src.exists()) {
            throw new FileNotFoundException("Source not found: " + source);
        }

        dest.getParentFile().mkdirs();
        Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    // Utility methods

    private File resolvePath(String path) {
        // Security: prevent path traversal
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(BASE_DIR, path);
        }
        return file;
    }

    private void validateFile(File file) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getPath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getPath());
        }
        if (file.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large: " + file.length());
        }

        // Check extension
        String ext = getExtension(file.getName());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + ext);
        }
    }

    private String wildcardToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (char c : pattern.toCharArray()) {
            switch (c) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append(".");
                default -> regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1) : "";
    }

    public static class FileInfo {
        private String name;
        private String path;
        private String type; // "file" or "directory"
        private long size;
        private long modifiedAt;
        private String extension;

        public FileInfo() {}

        public FileInfo(File file) {
            this.name = file.getName();
            this.path = file.getPath();
            this.type = file.isDirectory() ? "directory" : "file";
            this.size = file.length();
            this.modifiedAt = file.lastModified();
            this.extension = getExtension(file.getName());
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public long getModifiedAt() { return modifiedAt; }
        public void setModifiedAt(long modifiedAt) { this.modifiedAt = modifiedAt; }
        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
    }
}