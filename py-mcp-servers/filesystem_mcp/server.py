"""
Filesystem MCP Server - Core implementation with security controls
"""

import json
from typing import Any, Dict
from .file_operations import FileOperations


class FilesystemMCPServer:
    """Filesystem MCP Server with path traversal prevention"""

    def __init__(self, allowed_path: str = "/tmp/xarch-files"):
        self.file_ops = FileOperations(allowed_path=allowed_path)

    async def handle_initialize(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """Handle initialization"""
        return {
            "protocolVersion": "2024-11-05",
            "capabilities": {
                "tools": {},
                "resources": {},
                "prompts": {},
            },
            "serverInfo": {
                "name": "xarch-filesystem-mcp",
                "version": "1.0.0",
            },
        }

    async def handle_list_tools(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """List available tools"""
        return {
            "tools": [
                {
                    "name": "list_directory",
                    "description": "List contents of a directory",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "Directory path to list"},
                            "recursive": {"type": "boolean", "description": "List subdirectories recursively"},
                        },
                        "required": ["path"],
                    },
                },
                {
                    "name": "read_file",
                    "description": "Read contents of a file",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "File path to read"},
                        },
                        "required": ["path"],
                    },
                },
                {
                    "name": "write_file",
                    "description": "Write content to a file",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "File path to write"},
                            "content": {"type": "string", "description": "Content to write"},
                        },
                        "required": ["path", "content"],
                    },
                },
                {
                    "name": "delete",
                    "description": "Delete a file or directory",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "Path to delete"},
                        },
                        "required": ["path"],
                    },
                },
                {
                    "name": "create_directory",
                    "description": "Create a new directory",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "Directory path to create"},
                        },
                        "required": ["path"],
                    },
                },
                {
                    "name": "search_files",
                    "description": "Search for files matching a pattern",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "Directory to search in"},
                            "pattern": {"type": "string", "description": "Search pattern (e.g., *.txt, *.md)"},
                            "recursive": {"type": "boolean", "description": "Search subdirectories"},
                        },
                        "required": ["path", "pattern"],
                    },
                },
                {
                    "name": "get_file_info",
                    "description": "Get information about a file or directory",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "path": {"type": "string", "description": "File or directory path"},
                        },
                        "required": ["path"],
                    },
                },
                {
                    "name": "copy_file",
                    "description": "Copy a file to a new location",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "source": {"type": "string", "description": "Source file path"},
                            "destination": {"type": "string", "description": "Destination file path"},
                        },
                        "required": ["source", "destination"],
                    },
                },
                {
                    "name": "move_file",
                    "description": "Move a file to a new location",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "source": {"type": "string", "description": "Source file path"},
                            "destination": {"type": "string", "description": "Destination file path"},
                        },
                        "required": ["source", "destination"],
                    },
                },
                {
                    "name": "health",
                    "description": "Check filesystem service health",
                    "inputSchema": {"type": "object", "properties": {}},
                },
            ]
        }

    async def handle_call_tool(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """Handle tool calls"""
        tool_name = request.get("name", "")
        arguments = request.get("arguments", {})

        try:
            if tool_name == "list_directory":
                files = self.file_ops.list_directory(
                    path=arguments.get("path", ""),
                    recursive=arguments.get("recursive", False),
                )
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "success": True,
                            "path": arguments.get("path"),
                            "files": files,
                            "count": len(files),
                        }),
                    }],
                }

            elif tool_name == "read_file":
                content = self.file_ops.read_file(path=arguments.get("path", ""))
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "success": True,
                            "path": arguments.get("path"),
                            "content": content,
                        }),
                    }],
                }

            elif tool_name == "write_file":
                result = self.file_ops.write_file(
                    path=arguments.get("path", ""),
                    content=arguments.get("content", ""),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "delete":
                self.file_ops.delete(path=arguments.get("path", ""))
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True})}],
                }

            elif tool_name == "create_directory":
                result = self.file_ops.create_directory(path=arguments.get("path", ""))
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "search_files":
                files = self.file_ops.search_files(
                    path=arguments.get("path", ""),
                    pattern=arguments.get("pattern", ""),
                    recursive=arguments.get("recursive", False),
                )
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "success": True,
                            "path": arguments.get("path"),
                            "pattern": arguments.get("pattern"),
                            "files": files,
                            "count": len(files),
                        }),
                    }],
                }

            elif tool_name == "get_file_info":
                info = self.file_ops.get_file_info(path=arguments.get("path", ""))
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, "file": info})}],
                }

            elif tool_name == "copy_file":
                result = self.file_ops.copy_file(
                    source=arguments.get("source", ""),
                    destination=arguments.get("destination", ""),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "move_file":
                result = self.file_ops.move_file(
                    source=arguments.get("source", ""),
                    destination=arguments.get("destination", ""),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "health":
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "status": "UP",
                            "allowedPath": self.file_ops.allowed_path,
                        }),
                    }],
                }

            else:
                return {
                    "content": [{"type": "text", "text": json.dumps({"error": f"Unknown tool: {tool_name}"})}],
                    "isError": True,
                }

        except Exception as e:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": str(e)})}],
                "isError": True,
            }

    async def handle_list_resources(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """List resources"""
        return {
            "resources": [
                {
                    "uri": "filesystem://config",
                    "name": "Filesystem Configuration",
                    "description": "Current filesystem MCP configuration",
                    "mimeType": "application/json",
                }
            ]
        }

    async def handle_list_prompts(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """List prompts"""
        return {
            "prompts": [
                {
                    "name": "file-search",
                    "description": "Search and read files matching criteria",
                    "arguments": [
                        {"name": "pattern", "description": "File pattern to search for", "required": True},
                    ],
                }
            ]
        }


async def main():
    """Start the Filesystem MCP Server"""
    server = FilesystemMCPServer()
    from .stdio_server import run_stdio_server

    await run_stdio_server(server)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())