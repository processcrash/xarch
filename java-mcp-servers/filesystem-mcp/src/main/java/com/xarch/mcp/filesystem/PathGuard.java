package com.xarch.mcp.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Security boundary for the filesystem MCP server.
 *
 * <p>All paths presented by the MCP client are validated through this class
 * before any I/O is performed. If a path does not resolve inside one of the
 * configured {@link #allowedRoots allowed roots}, a {@link SecurityException}
 * is thrown — never silently accepted.
 *
 * <p>Configured roots are stored as their real (symlink-resolved) absolute
 * paths so symlinks cannot be used to escape the sandbox.
 */
public class PathGuard {

    private final List<Path> allowedRoots;

    /**
     * Default constructor — single allowed root at the JVM user.dir.
     */
    public PathGuard() {
        this(List.of(Paths.get(System.getProperty("user.dir")).toAbsolutePath()));
    }

    /**
     * Configure from a list of path strings (e.g. from an env var). Each entry
     * is resolved to an absolute, normalised, real path. The first root is the
     * "primary" root — used for {@link #primaryRoot()}.
     */
    public PathGuard(List<String> roots) {
        if (roots == null || roots.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed root must be configured");
        }
        List<Path> resolved = new ArrayList<>(roots.size());
        for (String r : roots) {
            if (r == null || r.isBlank()) continue;
            Path p = Paths.get(r).toAbsolutePath().normalize();
            try {
                // If the path exists, follow symlinks so we get the real location.
                if (Files.exists(p)) {
                    p = p.toRealPath();
                }
            } catch (IOException e) {
                // Non-fatal — keep the normalised path; existence check happens later.
            }
            resolved.add(p);
        }
        if (resolved.isEmpty()) {
            throw new IllegalArgumentException("At least one non-empty allowed root must be configured");
        }
        this.allowedRoots = Collections.unmodifiableList(resolved);
    }

    /** Returns the unmodifiable list of allowed root paths (absolute, normalised). */
    public List<Path> allowedRoots() {
        return allowedRoots;
    }

    /** Returns the primary (first) allowed root. */
    public Path primaryRoot() {
        return allowedRoots.get(0);
    }

    /**
     * Resolves and validates an input path against the allowed roots.
     *
     * <p>If {@code input} is relative, it is resolved against {@link #primaryRoot()}.
     * The result is normalised and must begin with one of the allowed roots.
     * For paths that already exist, symlinks are resolved with
     * {@link Path#toRealPath()} so a symlink pointing outside the sandbox
     * cannot bypass the check.
     *
     * @throws SecurityException if the path is outside every allowed root.
     */
    public Path validate(String input) {
        if (input == null || input.isBlank()) {
            throw new SecurityException("path is required");
        }
        Path candidate = Paths.get(input);
        if (!candidate.isAbsolute()) {
            candidate = primaryRoot().resolve(candidate);
        }
        Path normalized = candidate.normalize();

        // If the path exists, follow symlinks so a malicious symlink can't escape.
        Path resolved = normalized;
        if (Files.exists(normalized)) {
            try {
                resolved = normalized.toRealPath();
            } catch (IOException e) {
                throw new SecurityException("Cannot resolve real path for '" + input + "': " + e.getMessage());
            }
        }

        for (Path root : allowedRoots) {
            if (isInside(root, resolved)) {
                return resolved;
            }
        }
        throw new SecurityException(
                "Access denied: path '" + input + "' resolves to '" + resolved +
                        "' which is outside the allowed roots " + absoluteRootsAsString());
    }

    /**
     * True if {@code child} is {@code root} itself, or lies within {@code root}.
     */
    private static boolean isInside(Path root, Path child) {
        Path nRoot = root.toAbsolutePath().normalize();
        Path nChild = child.toAbsolutePath().normalize();
        if (nRoot.equals(nChild)) return true;
        return nChild.startsWith(nRoot);
    }

    private String absoluteRootsAsString() {
        return allowedRoots.stream().map(Path::toString).toList().toString();
    }

    /**
     * Convenience helper: produce the working directory used when a relative
     * path is supplied to a tool.
     */
    public String defaultWorkingDirectory() {
        return primaryRoot().toString();
    }

    /**
     * Validate and produce a JSON-serialisable summary suitable for {@code fs://config}.
     */
    public String configSnapshot() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"defaultWorkingDirectory\":").append(jsonString(defaultWorkingDirectory()));
        sb.append(",\"allowedRoots\":[");
        try (Stream<Path> s = allowedRoots.stream()) {
            boolean first = true;
            for (Path p : (Iterable<Path>) s::iterator) {
                if (!first) sb.append(',');
                first = false;
                sb.append(jsonString(p.toString()));
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String jsonString(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append("\"");
        return out.toString();
    }
}
