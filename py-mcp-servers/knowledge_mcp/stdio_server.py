"""
Simple stdio server for Python MCP implementation
"""

import json
import sys
from typing import Any, Dict


async def run_stdio_server(handler):
    """Run MCP server over stdio"""

    while True:
        try:
            line = sys.stdin.readline()
            if not line:
                break

            request = json.loads(line.strip())
            method = request.get("method", "")
            req_id = request.get("id")

            result = None
            error = None

            try:
                if method == "initialize":
                    result = await handler.handle_initialize(request)

                elif method == "tools/list":
                    result = await handler.handle_list_tools(request)

                elif method == "tools/call":
                    params = request.get("params", {})
                    result = await handler.handle_call_tool(params)

                elif method == "resources/list":
                    result = await handler.handle_list_resources(request)

                elif method == "prompts/list":
                    result = await handler.handle_list_prompts(request)

                else:
                    error = {"code": -32601, "message": f"Unknown method: {method}"}

            except Exception as e:
                error = {"code": -32603, "message": str(e)}

            # Send response
            if error:
                response = {"jsonrpc": "2.0", "id": req_id, "error": error}
            else:
                response = {"jsonrpc": "2.0", "id": req_id, "result": result}

            print(json.dumps(response), flush=True)

        except Exception as e:
            error_response = {
                "jsonrpc": "2.0",
                "id": None,
                "error": {"code": -32700, "message": f"Parse error: {str(e)}"}
            }
            print(json.dumps(error_response), flush=True)