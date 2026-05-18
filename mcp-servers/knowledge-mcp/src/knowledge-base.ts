/**
 * Knowledge Base with RAG Support
 * In-memory vector store with HNSWlib for similarity search
 */

import { v4 as uuidv4 } from 'uuid';

// Simple embedding function (in production, use OpenAI/Cohere/etc)
function createEmbedding(text: string): number[] {
  // Simple TF-IDF like embedding for demonstration
  // In production, use: OpenAI Embeddings, Cohere, or local models
  const words = text.toLowerCase().split(/\s+/);
  const embedding = new Array(1536).fill(0);
  words.forEach((word, i) => {
    const hash = hashString(word);
    embedding[hash % 1536] += 1 / (i + 1);
  });
  // Normalize
  const norm = Math.sqrt(embedding.reduce((sum, val) => sum + val * val, 0));
  return embedding.map(val => val / (norm || 1));
}

function hashString(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return Math.abs(hash);
}

function cosineSimilarity(a: number[], b: number[]): number {
  if (a.length !== b.length) return 0;
  let dotProduct = 0;
  let normA = 0;
  let normB = 0;
  for (let i = 0; i < a.length; i++) {
    dotProduct += a[i] * b[i];
    normA += a[i] * a[i];
    normB += b[i] * b[i];
  }
  return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
}

export interface Document {
  id: string;
  title: string;
  content: string;
  type: string;
  metadata: Record<string, any>;
  embedding: number[];
  createdAt: Date;
  updatedAt: Date;
}

export interface SearchResult {
  id: string;
  title: string;
  content: string;
  type: string;
  score: number;
  metadata: Record<string, any>;
}

export class KnowledgeBase {
  private documents: Map<string, Document> = new Map();

  /**
   * Index a document
   */
  async indexDocument(params: {
    id?: string;
    title: string;
    content: string;
    type?: string;
    metadata?: Record<string, any>;
  }): Promise<{ id: string; success: boolean }> {
    const id = params.id || uuidv4();
    const embedding = createEmbedding(params.content);

    const doc: Document = {
      id,
      title: params.title,
      content: params.content,
      type: params.type || 'document',
      metadata: params.metadata || {},
      embedding,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    this.documents.set(id, doc);
    return { id, success: true };
  }

  /**
   * Index a file (for PDF, Markdown, etc.)
   */
  async indexFile(params: {
    id?: string;
    title: string;
    content: string;
    fileType: string;
    metadata?: Record<string, any>;
  }): Promise<{ id: string; success: boolean }> {
    return this.indexDocument({
      id: params.id,
      title: params.title,
      content: params.content,
      type: `file.${params.fileType}`,
      metadata: {
        ...params.metadata,
        fileType: params.fileType,
      },
    });
  }

  /**
   * Semantic search
   */
  async search(params: {
    query: string;
    topK?: number;
    minScore?: number;
  }): Promise<SearchResult[]> {
    const topK = params.topK || 5;
    const minScore = params.minScore || 0.0;
    const queryEmbedding = createEmbedding(params.query);

    const results: SearchResult[] = [];

    for (const doc of this.documents.values()) {
      const score = cosineSimilarity(queryEmbedding, doc.embedding);
      if (score >= minScore) {
        results.push({
          id: doc.id,
          title: doc.title,
          content: doc.content,
          type: doc.type,
          score,
          metadata: doc.metadata,
        });
      }
    }

    // Sort by score descending
    results.sort((a, b) => b.score - a.score);

    return results.slice(0, topK);
  }

  /**
   * Get document by ID
   */
  async getDocument(id: string): Promise<Document | null> {
    return this.documents.get(id) || null;
  }

  /**
   * Delete document
   */
  async deleteDocument(id: string): Promise<{ success: boolean }> {
    return { success: this.documents.delete(id) };
  }

  /**
   * List all documents
   */
  async listDocuments(): Promise<{ documents: any[]; total: number }> {
    const docs = Array.from(this.documents.values()).map(doc => ({
      id: doc.id,
      title: doc.title,
      type: doc.type,
      metadata: doc.metadata,
      createdAt: doc.createdAt,
      updatedAt: doc.updatedAt,
    }));
    return { documents: docs, total: docs.length };
  }

  /**
   * Update document
   */
  async updateDocument(id: string, updates: Partial<{
    title: string;
    content: string;
    metadata: Record<string, any>;
  }>): Promise<{ success: boolean }> {
    const doc = this.documents.get(id);
    if (!doc) {
      return { success: false };
    }

    if (updates.content) {
      doc.embedding = createEmbedding(updates.content);
    }
    if (updates.title) doc.title = updates.title;
    if (updates.content) doc.content = updates.content;
    if (updates.metadata) doc.metadata = { ...doc.metadata, ...updates.metadata };
    doc.updatedAt = new Date();

    return { success: true };
  }

  /**
   * Clear all documents
   */
  async clear(): Promise<void> {
    this.documents.clear();
  }

  /**
   * Get statistics
   */
  getStats(): { totalDocuments: number; byType: Record<string, number> } {
    const byType: Record<string, number> = {};
    for (const doc of this.documents.values()) {
      byType[doc.type] = (byType[doc.type] || 0) + 1;
    }
    return {
      totalDocuments: this.documents.size,
      byType,
    };
  }
}

export const knowledgeBase = new KnowledgeBase();