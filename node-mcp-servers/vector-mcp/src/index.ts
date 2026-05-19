/**
 * Vector Database MCP Server
 * Full MCP Protocol Implementation for Vector Databases
 * Supports: Qdrant, Milvus, Chroma, Weaviate, Pinecone, PGvector, OpenSearch
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
import { vectorConnectionManager, VectorConfig, VectorDBType } from './vector-db.js';

// Server instance
const server = new Server(
  {
    name: 'xarch-vector-mcp',
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

// Current configuration
let currentConfig: VectorConfig | null = null;

/**
 * List available tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'configure',
        description: 'Configure vector database connection',
        inputSchema: {
          type: 'object',
          properties: {
            type: {
              type: 'string',
              enum: ['qdrant', 'milvus', 'chroma', 'weaviate', 'pinecone', 'pgvector', 'opensearch'],
              description: 'Vector database type',
            },
            host: { type: 'string', description: 'Database host' },
            port: { type: 'number', description: 'Database port' },
            ssl: { type: 'boolean', description: 'Use SSL/TLS', default: false },
            username: { type: 'string', description: 'Username (for Weaviate, OpenSearch, PGvector)' },
            password: { type: 'string', description: 'Password' },
            apiKey: { type: 'string', description: 'API key (for Qdrant, Pinecone)' },
            environment: { type: 'string', description: 'Environment (for Pinecone)' },
            scheme: { type: 'string', description: 'Scheme (for Weaviate, default http)' },
            database: { type: 'string', description: 'Database name (for PGvector)' },
          },
          required: ['type', 'host', 'port'],
        },
      },
      {
        name: 'create_collection',
        description: 'Create a new vector collection/index',
        inputSchema: {
          type: 'object',
          properties: {
            name: { type: 'string', description: 'Collection name' },
            dimension: { type: 'number', description: 'Vector dimension (e.g., 1536 for OpenAI, 768 for BERT)' },
          },
          required: ['name', 'dimension'],
        },
      },
      {
        name: 'upsert',
        description: 'Insert or update vectors in a collection',
        inputSchema: {
          type: 'object',
          properties: {
            collection: { type: 'string', description: 'Collection name' },
            vectors: {
              type: 'array',
              description: 'Array of vector objects with id, vector, content, metadata',
              items: {
                type: 'object',
                properties: {
                  id: { type: 'string', description: 'Document ID' },
                  vector: { type: 'array', items: { type: 'number' }, description: 'Embedding vector' },
                  content: { type: 'string', description: 'Text content' },
                  metadata: { type: 'object', description: 'Additional metadata' },
                },
                required: ['vector', 'content'],
              },
            },
          },
          required: ['collection', 'vectors'],
        },
      },
      {
        name: 'search',
        description: 'Search for similar vectors using vector similarity',
        inputSchema: {
          type: 'object',
          properties: {
            collection: { type: 'string', description: 'Collection name' },
            vector: {
              type: 'array',
              items: { type: 'number' },
              description: 'Query vector',
            },
            limit: { type: 'number', description: 'Maximum results to return', default: 10 },
            filter: { type: 'object', description: 'Metadata filter conditions' },
          },
          required: ['collection', 'vector'],
        },
      },
      {
        name: 'text_search',
        description: 'Search for similar content using text query (for databases with built-in embedding)',
        inputSchema: {
          type: 'object',
          properties: {
            collection: { type: 'string', description: 'Collection name' },
            query: { type: 'string', description: 'Text search query' },
            limit: { type: 'number', description: 'Maximum results to return', default: 10 },
          },
          required: ['collection', 'query'],
        },
      },
      {
        name: 'delete',
        description: 'Delete vectors from a collection',
        inputSchema: {
          type: 'object',
          properties: {
            collection: { type: 'string', description: 'Collection name' },
            id: { type: 'string', description: 'Document ID to delete' },
          },
          required: ['collection', 'id'],
        },
      },
      {
        name: 'list_collections',
        description: 'List all available collections/indexes',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'get_stats',
        description: 'Get collection/index statistics',
        inputSchema: {
          type: 'object',
          properties: {
            collection: { type: 'string', description: 'Collection name' },
          },
          required: ['collection'],
        },
      },
      {
        name: 'health',
        description: 'Check vector database health and status',
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
      case 'configure': {
        const config: VectorConfig = {
          type: args?.type as VectorDBType,
          host: args?.host as string,
          port: args?.port as number,
          ssl: args?.ssl as boolean || false,
          username: args?.username as string,
          password: args?.password as string,
          apiKey: args?.apiKey as string,
          environment: args?.environment as string,
          scheme: args?.scheme as string,
          database: args?.database as string,
        };
        await vectorConnectionManager.configure(config);
        currentConfig = config;
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                message: `Configured ${config.type} vector database: ${config.host}:${config.port}`,
                supported: ['qdrant', 'milvus', 'chroma', 'weaviate', 'pinecone', 'pgvector', 'opensearch'],
              }),
            },
          ],
        };
      }

      case 'create_collection': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const name = args?.name as string;
        const dimension = args?.dimension as number;
        if (!name || !dimension) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'name and dimension are required' }) }], isError: true };
        }
        await vectorConnectionManager.createCollection(name, dimension);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, message: `Collection '${name}' created with dimension ${dimension}` }),
            },
          ],
        };
      }

      case 'upsert': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collection = args?.collection as string;
        const vectors = args?.vectors as any[];
        if (!collection || !vectors) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'collection and vectors are required' }) }], isError: true };
        }
        await vectorConnectionManager.upsert(collection, vectors);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, message: `Upserted ${vectors.length} vectors to '${collection}'` }),
            },
          ],
        };
      }

      case 'search': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collection = args?.collection as string;
        const vector = args?.vector as number[];
        const limit = (args?.limit as number) || 10;
        const filter = args?.filter as any;
        if (!collection || !vector) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'collection and vector are required' }) }], isError: true };
        }
        const results = await vectorConnectionManager.search(collection, vector, limit, filter);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, results, count: results.length }),
            },
          ],
        };
      }

      case 'text_search': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collection = args?.collection as string;
        const query = args?.query as string;
        const limit = (args?.limit as number) || 10;
        if (!collection || !query) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'collection and query are required' }) }], isError: true };
        }
        const results = await vectorConnectionManager.textSearch(collection, query, limit);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, results, count: results.length }),
            },
          ],
        };
      }

      case 'delete': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collection = args?.collection as string;
        const id = args?.id as string;
        if (!collection || !id) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'collection and id are required' }) }], isError: true };
        }
        await vectorConnectionManager.delete(collection, id);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, message: `Deleted document '${id}' from '${collection}'` }),
            },
          ],
        };
      }

      case 'list_collections': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collections = await vectorConnectionManager.listCollections();
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({ success: true, collections, count: collections.length }),
            },
          ],
        };
      }

      case 'get_stats': {
        if (!currentConfig) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'Not configured. Call configure first.' }) }], isError: true };
        }
        const collection = args?.collection as string;
        if (!collection) {
          return { content: [{ type: 'text', text: JSON.stringify({ error: 'collection is required' }) }], isError: true };
        }
        const collections = await vectorConnectionManager.listCollections();
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                success: true,
                collection,
                exists: collections.includes(collection),
                totalCollections: collections.length,
              }),
            },
          ],
        };
      }

      case 'health': {
        if (!currentConfig) {
          return {
            content: [{ type: 'text', text: JSON.stringify({ status: 'DOWN', message: 'Not configured' }) }],
            isError: true,
          };
        }
        const health = await vectorConnectionManager.healthCheck();
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify({
                status: health.status,
                type: health.type,
                collections: health.collections,
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
        uri: 'vector://config',
        name: 'Vector Database Configuration',
        description: 'Current vector database connection configuration',
        mimeType: 'application/json',
      },
      {
        uri: 'vector://collections',
        name: 'Vector Collections',
        description: 'List of available vector collections',
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
        name: 'semantic-search',
        description: 'Perform semantic search across vector collections',
        arguments: [
          {
            name: 'collection',
            description: 'Target collection name',
            required: true,
          },
          {
            name: 'query',
            description: 'Search query in natural language',
            required: true,
          },
          {
            name: 'limit',
            description: 'Maximum number of results',
            required: false,
          },
        ],
      },
      {
        name: 'similarity-search',
        description: 'Find similar vectors using embedding',
        arguments: [
          {
            name: 'collection',
            description: 'Target collection name',
            required: true,
          },
          {
            name: 'embedding',
            description: 'Vector embedding array',
            required: true,
          },
          {
            name: 'limit',
            description: 'Maximum number of results',
            required: false,
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
      name: 'xarch-vector-mcp',
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
  console.error('Vector Database MCP Server started on stdio');
  console.error('Supported: Qdrant, Milvus, Chroma, Weaviate, Pinecone, PGvector, OpenSearch');
}

main().catch(console.error);