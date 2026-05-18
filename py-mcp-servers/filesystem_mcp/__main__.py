#!/usr/bin/env python3
"""
Filesystem MCP Server - Python Implementation
Run with: python -m filesystem_mcp
"""

import json
import sys
from .server import FilesystemMCPServer
from .stdio_server import run_stdio_server


async def main():
    """Main entry point for Filesystem MCP Server"""
    server = FilesystemMCPServer()
    await run_stdio_server(server)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())