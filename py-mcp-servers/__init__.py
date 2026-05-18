"""
xarch MCP Servers - Python Implementation
==========================================

This package provides Python implementations of the xarch MCP servers
using the Model Context Protocol (MCP).

Servers
-------

1. database_mcp
   Multi-database support: MySQL, PostgreSQL, MongoDB, SQL Server
   Run: python -m database_mcp

2. knowledge_mcp
   RAG-based knowledge base with semantic search
   Run: python -m knowledge_mcp

3. filesystem_mcp
   Secure file operations with path traversal prevention
   Run: python -m filesystem_mcp

Installation
-----------

Install optional dependencies as needed:

    pip install mysql-connector-python  # MySQL support
    pip install psycopg2-binary         # PostgreSQL support
    pip install pymongo                 # MongoDB support
    pip install pymssql                # SQL Server support

Usage
-----

Each server communicates over stdio using JSON-RPC 2.0 protocol.

Example interaction:

    {"jsonrpc": "2.0", "method": "initialize", "id": 1}

Response:

    {"jsonrpc": "2.0", "id": 1, "result": {...}}
"""

from .database_mcp import DatabaseMCPServer
from .knowledge_mcp import KnowledgeMCPServer
from .filesystem_mcp import FilesystemMCPServer

__all__ = [
    "DatabaseMCPServer",
    "KnowledgeMCPServer",
    "FilesystemMCPServer",
]