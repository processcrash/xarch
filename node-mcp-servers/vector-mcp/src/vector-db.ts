/**
 * Vector Database Connection Manager
 * Supports: Qdrant, Milvus, Chroma, Weaviate, Pinecone, PGvector, OpenSearch
 */

export type VectorDBType = 'qdrant' | 'milvus' | 'chroma' | 'weaviate' | 'pinecone' | 'pgvector' | 'opensearch';

export interface VectorConfig {
  type: VectorDBType;
  host: string;
  port: number;
  ssl: boolean;
  username?: string;
  password?: string;
  collection?: string;
  dimension?: number;
  // Qdrant specific
  apiKey?: string;
  // Pinecone specific
  environment?: string;
  // Weaviate specific
  scheme?: string;
  // PGvector specific
  database?: string;
}

export interface VectorDocument {
  id?: string;
  vector?: number[];
  content?: string;
  metadata?: Record<string, any>;
  score?: number;
}

export interface SearchResult {
  id: string;
  score: number;
  content?: string;
  metadata?: Record<string, any>;
}

class QdrantClient {
  private url: string;
  private apiKey?: string;

  constructor(config: VectorConfig) {
    this.url = `http://${config.host}:${config.port}`;
    this.apiKey = config.apiKey;
  }

  async configure(config: VectorConfig): Promise<void> {
    this.url = config.ssl ? `https://${config.host}:${config.port}` : `http://${config.host}:${config.port}`;
    this.apiKey = config.apiKey;
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    const response = await fetch(`${this.url}/collections/${name}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(this.apiKey ? { 'api-key': this.apiKey } : {}),
      },
      body: JSON.stringify({
        vectors: { size: dimension, distance: 'Cosine' },
      }),
    });
    if (!response.ok) throw new Error(`Qdrant create collection failed: ${await response.text()}`);
  }

  async upsert(collection: string, points: any[]): Promise<void> {
    const response = await fetch(`${this.url}/collections/${collection}/points`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(this.apiKey ? { 'api-key': this.apiKey } : {}),
      },
      body: JSON.stringify({ points }),
    });
    if (!response.ok) throw new Error(`Qdrant upsert failed: ${await response.text()}`);
  }

  async search(collection: string, vector: number[], limit: number = 10, filter?: any): Promise<SearchResult[]> {
    const response = await fetch(`${this.url}/collections/${collection}/points/search`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(this.apiKey ? { 'api-key': this.apiKey } : {}),
      },
      body: JSON.stringify({
        vector,
        limit,
        filter,
        with_payload: true,
      }),
    });
    const data = await response.json();
    return (data.result || []).map((p: any) => ({
      id: p.id.toString(),
      score: p.score,
      content: p.payload?.content || p.payload?.text,
      metadata: p.payload,
    }));
  }

  async delete(collection: string, id: string): Promise<void> {
    await fetch(`${this.url}/collections/${collection}/points/delete`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(this.apiKey ? { 'api-key': this.apiKey } : {}),
      },
      body: JSON.stringify({ points: [id] }),
    });
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch(`${this.url}/collections`, {
      headers: this.apiKey ? { 'api-key': this.apiKey } : {},
    });
    const data = await response.json();
    return (data.result?.collections || []).map((c: any) => c.name);
  }
}

class MilvusClient {
  private host: string;
  private port: number;
  private ssl: boolean;

  constructor(config: VectorConfig) {
    this.host = config.host;
    this.port = config.port;
    this.ssl = config.ssl;
  }

  async configure(config: VectorConfig): Promise<void> {
    this.host = config.host;
    this.port = config.port;
    this.ssl = config.ssl;
  }

  private getAddress(): string {
    return this.ssl ? `https://${this.host}:${this.port}` : `${this.host}:${this.port}`;
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    const response = await fetch(`${this.getAddress()}/api/v1/collection`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        collection_name: name,
        dimension,
        metric_type: 'COSINE',
      }),
    });
    if (!response.ok) throw new Error(`Milvus create collection failed`);
  }

  async upsert(collection: string, vectors: any[]): Promise<void> {
    await fetch(`${this.getAddress()}/api/v1/collection/${collection}/entities`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ data: vectors }),
    });
  }

  async search(collection: string, vector: number[], limit: number = 10): Promise<SearchResult[]> {
    const response = await fetch(`${this.getAddress()}/api/v1/collection/${collection}/query`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        vector,
        limit,
        output_fields: ['*'],
      }),
    });
    const data = await response.json();
    return (data.data || []).map((item: any) => ({
      id: item.id?.toString() || '',
      score: item.score || 0,
      content: item.content || item.text,
      metadata: item,
    }));
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch(`${this.getAddress()}/api/v1/collection`, {
      headers: { 'Content-Type': 'application/json' },
    });
    const data = await response.json();
    return data.collection_names || [];
  }
}

class ChromaClient {
  private url: string;

  constructor(config: VectorConfig) {
    this.url = `http://${config.host}:${config.port}`;
  }

  async configure(config: VectorConfig): Promise<void> {
    this.url = config.ssl ? `https://${config.host}:${config.port}` : `http://${config.host}:${config.port}`;
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    await fetch(`${this.url}/v1/collections`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        get_or_create: true,
        metadata: { dimension },
      }),
    });
  }

  async upsert(collection: string, documents: any[]): Promise<void> {
    await fetch(`${this.url}/v1/collections/${collection}/add`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ documents }),
    });
  }

  async search(collection: string, query: string, limit: number = 10, where?: any): Promise<SearchResult[]> {
    const response = await fetch(`${this.url}/v1/collections/${collection}/query`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query_texts: [query],
        n_results: limit,
        where,
      }),
    });
    const data = await response.json();
    const results: SearchResult[] = [];
    if (data.documents?.[0]) {
      for (let i = 0; i < data.documents[0].length; i++) {
        results.push({
          id: data.ids[0][i],
          score: 1 - (data.distances?.[0][i] || 0),
          content: data.documents[0][i],
          metadata: data.metadatas?.[0][i],
        });
      }
    }
    return results;
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch(`${this.url}/v1/collections`);
    const data = await response.json();
    return (data || []).map((c: any) => c.name);
  }

  async deleteCollection(name: string): Promise<void> {
    await fetch(`${this.url}/v1/collections/${name}`, { method: 'DELETE' });
  }
}

class WeaviateClient {
  private url: string;
  private headers: Record<string, string> = {};

  constructor(config: VectorConfig) {
    this.url = `${config.scheme || 'http'}://${config.host}:${config.port}`;
  }

  async configure(config: VectorConfig): Promise<void> {
    this.url = `${config.scheme || 'http'}://${config.host}:${config.port}`;
    if (config.username && config.password) {
      const auth = Buffer.from(`${config.username}:${config.password}`).toString('base64');
      this.headers['Authorization'] = `Basic ${auth}`;
    }
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    await fetch(`${this.url}/v1/schema`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...this.headers },
      body: JSON.stringify({
        class: name,
        vectorizer: 'none',
        properties: [
          { name: 'content', dataType: ['text'] },
          { name: 'metadata', dataType: ['object'] },
        ],
        vectorIndexConfig: { distance: 'cosine' },
      }),
    });
  }

  async upsert(collection: string, objects: any[]): Promise<void> {
    for (const obj of objects) {
      await fetch(`${this.url}/v1/objects`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...this.headers },
        body: JSON.stringify({
          class: collection,
          vector: obj.vector,
          properties: {
            content: obj.content,
            metadata: obj.metadata,
          },
        }),
      });
    }
  }

  async search(collection: string, vector: number[], limit: number = 10): Promise<SearchResult[]> {
    const response = await fetch(`${this.url}/v1/graphql`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...this.headers },
      body: JSON.stringify({
        query: `{
          Get {
            ${collection}(nearVector: {vector: [${vector.join(',')}], limit: ${limit}}) {
              content
              _score
              metadata
            }
          }
        }`,
      }),
    });
    const data = await response.json();
    return (data.data?.Get?.[collection] || []).map((item: any) => ({
      id: '',
      score: item._score,
      content: item.content,
      metadata: item.metadata,
    }));
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch(`${this.url}/v1/schema`, { headers: this.headers });
    const data = await response.json();
    return (data.classes || []).map((c: any) => c.class);
  }
}

class PineconeClient {
  private apiKey: string;
  private environment: string;
  private indexUrl: string = '';

  constructor(config: VectorConfig) {
    this.apiKey = config.apiKey || '';
    this.environment = config.environment || 'us-east-1';
  }

  async configure(config: VectorConfig): Promise<void> {
    this.apiKey = config.apiKey || '';
    this.environment = config.environment || 'us-east-1';
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    const response = await fetch(`https://api.pinecone.io/indexes`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Api-Key': this.apiKey,
      },
      body: JSON.stringify({
        name,
        dimension,
        metric: 'cosine',
        pod_type: 'starter',
      }),
    });
    if (!response.ok) throw new Error(`Pinecone create index failed: ${await response.text()}`);
    this.indexUrl = `https://${name}-${this.environment}.pinecone.io`;
  }

  async upsert(collection: string, vectors: any[]): Promise<void> {
    if (!this.indexUrl) {
      this.indexUrl = `https://${collection}-${this.environment}.pinecone.io`;
    }
    await fetch(`${this.indexUrl}/vectors/upsert`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Api-Key': this.apiKey,
      },
      body: JSON.stringify({ vectors }),
    });
  }

  async search(collection: string, vector: number[], limit: number = 10): Promise<SearchResult[]> {
    if (!this.indexUrl) {
      this.indexUrl = `https://${collection}-${this.environment}.pinecone.io`;
    }
    const response = await fetch(`${this.indexUrl}/vectors/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Api-Key': this.apiKey,
      },
      body: JSON.stringify({
        vector,
        top_k: limit,
        includeMetadata: true,
      }),
    });
    const data = await response.json();
    return (data.matches || []).map((item: any) => ({
      id: item.id,
      score: item.score,
      content: item.metadata?.content || item.metadata?.text,
      metadata: item.metadata,
    }));
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch('https://api.pinecone.io/indexes', {
      headers: { 'Api-Key': this.apiKey },
    });
    const data = await response.json();
    return (data.indexes || []).map((idx: any) => idx.name);
  }
}

class PGvectorClient {
  private config: VectorConfig;

  constructor(config: VectorConfig) {
    this.config = config;
  }

  async configure(config: VectorConfig): Promise<void> {
    this.config = config;
  }

  private getPool() {
    const pg = require('pg');
    return new pg.Pool({
      host: this.config.host,
      port: this.config.port,
      database: this.config.database || 'postgres',
      user: this.config.username,
      password: this.config.password,
    });
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    const pool = this.getPool();
    try {
      await pool.query(`CREATE EXTENSION IF NOT EXISTS vector`);
      await pool.query(`CREATE TABLE IF NOT EXISTS ${name} (id VARCHAR, embedding vector(${dimension}), content TEXT, metadata JSONB)`);
      await pool.query(`CREATE INDEX IF NOT EXISTS ${name}_idx ON ${name} USING ivfflat (embedding vector_cosine_ops)`);
    } finally {
      await pool.end();
    }
  }

  async upsert(collection: string, vectors: any[]): Promise<void> {
    const pool = this.getPool();
    try {
      for (const vec of vectors) {
        await pool.query(
          `INSERT INTO ${collection} (id, embedding, content, metadata) VALUES ($1, $2, $3, $4) ON CONFLICT (id) DO UPDATE SET embedding = $2, content = $3, metadata = $4`,
          [vec.id, vec.vector, vec.content, JSON.stringify(vec.metadata || {})]
        );
      }
    } finally {
      await pool.end();
    }
  }

  async search(collection: string, vector: number[], limit: number = 10): Promise<SearchResult[]> {
    const pool = this.getPool();
    try {
      const result = await pool.query(
        `SELECT id, (embedding <=> $1) as distance, content, metadata FROM ${collection} ORDER BY embedding <=> $1 LIMIT $2`,
        [vector, limit]
      );
      return result.rows.map(row => ({
        id: row.id,
        score: 1 - parseFloat(row.distance),
        content: row.content,
        metadata: row.metadata,
      }));
    } finally {
      await pool.end();
    }
  }

  async listCollections(): Promise<string[]> {
    const pool = this.getPool();
    try {
      const result = await pool.query(
        `SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '%embedding%'`
      );
      return result.rows.map(row => row.tablename);
    } finally {
      await pool.end();
    }
  }
}

class OpenSearchClient {
  private url: string;
  private auth?: { username: string; password: string };

  constructor(config: VectorConfig) {
    this.url = `http://${config.host}:${config.port}`;
    if (config.username && config.password) {
      this.auth = { username: config.username, password: config.password };
    }
  }

  async configure(config: VectorConfig): Promise<void> {
    this.url = config.ssl ? `https://${config.host}:${config.port}` : `http://${config.host}:${config.port}`;
    if (config.username && config.password) {
      this.auth = { username: config.username, password: config.password };
    }
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    const response = await fetch(`${this.url}/${name}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        settings: { index: { knn: true } },
        mappings: {
          properties: {
            vector_field: { type: 'knn_vector', dimension },
            content: { type: 'text' },
            metadata: { type: 'object', enabled: true },
          },
        },
      }),
    });
    if (!response.ok && !response.status === 400) {
      throw new Error(`OpenSearch create index failed: ${await response.text()}`);
    }
  }

  async upsert(collection: string, vectors: any[]): Promise<void> {
    for (const vec of vectors) {
      await fetch(`${this.url}/${collection}/_doc/${vec.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          vector_field: vec.vector,
          content: vec.content,
          metadata: vec.metadata,
        }),
      });
    }
  }

  async search(collection: string, vector: number[], limit: number = 10): Promise<SearchResult[]> {
    const response = await fetch(`${this.url}/${collection}/_search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        size: limit,
        query: {
          knn: {
            vector_field: { vector, k: limit },
          },
        },
      }),
    });
    const data = await response.json();
    return (data.hits?.hits || []).map((hit: any) => ({
      id: hit._id,
      score: hit._score,
      content: hit._source.content,
      metadata: hit._source.metadata,
    }));
  }

  async listCollections(): Promise<string[]> {
    const response = await fetch(`${this.url}/_cat/indices?format=json`, {
      headers: this.auth ? { Authorization: `Basic ${Buffer.from(`${this.auth.username}:${this.auth.password}`).toString('base64')}` } : {},
    });
    const data = await response.json();
    return data.map((idx: any) => idx.index).filter(Boolean);
  }
}

export class VectorConnectionManager {
  private clients: Map<VectorDBType, any> = new Map();
  private config: VectorConfig | null = null;

  async configure(config: VectorConfig): Promise<void> {
    this.config = config;
    let client: any;

    switch (config.type) {
      case 'qdrant':
        client = new QdrantClient(config);
        break;
      case 'milvus':
        client = new MilvusClient(config);
        break;
      case 'chroma':
        client = new ChromaClient(config);
        break;
      case 'weaviate':
        client = new WeaviateClient(config);
        break;
      case 'pinecone':
        client = new PineconeClient(config);
        break;
      case 'pgvector':
        client = new PGvectorClient(config);
        break;
      case 'opensearch':
        client = new OpenSearchClient(config);
        break;
      default:
        throw new Error(`Unsupported vector database type: ${config.type}`);
    }

    await client.configure(config);
    this.clients.set(config.type, client);
  }

  private getClient(): any {
    if (!this.config) throw new Error('Vector database not configured');
    const client = this.clients.get(this.config.type);
    if (!client) throw new Error(`Client for ${this.config.type} not initialized`);
    return client;
  }

  async createCollection(name: string, dimension: number): Promise<void> {
    await this.getClient().createCollection(name, dimension);
  }

  async upsert(collection: string, documents: VectorDocument[]): Promise<void> {
    const points = documents.map((doc, i) => ({
      id: doc.id || `${Date.now()}-${i}`,
      vector: doc.vector || [],
      payload: { content: doc.content, metadata: doc.metadata },
    }));
    await this.getClient().upsert(collection, points);
  }

  async search(collection: string, vector: number[], limit: number = 10, filter?: any): Promise<SearchResult[]> {
    return this.getClient().search(collection, vector, limit, filter);
  }

  async textSearch(collection: string, text: string, limit: number = 10): Promise<SearchResult[]> {
    return this.getClient().textSearch(collection, text, limit);
  }

  async delete(collection: string, id: string): Promise<void> {
    await this.getClient().delete(collection, id);
  }

  async listCollections(): Promise<string[]> {
    return this.getClient().listCollections();
  }

  async healthCheck(): Promise<{ status: string; type: string; collections: number }> {
    const client = this.getClient();
    const collections = await client.listCollections();
    return {
      status: 'UP',
      type: this.config?.type || 'unknown',
      collections: collections.length,
    };
  }
}

export const vectorConnectionManager = new VectorConnectionManager();