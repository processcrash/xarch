"""
Knowledge Base MCP Server - Core implementation with RAG
"""

import json
from typing import Any, Dict, List, Optional
from .knowledge_base import KnowledgeBase


class KnowledgeMCPServer:
    """Knowledge Base MCP Server with RAG support"""

    def __init__(self):
        self.knowledge_base = KnowledgeBase()

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
                "name": "xarch-knowledge-mcp",
                "version": "1.0.0",
            },
        }

    async def handle_list_tools(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """List available tools"""
        return {
            "tools": [
                {
                    "name": "kb_index_document",
                    "description": "Index a document into the knowledge base for semantic search",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "id": {"type": "string", "description": "Optional document ID (auto-generated if not provided)"},
                            "title": {"type": "string", "description": "Document title"},
                            "content": {"type": "string", "description": "Document content to index"},
                            "type": {"type": "string", "description": "Document type (e.g., 'article', 'policy', 'faq')"},
                            "metadata": {"type": "object", "description": "Additional metadata"},
                        },
                        "required": ["title", "content"],
                    },
                },
                {
                    "name": "kb_index_file",
                    "description": "Index a file (PDF, Markdown, TXT) into the knowledge base",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "id": {"type": "string", "description": "Optional document ID"},
                            "title": {"type": "string", "description": "File title"},
                            "content": {"type": "string", "description": "File content"},
                            "fileType": {
                                "type": "string",
                                "enum": ["pdf", "md", "txt", "html", "doc"],
                                "description": "File type",
                            },
                            "metadata": {"type": "object", "description": "Additional metadata"},
                        },
                        "required": ["title", "content", "fileType"],
                    },
                },
                {
                    "name": "kb_search",
                    "description": "Semantic search across the knowledge base using natural language",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "Search query in natural language"},
                            "topK": {"type": "number", "description": "Number of results to return (default: 5)"},
                            "minScore": {"type": "number", "description": "Minimum similarity score threshold (0-1)"},
                        },
                        "required": ["query"],
                    },
                },
                {
                    "name": "kb_get_document",
                    "description": "Get a specific document by ID",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "documentId": {"type": "string", "description": "Document ID"},
                        },
                        "required": ["documentId"],
                    },
                },
                {
                    "name": "kb_delete",
                    "description": "Delete a document from the knowledge base",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "documentId": {"type": "string", "description": "Document ID to delete"},
                        },
                        "required": ["documentId"],
                    },
                },
                {
                    "name": "kb_list",
                    "description": "List all documents in the knowledge base",
                    "inputSchema": {"type": "object", "properties": {}},
                },
                {
                    "name": "kb_update",
                    "description": "Update an existing document",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "documentId": {"type": "string", "description": "Document ID to update"},
                            "title": {"type": "string", "description": "New title"},
                            "content": {"type": "string", "description": "New content"},
                            "metadata": {"type": "object", "description": "Updated metadata"},
                        },
                        "required": ["documentId"],
                    },
                },
                {
                    "name": "kb_stats",
                    "description": "Get knowledge base statistics",
                    "inputSchema": {"type": "object", "properties": {}},
                },
                {
                    "name": "health",
                    "description": "Check knowledge base service health",
                    "inputSchema": {"type": "object", "properties": {}},
                },
            ]
        }

    async def handle_call_tool(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """Handle tool calls"""
        tool_name = request.get("name", "")
        arguments = request.get("arguments", {})

        try:
            if tool_name == "kb_index_document":
                result = self.knowledge_base.index_document(
                    id=arguments.get("id"),
                    title=arguments.get("title", ""),
                    content=arguments.get("content", ""),
                    doc_type=arguments.get("type"),
                    metadata=arguments.get("metadata"),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "kb_index_file":
                result = self.knowledge_base.index_file(
                    id=arguments.get("id"),
                    title=arguments.get("title", ""),
                    content=arguments.get("content", ""),
                    file_type=arguments.get("fileType", ""),
                    metadata=arguments.get("metadata"),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "kb_search":
                results = self.knowledge_base.search(
                    query=arguments.get("query", ""),
                    top_k=arguments.get("topK", 5),
                    min_score=arguments.get("minScore", 0.0),
                )
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "success": True,
                            "results": results,
                            "count": len(results),
                        }),
                    }],
                }

            elif tool_name == "kb_get_document":
                doc = self.knowledge_base.get_document(arguments.get("documentId", ""))
                if not doc:
                    return {
                        "content": [{"type": "text", "text": json.dumps({"error": "Document not found"})}],
                        "isError": True,
                    }
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, "document": doc})}],
                }

            elif tool_name == "kb_delete":
                result = self.knowledge_base.delete_document(arguments.get("documentId", ""))
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": result["success"]})}],
                }

            elif tool_name == "kb_list":
                result = self.knowledge_base.list_documents()
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, **result})}],
                }

            elif tool_name == "kb_update":
                result = self.knowledge_base.update_document(
                    document_id=arguments.get("documentId", ""),
                    title=arguments.get("title"),
                    content=arguments.get("content"),
                    metadata=arguments.get("metadata"),
                )
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": result["success"]})}],
                }

            elif tool_name == "kb_stats":
                stats = self.knowledge_base.get_stats()
                return {
                    "content": [{"type": "text", "text": json.dumps({"success": True, "stats": stats})}],
                }

            elif tool_name == "health":
                return {
                    "content": [{
                        "type": "text",
                        "text": json.dumps({
                            "status": "UP",
                            "documents": self.knowledge_base.get_stats().get("totalDocuments", 0),
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
                    "uri": "knowledge://stats",
                    "name": "Knowledge Base Statistics",
                    "description": "Current knowledge base statistics",
                    "mimeType": "application/json",
                }
            ]
        }

    async def handle_list_prompts(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """List prompts"""
        return {
            "prompts": [
                {
                    "name": "rag-search",
                    "description": "Search knowledge base and generate response",
                    "arguments": [
                        {"name": "query", "description": "User query", "required": True},
                    ],
                }
            ]
        }


async def main():
    """Start the Knowledge Base MCP Server"""
    server = KnowledgeMCPServer()
    import sys
    from .stdio_server import run_stdio_server

    await run_stdio_server(server)


if __name__ == "__main__":
    import asyncio
    asyncio.run(main())