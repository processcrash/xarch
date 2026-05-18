/**
 * Filesystem MCP Server
 * Full MCP Protocol Implementation with secure file operations
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
import { fileOperations } from './file-operations.js';

const server = new Server(
  {
    name: 'xarch-filesystem-mcp',
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
        name: 'list_directory',
        description: 'List contents of a directory',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'Directory path to list',
            },
            recursive: {
              type: 'boolean',
              description: 'List subdirectories recursively',
            },
          },
          required: ['path'],
        },
      },
      {
        name: 'read_file',
        description: 'Read contents of a file',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'File path to read',
            },
          },
          required: ['path'],
        },
      },
      {
        name: 'write_file',
        description: 'Write content to a file',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'File path to write',
            },
            content: {
              type: 'string',
              description: 'Content to write',
            },
          },
          required: ['path', 'content'],
        },
      },
      {
        name: 'delete',
        description: 'Delete a file or directory',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'Path to delete',
            },
          },
          required: ['path'],
        },
      },
      {
        name: 'create_directory',
        description: 'Create a new directory',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'Directory path to create',
            },
          },
          required: ['path'],
        },
      },
      {
        name: 'search_files',
        description: 'Search for files matching a pattern',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'Directory to search in',
            },
            pattern: {
              type: 'string',
              description: 'Search pattern (e.g., *.txt, *.md)',
            },
            recursive: {
              type: 'boolean',
              description: 'Search subdirectories',
            },
          },
          required: ['path', 'pattern'],
        },
      },
      {
        name: 'get_file_info',
        description: 'Get information about a file or directory',
        inputSchema: {
          type: 'object',
          properties: {
            path: {
              type: 'string',
              description: 'File or directory path',
            },
          },
          required: ['path'],
        },
      },
      {
        name: 'copy_file',
        description: 'Copy a file to a new location',
        inputSchema: {
          type: 'object',
          properties: {
            source: {
              type: 'string',
              description: 'Source file path',
            },
            destination: {
              type: 'string',
              description: 'Destination file path',
            },
          },
          required: ['source', 'destination'],
        },
      },
      {
        name: 'move_file',
        description: 'Move a file to a new location',
        inputSchema: {
          type: 'object',
          properties: {
            source: {
              type: 'string',
              description: 'Source file path',
            },
            destination: {
              type: 'string',
              description: 'Destination file path',
            },
          },
          required: ['source', 'destination'],
        },
      },
      {
        name: 'health',
        description: 'Check filesystem service health',
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
      case 'list_directory': {
        const files = await fileOperations.listDirectory(
          args?.path as string,
          args?.recursive as boolean
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                path: args?.path,
                files,
                count: files.length,
              }),
            },
          ],
        };
      }

      case 'read_file': {
        const content = await fileOperations.readFile(args?.path as string);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                path: args?.path,
                content,
              }),
            },
          ],
        };
      }

      case 'write_file': {
        const result = await fileOperations.writeFile(
          args?.path as string,
          args?.content as string
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, ...result }),
            },
          ],
        };
      }

      case 'delete': {
        const result = await fileOperations.delete(args?.path as string);
        return {
          content: [{ type: 'text', text: JSON.stringify({ success: true }) }],
        };
      }

      case 'create_directory': {
        const result = await fileOperations.createDirectory(args?.path as string);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, ...result }),
            },
          ],
        };
      }

      case 'search_files': {
        const files = await fileOperations.searchFiles(
          args?.path as string,
          args?.pattern as string,
          args?.recursive as boolean
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                path: args?.path,
                pattern: args?.pattern,
                files,
                count: files.length,
              }),
            },
          ],
        };
      }

      case 'get_file_info': {
        const info = await fileOperations.getFileInfo(args?.path as string);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, file: info }),
            },
          ],
        };
      }

      case 'copy_file': {
        const result = await fileOperations.copyFile(
          args?.source as string,
          args?.destination as string
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, ...result }),
            },
          ],
        };
      }

      case 'move_file': {
        const result = await fileOperations.moveFile(
          args?.source as string,
          args?.destination as string
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, ...result }),
            },
          ],
        };
      }

      case 'health': {
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                status: 'UP',
                allowedPath: '/tmp/xarch-files',
              }),
            },
          ],
        };
      }

      default:
        return {
          content: [
            { type: 'text', text: JSON.stringify({ error: `Unknown tool: ${name}` }) },
          ],
          isError: true,
        };
    }
  } catch (error) {
    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify({
            error: error instanceof Error ? error.message : String(error),
          }),
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
        uri: 'filesystem://config',
        name: 'Filesystem Configuration',
        description: 'Current filesystem MCP configuration',
        mimeType: 'application/json',
      },
    ],
  };
});

server.setRequestHandler(ListPromptsRequestSchema, async () => {
  return {
    prompts: [
      {
        name: 'file-search',
        description: 'Search and read files matching criteria',
        arguments: [
          {
            name: 'pattern',
            description: 'File pattern to search for',
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
      name: 'xarch-filesystem-mcp',
      version: '1.0.0',
    },
  };
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Filesystem MCP Server started on stdio');
}

main().catch(console.error);