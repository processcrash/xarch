#!/usr/bin/env python3
"""
Database MCP Server - Python Implementation
Run with: python -m database_mcp
"""

import json
import sys
from .server import DatabaseMCPServer


async def main():
    """Main entry point for Database MCP Server"""
    server = DatabaseMCPServer()

    # Simple stdio communication
    while True:
        try:
            line = sys.stdin.readline()
            if not line:
                break

            request = json.loads(line.strip())
            method = request.get("method", "")

            if method == "initialize":
                result = await server.handle_initialize(request)
                response = {"jsonrpc": "2.0", "id": request.get("id"), "result": result}
                print(json.dumps(response))
                sys.stdout.flush()

            elif method == "tools/list":
                result = await server.handle_list_tools(request)
                response = {"jsonrpc": "2.0", "id": request.get("id"), "result": result}
                print(json.dumps(response))
                sys.stdout.flush()

            elif method == "tools/call":
                result = await server.handle_call_tool(request.get("params", {}))
                response = {"jsonrpc": "2.0", "id": request.get("id"), "result": result}
                print(json.dumps(response))
                sys.stdout.flush()

            elif method == "resources/list":
                result = await server.handle_list_resources(request)
                response = {"jsonrpc": "2.0", "id": request.get("id"), "result": result}
                print(json.dumps(response))
                sys.stdout.flush()

            elif method == "prompts/list":
                result = await server.handle_list_prompts(request)
                response = {"jsonrpc": "2.0", "id": request.get("id"), "result": result}
                print(json.dumps(response))
                sys.stdout.flush()

        except Exception as e:
            error_response = {
                "jsonrpc": "2.0",
                "id": None,
                "error": {"code": -32603, "message": str(e)}
            }
            print(json.dumps(error_response))
            sys.stdout.flush()


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())