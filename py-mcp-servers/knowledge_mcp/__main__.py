#!/usr/bin/env python3
"""
Knowledge Base MCP Server - Python Implementation
Run with: python -m knowledge_mcp
"""

import json
import sys
from .server import KnowledgeMCPServer
from .stdio_server import run_stdio_server


async def main():
    """Main entry point for Knowledge Base MCP Server"""
    server = KnowledgeMCPServer()
    await run_stdio_server(server)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())