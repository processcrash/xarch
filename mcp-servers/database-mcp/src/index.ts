/**
 * Database MCP Server
 * Full MCP Protocol Implementation using @modelcontextprotocol/sdk
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
import { connectionManager, DatabaseConfig } from './database.js';

// Server instance
const server = new Server(
  {
    name: 'xarch-database-mcp',
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

// Database configuration
let dbConfig: DatabaseConfig | null = null;

/**
 * List available tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'query_execute',
        description: 'Execute a SELECT query on the configured database',
        inputSchema: {
          type: 'object',
          properties: {
            sql: {
              type: 'string',
              description: 'SQL SELECT query to execute',
            },
            params: {
              type: 'array',
              description: 'Query parameters',
              items: {},
            },
          },
          required: ['sql'],
        },
      },
      {
        name: 'schema_get',
        description: 'Get database schema information including all tables',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'table_list',
        description: 'List all tables in the database',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'table_describe',
        description: 'Get the structure of a specific table',
        inputSchema: {
          type: 'object',
          properties: {
            table: {
              type: 'string',
              description: 'Table name to describe',
            },
          },
          required: ['table'],
        },
      },
      {
        name: 'index_list',
        description: 'List all indexes for a specific table',
        inputSchema: {
          type: 'object',
          properties: {
            table: {
              type: 'string',
              description: 'Table name',
            },
          },
          required: ['table'],
        },
      },
      {
        name: 'execute_update',
        description: 'Execute an INSERT, UPDATE, or DELETE statement',
        inputSchema: {
          type: 'object',
          properties: {
            sql: {
              type: 'string',
              description: 'SQL INSERT/UPDATE/DELETE query',
            },
            params: {
              type: 'array',
              description: 'Query parameters',
              items: {},
            },
          },
          required: ['sql'],
        },
      },
      {
        name: 'configure',
        description: 'Configure database connection',
        inputSchema: {
          type: 'object',
          properties: {
            type: {
              type: 'string',
              enum: ['mysql', 'postgresql', 'mongodb', 'sqlserver'],
              description: 'Database type',
            },
            host: {
              type: 'string',
              description: 'Database host',
            },
            port: {
              type: 'number',
              description: 'Database port',
            },
            database: {
              type: 'string',
              description: 'Database name',
            },
            username: {
              type: 'string',
              description: 'Database username',
            },
            password: {
              type: 'string',
              description: 'Database password',
            },
          },
          required: ['type', 'host', 'port', 'database', 'username', 'password'],
        },
      },
      {
        name: 'health',
        description: 'Check database connection health',
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
      case 'query_execute': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const sql = args?.sql as string;
        const params = (args?.params as any[]) || [];
        const rows = await connectionManager.executeQuery(sql, params);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, data: rows, count: rows.length }),
            },
          ],
        };
      }

      case 'execute_update': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const sql = args?.sql as string;
        const params = (args?.params as any[]) || [];
        const result = await connectionManager.executeUpdate(sql, params);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, affectedRows: result.affectedRows }),
            },
          ],
        };
      }

      case 'schema_get': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const schema = await connectionManager.getSchema();
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, schema }),
            },
          ],
        };
      }

      case 'table_list': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const tables = await connectionManager.listTables();
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, tables, count: tables.length }),
            },
          ],
        };
      }

      case 'table_describe': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const table = args?.table as string;
        if (!table) {
          return {
            content: [{ type: 'text', text: JSON.stringify({ error: 'Table name required' }) }],
            isError: true,
          };
        }
        const columns = await connectionManager.describeTable(table);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, table, columns }),
            },
          ],
        };
      }

      case 'index_list': {
        if (!dbConfig) {
          return {
            content: [
              {
                type: 'text',
                text: JSON.stringify({ error: 'Database not configured. Call configure first.' }),
              },
            ],
            isError: true,
          };
        }
        const table = args?.table as string;
        if (!table) {
          return {
            content: [{ type: 'text', text: JSON.stringify({ error: 'Table name required' }) }],
            isError: true,
          };
        }
        const indexes = await connectionManager.listIndexes(table);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, table, indexes }),
            },
          ],
        };
      }

      case 'configure': {
        const config: DatabaseConfig = {
          type: args?.type as DatabaseConfig['type'],
          host: args?.host as string,
          port: args?.port as number,
          database: args?.database as string,
          username: args?.username as string,
          password: args?.password as string,
        };
        await connectionManager.configure(config);
        dbConfig = config;
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, message: `Configured ${config.type} database: ${config.host}:${config.port}/${config.database}` }),
            },
          ],
        };
      }

      case 'health': {
        const isHealthy = dbConfig !== null;
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                status: isHealthy ? 'UP' : 'DOWN',
                database: dbConfig ? dbConfig.type : 'not configured',
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

/**
 * List resources
 */
server.setRequestHandler(ListResourcesRequestSchema, async () => {
  return {
    resources: [
      {
        uri: 'database://config',
        name: 'Database Configuration',
        description: 'Current database connection configuration',
        mimeType: 'application/json',
      },
    ],
  };
});

/**
 * List prompts
 */
server.setRequestHandler(ListPromptsRequestSchema, async () => {
  return {
    prompts: [
      {
        name: 'sql-query',
        description: 'Generate a SQL query from natural language',
        arguments: [
          {
            name: 'database',
            description: 'Target database type',
            required: true,
          },
          {
            name: 'intent',
            description: 'What you want to query',
            required: true,
          },
        ],
      },
    ],
  };
});

/**
 * Initialize server
 */
server.setRequestHandler(InitializeRequestSchema, async (request) => {
  return {
    protocolVersion: '2024-11-05',
    capabilities: {
      tools: {},
      resources: {},
      prompts: {},
    },
    serverInfo: {
      name: 'xarch-database-mcp',
      version: '1.0.0',
    },
  };
});

/**
 * Start the server
 */
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Database MCP Server started on stdio');
}

main().catch(console.error);