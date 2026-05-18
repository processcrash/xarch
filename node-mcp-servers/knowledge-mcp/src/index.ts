/**
 * Knowledge Base MCP Server with RAG
 * Full MCP Protocol Implementation
 */

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ListResourcesRequestSchema,
  ListPromptsRequestSchema,
  InitializeRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import { knowledgeBase, SearchResult } from './knowledge-base.js';

const server = new Server(
  {
    name: 'xarch-knowledge-mcp',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
      resources: {},
      prompts: {},
    },
  }
);

/**
 * List available tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'kb_index_document',
        description: 'Index a document into the knowledge base for semantic search',
        inputSchema: {
          type: 'object',
          properties: {
            id: {
              type: 'string',
              description: 'Optional document ID (auto-generated if not provided)',
            },
            title: {
              type: 'string',
              description: 'Document title',
            },
            content: {
              type: 'string',
              description: 'Document content to index',
            },
            type: {
              type: 'string',
              description: 'Document type (e.g., "article", "policy", "faq")',
            },
            metadata: {
              type: 'object',
              description: 'Additional metadata',
              additionalProperties: true,
            },
          },
          required: ['title', 'content'],
        },
      },
      {
        name: 'kb_index_file',
        description: 'Index a file (PDF, Markdown, TXT) into the knowledge base',
        inputSchema: {
          type: 'object',
          properties: {
            id: {
              type: 'string',
              description: 'Optional document ID',
            },
            title: {
              type: 'string',
              description: 'File title',
            },
            content: {
              type: 'string',
              description: 'File content',
            },
            fileType: {
              type: 'string',
              enum: ['pdf', 'md', 'txt', 'html', 'doc'],
              description: 'File type',
            },
            metadata: {
              type: 'object',
              description: 'Additional metadata',
            },
          },
          required: ['title', 'content', 'fileType'],
        },
      },
      {
        name: 'kb_search',
        description: 'Semantic search across the knowledge base using natural language',
        inputSchema: {
          type: 'object',
          properties: {
            query: {
              type: 'string',
              description: 'Search query in natural language',
            },
            topK: {
              type: 'number',
              description: 'Number of results to return (default: 5)',
            },
            minScore: {
              type: 'number',
              description: 'Minimum similarity score threshold (0-1)',
            },
          },
          required: ['query'],
        },
      },
      {
        name: 'kb_get_document',
        description: 'Get a specific document by ID',
        inputSchema: {
          type: 'object',
          properties: {
            documentId: {
              type: 'string',
              description: 'Document ID',
            },
          },
          required: ['documentId'],
        },
      },
      {
        name: 'kb_delete',
        description: 'Delete a document from the knowledge base',
        inputSchema: {
          type: 'object',
          properties: {
            documentId: {
              type: 'string',
              description: 'Document ID to delete',
            },
          },
          required: ['documentId'],
        },
      },
      {
        name: 'kb_list',
        description: 'List all documents in the knowledge base',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'kb_update',
        description: 'Update an existing document',
        inputSchema: {
          type: 'object',
          properties: {
            documentId: {
              type: 'string',
              description: 'Document ID to update',
            },
            title: {
              type: 'string',
              description: 'New title',
            },
            content: {
              type: 'string',
              description: 'New content',
            },
            metadata: {
              type: 'object',
              description: 'Updated metadata',
            },
          },
          required: ['documentId'],
        },
      },
      {
        name: 'kb_stats',
        description: 'Get knowledge base statistics',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'health',
        description: 'Check knowledge base service health',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
    ],
  };
});

/**
 * Handle tool calls
 */
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case 'kb_index_document': {
        const result = await knowledgeBase.indexDocument({
          id: args?.id as string,
          title: args?.title as string,
          content: args?.content as string,
          type: args?.type as string,
          metadata: args?.metadata as Record<string, any>,
        });
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true, ...result }) }],
        };
      }

      case 'kb_index_file': {
        const result = await knowledgeBase.indexFile({
          id: args?.id as string,
          title: args?.title as string,
          content: args?.content as string,
          fileType: args?.fileType as string,
          metadata: args?.metadata as Record<string, any>,
        });
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true, ...result }) }],
        };
      }

      case 'kb_search': {
        const results = await knowledgeBase.search({
          query: args?.query as string,
          topK: args?.topK as number,
          minScore: args?.minScore as number,
        });
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                results,
                count: results.length,
              }),
            },
          ],
        };
      }

      case 'kb_get_document': {
        const doc = await knowledgeBase.getDocument(args?.documentId as string);
        if (!doc) {
          return {
            content: [{ type: 'text', text: JSON.stringify({ error: 'Document not found' }) }],
            isError: true,
          };
        }
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true, document: doc }) }],
        };
      }

      case 'kb_delete': {
        const result = await knowledgeBase.deleteDocument(args?.documentId as string);
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: result.success }) }],
        };
      }

      case 'kb_list': {
        const result = await knowledgeBase.listDocuments();
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true, ...result }) }],
        };
      }

      case 'kb_update': {
        const result = await knowledgeBase.updateDocument(args?.documentId as string, {
          title: args?.title as string,
          content: args?.content as string,
          metadata: args?.metadata as Record<string, any>,
        });
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: result.success }) }],
        };
      }

      case 'kb_stats': {
        const stats = knowledgeBase.getStats();
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true, stats }) }],
        };
      }

      case 'health': {
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                status: 'UP',
                documents: knowledgeBase.getStats().totalDocuments,
              }),
            },
          ],
        };
      }

      default:
        return {
          content: [{ type: 'text', text: JSON.stringify({ error: `Unknown tool: ${name}` }) }],
          isError: true,
        };
    }
  } catch (error) {
    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify({ error: error instanceof Error ? error.message : String(error) }),
        },
      ],
      isError: true,
    };
  }
});

server.setRequestHandler(ListResourcesRequestSchema, async () => {
  return {
    resources: [
      {
        uri: 'knowledge://stats',
        name: 'Knowledge Base Statistics',
        description: 'Current knowledge base statistics',
        mimeType: 'application/json',
      },
    ],
  };
});

server.setRequestHandler(ListPromptsRequestSchema, async () => {
  return {
    prompts: [
      {
        name: 'rag-search',
        description: 'Search knowledge base and generate response',
        arguments: [
          {
            name: 'query',
            description: 'User query',
            required: true,
          },
        ],
      },
    ],
  };
});

server.setRequestHandler(InitializeRequestSchema, async (request) => {
  return {
    protocolVersion: '2024-11-05',
    capabilities: {
      tools: {},
      resources: {},
      prompts: {},
    },
    serverInfo: {
      name: 'xarch-knowledge-mcp',
      version: '1.0.0',
    },
  };
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Knowledge Base MCP Server started on stdio');
}

main().catch(console.error);