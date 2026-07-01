package com.xarch.mcp.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Pure filesystem operations for the MCP server. No MCP-specific knowledge
 * lives here — every method just does I/O on a {@link Path} that was already
 * validated by {@link PathGuard}.
 */
public class FileOps {

    /** Default cap when callers don't specify a maxBytes. 1 MB. */
    public static final long DEFAULT_MAX_BYTES = 1024L * 1024L;

    /** A single directory entry as returned to MCP callers. */
    public record FileEntry(String name, String path, long size, boolean isDirectory, String modifiedTime) {
    }

    /** Result of {@link #writeFile(Path, String, String, boolean)}. */
    public record WriteResult(boolean success, String path, long bytesWritten) {
    }

    /** Result of {@link #delete(Path, boolean)}. */
    public record DeleteResult(boolean success, String path, boolean wasDirectory) {
    }

    /** Result of {@link #createDirectory(Path, boolean)}. */
    public record CreateDirResult(boolean success, String path) {
    }

    /** Result of {@link #copyFile(Path, Path, boolean)} and {@link #moveFile(Path, Path, boolean)}. */
    public record TransferResult(boolean success, String source, String destination) {
    }

    /** Detailed stat for {@link #getFileInfo(Path)}. */
    public record FileInfo(String name, String path, long size, boolean isDirectory,
                           boolean isSymbolicLink, String modifiedTime, String createdTime,
                           String permissions) {
    }

    // ------------------------------------------------------------------
    // Listing & search
    // ------------------------------------------------------------------

    public List<FileEntry> listDirectory(Path root, boolean recursive, boolean includeHidden) throws IOException {
        if (!Files.exists(root)) {
            throw new NoSuchFileException("Directory does not exist: " + root);
        }
        if (!Files.isDirectory(root)) {
            throw new NotDirectoryException(root.toString());
        }
        List<FileEntry> out = new ArrayList<>();
        walk(root, recursive, includeHidden, out, 0, Integer.MAX_VALUE);
        return out;
    }

    public List<Path> searchFiles(Path root, String globPattern, Integer maxDepth) throws IOException {
        if (!Files.exists(root)) {
            throw new NoSuchFileException("Directory does not exist: " + root);
        }
        if (!Files.isDirectory(root)) {
            throw new NotDirectoryException(root.toString());
        }
        // Convert simple glob (containing * / ?) to a regex. Full glob parsing
        // (e.g. **) is not supported — the Node.js sibling uses simple regex
        // translation as well.
        String regex = globToRegex(globPattern);
        Pattern p = Pattern.compile(regex);
        int depth = maxDepth == null ? Integer.MAX_VALUE : Math.max(0, maxDepth);

        List<Path> out = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root, depth)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> {
                        Path rel = root.relativize(path);
                        String name = rel.getFileName().toString();
                        return p.matcher(name).matches();
                    })
                    .forEach(out::add);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Read / write
    // ------------------------------------------------------------------

    public String readFile(Path file, String encoding, long maxBytes) throws IOException {
        if (!Files.exists(file)) {
            throw new NoSuchFileException("File does not exist: " + file);
        }
        if (Files.isDirectory(file)) {
            throw new IOException("Cannot read a directory as a file: " + file);
        }
        long actual = Files.size(file);
        if (actual > maxBytes) {
            throw new IOException("File is " + actual + " bytes which exceeds maxBytes " + maxBytes);
        }
        byte[] bytes = Files.readAllBytes(file);
        String enc = encoding == null ? "utf-8" : encoding.trim().toLowerCase();
        return switch (enc) {
            case "utf-8", "utf8", "text" -> new String(bytes, StandardCharsets.UTF_8);
            case "base64" -> Base64.getEncoder().encodeToString(bytes);
            default -> throw new IllegalArgumentException("Unsupported encoding: " + encoding);
        };
    }

    public WriteResult writeFile(Path file, String content, String encoding, boolean createDirs) throws IOException {
        if (createDirs) {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
        }
        byte[] bytes;
        String enc = encoding == null ? "utf-8" : encoding.trim().toLowerCase();
        bytes = switch (enc) {
            case "utf-8", "utf8", "text" -> (content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8));
            case "base64" -> Base64.getDecoder().decode(content == null ? "" : content);
            default -> throw new IllegalArgumentException("Unsupported encoding: " + encoding);
        };
        // Atomic write: stage in a temp file in the same directory, then move.
        Path parent = file.getParent() == null ? file.toAbsolutePath().getParent() : file.getParent();
        Path tmp = Files.createTempFile(parent, ".xarch-write-", ".tmp");
        try {
            Files.write(tmp, bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ams) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
        return new WriteResult(true, file.toString(), bytes.length);
    }

    // ------------------------------------------------------------------
    // Delete / create-dir
    // ------------------------------------------------------------------

    public DeleteResult delete(Path target, boolean recursive) throws IOException {
        if (!Files.exists(target)) {
            throw new NoSuchFileException("Path does not exist: " + target);
        }
        boolean wasDir = Files.isDirectory(target);
        if (wasDir) {
            if (recursive) {
                try (Stream<Path> walk = Files.walk(target)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + p + ": " + e.getMessage(), e);
                        }
                    });
                }
            } else {
                Files.delete(target); // throws DirectoryNotEmptyException if not empty
            }
        } else {
            Files.delete(target);
        }
        return new DeleteResult(true, target.toString(), wasDir);
    }

    public CreateDirResult createDirectory(Path dir, boolean createParents) throws IOException {
        if (createParents) {
            Files.createDirectories(dir);
        } else {
            Files.createDirectory(dir);
        }
        return new CreateDirResult(true, dir.toString());
    }

    // ------------------------------------------------------------------
    // Move / copy
    // ------------------------------------------------------------------

    public TransferResult copyFile(Path source, Path dest, boolean overwrite) throws IOException {
        if (!Files.exists(source)) throw new NoSuchFileException("Source does not exist: " + source);
        if (Files.isDirectory(source)) throw new IOException("Source is a directory; copyFile supports files only");
        Path parent = dest.getParent();
        if (parent != null) Files.createDirectories(parent);
        CopyOption[] opts = overwrite
                ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES }
                : new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES };
        Files.copy(source, dest, opts);
        return new TransferResult(true, source.toString(), dest.toString());
    }

    public TransferResult moveFile(Path source, Path dest, boolean overwrite) throws IOException {
        if (!Files.exists(source)) throw new NoSuchFileException("Source does not exist: " + source);
        Path parent = dest.getParent();
        if (parent != null) Files.createDirectories(parent);
        CopyOption[] opts = overwrite
                ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE }
                : new CopyOption[] { StandardCopyOption.ATOMIC_MOVE };
        try {
            Files.move(source, dest, opts);
        } catch (AtomicMoveNotSupportedException ams) {
            // Fallback: non-atomic when filesystem doesn't support it.
            CopyOption[] fallback = overwrite
                    ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING }
                    : new CopyOption[] {};
            Files.move(source, dest, fallback);
        }
        return new TransferResult(true, source.toString(), dest.toString());
    }

    // ------------------------------------------------------------------
    // Stat
    // ------------------------------------------------------------------

    public FileInfo getFileInfo(Path target) throws IOException {
        if (!Files.exists(target)) throw new NoSuchFileException("Path does not exist: " + target);
        BasicFileAttributes attrs = Files.readAttributes(target, BasicFileAttributes.class);
        return new FileInfo(
                target.getFileName().toString(),
                target.toString(),
                attrs.size(),
                attrs.isDirectory(),
                attrs.isSymbolicLink(),
                attrs.lastModifiedTime().toString(),
                attrs.creationTime().toString(),
                permissionsString(target)
        );
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private void walk(Path dir, boolean recursive, boolean includeHidden,
                      List<FileEntry> out, int currentDepth, int maxDepth) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path child : ds) {
                String name = child.getFileName().toString();
                if (!includeHidden && name.startsWith(".")) continue;
                out.add(toFileEntry(child));
                if (recursive && Files.isDirectory(child) && currentDepth < maxDepth) {
                    walk(child, recursive, includeHidden, out, currentDepth + 1, maxDepth);
                }
            }
        }
    }

    private FileEntry toFileEntry(Path child) {
        try {
            BasicFileAttributes a = Files.readAttributes(child, BasicFileAttributes.class);
            return new FileEntry(
                    child.getFileName().toString(),
                    child.toString(),
                    a.size(),
                    a.isDirectory(),
                    a.lastModifiedTime().toString());
        } catch (IOException e) {
            return new FileEntry(child.getFileName().toString(), child.toString(), 0L,
                    Files.isDirectory(child), "");
        }
    }

    private static String permissionsString(Path path) {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (view != null) {
                Set<PosixFilePermission> perms = view.readAttributes().permissions();
                return PosixFilePermissions.toString(perms);
            }
        } catch (IOException ignored) {
        }
        // Windows fallback — render the DOS attributes.
        try {
            StringBuilder sb = new StringBuilder("---");
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            if (attrs.isDirectory()) sb.setCharAt(0, 'd');
            if (attrs.isSymbolicLink()) sb.setCharAt(0, 'l');
            return sb.toString() + "(win32)";
        } catch (IOException e) {
            return "----------";
        }
    }

    private static String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> sb.append(".*");
                case '?' -> sb.append('.');
                case '.', '(', ')', '+', '|', '^', '$', '@', '%', '{', '}', '[', ']', '\\' -> {
                    sb.append('\\').append(c);
                }
                default -> sb.append(c);
            }
        }
        sb.append('$');
        return sb.toString();
    }

    /** Exposed for tests. */
    static EnumSet<StandardOpenOption> writableOpen() {
        return EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Exposed for tests. */
    static String globToRegexPublic(String glob) {
        return globToRegex(glob);
    }
}
