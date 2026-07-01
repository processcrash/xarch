# xarch filesystem-mcp (Java)

Sandboxed filesystem operations over the MCP stdio protocol. Mirror of
the [Node.js sibling](../node-mcp-servers/filesystem-mcp).

## Security: path-traversal protection

`PathGuard` validates every path against a configurable allow-list of
root directories (default: `$XARCH_FS_ALLOWED_ROOTS` env var
colon-separated, falling back to JVM working dir). Symlinks are
resolved via `Path.toRealPath()` to defeat `..` bypasses.

```bash
export XARCH_FS_ALLOWED_ROOTS=/home/user/projects:/tmp/scratch
java -jar filesystem-mcp.jar
```

## Tools (10)

| Tool | Description |
|------|-------------|
| `list_directory` | List directory contents |
| `read_file` | Read a file (utf-8 or base64) |
| `write_file` | Write content atomically |
| `delete` | Delete a file or directory |
| `create_directory` | Create a directory |
| `search_files` | Glob-based file search |
| `get_file_info` | Stat a path |
| `copy_file` | Copy file |
| `move_file` | Move/rename |
| `health` | Health check + working dir |

Plus 2 resources (`fs://config`, `fs://stats`) and 1 prompt (`file-search`).

## License

MIT