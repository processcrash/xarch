"""
Database MCP Server - Core implementation
"""

import json
from typing import Any, Dict, List, Optional
from .database import ConnectionManager, DatabaseConfig

try:
    from mcp.server import Server
    from mcp.types import (
        CallToolRequest,
        ListToolsRequest,
        ListResourcesRequest,
        ListPromptsRequest,
        InitializeRequest,
        Tool,
        Resource,
        Prompt,
        ServerCapabilities,
    )
    from mcp.server.stdio import stdio_server
except ImportError:
    # Fallback for when mcp package is not installed
    # We'll define the protocol interfaces ourselves
    Server = None
    CallToolRequest = Any
    ListToolsRequest = Any
    ListResourcesRequest = Any
    ListPromptsRequest = Any
    InitializeRequest = Any
    Tool = Any
    Resource = Any
    Prompt = Any
    ServerCapabilities = Any

# Server instance
app = Server(
    name="xarch-database-mcp",
    version="1.0.0",
)


class DatabaseMCPServer:
    """Database MCP Server with multi-database support"""

    def __init__(self):
        self.connection_manager = ConnectionManager()
        self.db_config: Optional[DatabaseConfig] = None

    async def handle_initialize(self, request: InitializeRequest) -> Dict[str, Any]:
        """Handle initialization"""
        return {
            "protocolVersion": "2024-11-05",
            "capabilities": {
                "tools": {},
                "resources": {},
                "prompts": {},
            },
            "serverInfo": {
                "name": "xarch-database-mcp",
                "version": "1.0.0",
            },
        }

    async def handle_list_tools(self, request: ListToolsRequest) -> Dict[str, Any]:
        """List available tools"""
        return {
            "tools": [
                {
                    "name": "configure",
                    "description": "Configure database connection",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "type": {
                                "type": "string",
                                "enum": ["mysql", "postgresql", "mongodb", "sqlserver"],
                                "description": "Database type",
                            },
                            "host": {"type": "string", "description": "Database host"},
                            "port": {"type": "integer", "description": "Database port"},
                            "database": {"type": "string", "description": "Database name"},
                            "username": {"type": "string", "description": "Database username"},
                            "password": {"type": "string", "description": "Database password"},
                        },
                        "required": ["type", "host", "port", "database", "username", "password"],
                    },
                },
                {
                    "name": "query_execute",
                    "description": "Execute a SELECT query on the configured database",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "sql": {"type": "string", "description": "SQL SELECT query to execute"},
                            "params": {
                                "type": "array",
                                "description": "Query parameters",
                                "items": {},
                            },
                        },
                        "required": ["sql"],
                    },
                },
                {
                    "name": "execute_update",
                    "description": "Execute an INSERT, UPDATE, or DELETE statement",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "sql": {"type": "string", "description": "SQL INSERT/UPDATE/DELETE query"},
                            "params": {
                                "type": "array",
                                "description": "Query parameters",
                                "items": {},
                            },
                        },
                        "required": ["sql"],
                    },
                },
                {
                    "name": "schema_get",
                    "description": "Get database schema information including all tables",
                    "inputSchema": {"type": "object", "properties": {}},
                },
                {
                    "name": "table_list",
                    "description": "List all tables in the database",
                    "inputSchema": {"type": "object", "properties": {}},
                },
                {
                    "name": "table_describe",
                    "description": "Get the structure of a specific table",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "table": {"type": "string", "description": "Table name to describe"},
                        },
                        "required": ["table"],
                    },
                },
                {
                    "name": "index_list",
                    "description": "List all indexes for a specific table",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "table": {"type": "string", "description": "Table name"},
                        },
                        "required": ["table"],
                    },
                },
                {
                    "name": "health",
                    "description": "Check database connection health",
                    "inputSchema": {"type": "object", "properties": {}},
                },
            ]
        }

    async def handle_call_tool(self, request: CallToolRequest) -> Dict[str, Any]:
        """Handle tool calls"""
        tool_name = request.get("name", "")
        arguments = request.get("arguments", {})

        try:
            if tool_name == "configure":
                return await self._handle_configure(arguments)
            elif tool_name == "query_execute":
                return await self._handle_query_execute(arguments)
            elif tool_name == "execute_update":
                return await self._handle_execute_update(arguments)
            elif tool_name == "schema_get":
                return await self._handle_schema_get()
            elif tool_name == "table_list":
                return await self._handle_table_list()
            elif tool_name == "table_describe":
                return await self._handle_table_describe(arguments)
            elif tool_name == "index_list":
                return await self._handle_index_list(arguments)
            elif tool_name == "health":
                return await self._handle_health()
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

    async def _handle_configure(self, args: Dict[str, Any]) -> Dict[str, Any]:
        """Configure database connection"""
        config = DatabaseConfig(
            type=args.get("type"),
            host=args.get("host"),
            port=args.get("port"),
            database=args.get("database"),
            username=args.get("username"),
            password=args.get("password"),
        )
        await self.connection_manager.configure(config)
        self.db_config = config
        return {
            "content": [
                {
                    "type": "text",
                    "text": json.dumps({
                        "success": True,
                        "message": f"Configured {config.type} database: {config.host}:{config.port}/{config.database}"
                    }),
                }
            ]
        }

    async def _handle_query_execute(self, args: Dict[str, Any]) -> Dict[str, Any]:
        """Execute SELECT query"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        sql = args.get("sql", "")
        params = args.get("params", [])
        rows = await self.connection_manager.execute_query(sql, params)
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "data": rows, "count": len(rows)})}]
        }

    async def _handle_execute_update(self, args: Dict[str, Any]) -> Dict[str, Any]:
        """Execute INSERT/UPDATE/DELETE"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        sql = args.get("sql", "")
        params = args.get("params", [])
        result = await self.connection_manager.execute_update(sql, params)
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "affectedRows": result.get("affectedRows", 0)})}]
        }

    async def _handle_schema_get(self) -> Dict[str, Any]:
        """Get database schema"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        schema = await self.connection_manager.get_schema()
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "schema": schema})}]
        }

    async def _handle_table_list(self) -> Dict[str, Any]:
        """List all tables"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        tables = await self.connection_manager.list_tables()
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "tables": tables, "count": len(tables)})}]
        }

    async def _handle_table_describe(self, args: Dict[str, Any]) -> Dict[str, Any]:
        """Describe table structure"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        table = args.get("table", "")
        if not table:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Table name required"})}],
                "isError": True,
            }
        columns = await self.connection_manager.describe_table(table)
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "table": table, "columns": columns})}]
        }

    async def _handle_index_list(self, args: Dict[str, Any]) -> Dict[str, Any]:
        """List indexes for a table"""
        if not self.db_config:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Database not configured. Call configure first."})}],
                "isError": True,
            }
        table = args.get("table", "")
        if not table:
            return {
                "content": [{"type": "text", "text": json.dumps({"error": "Table name required"})}],
                "isError": True,
            }
        indexes = await self.connection_manager.list_indexes(table)
        return {
            "content": [{"type": "text", "text": json.dumps({"success": True, "table": table, "indexes": indexes})}]
        }

    async def _handle_health(self) -> Dict[str, Any]:
        """Health check"""
        return {
            "content": [{
                "type": "text",
                "text": json.dumps({
                    "status": "UP" if self.db_config else "DOWN",
                    "database": self.db_config.type if self.db_config else "not configured",
                })
            }]
        }

    async def handle_list_resources(self, request: ListResourcesRequest) -> Dict[str, Any]:
        """List resources"""
        return {
            "resources": [
                {
                    "uri": "database://config",
                    "name": "Database Configuration",
                    "description": "Current database connection configuration",
                    "mimeType": "application/json",
                }
            ]
        }

    async def handle_list_prompts(self, request: ListPromptsRequest) -> Dict[str, Any]:
        """List prompts"""
        return {
            "prompts": [
                {
                    "name": "sql-query",
                    "description": "Generate a SQL query from natural language",
                    "arguments": [
                        {"name": "database", "description": "Target database type", "required": True},
                        {"name": "intent", "description": "What you want to query", "required": True},
                    ],
                }
            ]
        }


async def main():
    """Start the Database MCP Server"""
    server_instance = DatabaseMCPServer()
    async with stdio_server() as (read_stream, write_stream):
        await app.run(read_stream, write_stream, server_instance)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())