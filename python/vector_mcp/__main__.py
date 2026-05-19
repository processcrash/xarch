#!/usr/bin/env python3
"""
Vector Database MCP Server - Python Implementation
Supports: Qdrant, Milvus, Chroma, Weaviate, Pinecone, PGvector, OpenSearch, FAISS, Elasticsearch
"""

import json
import sys
import os
from abc import ABC, abstractmethod
from typing import Any, Optional
from dataclasses import dataclass, field

try:
    import requests
except ImportError:
    requests = None

try:
    import psycopg2
    from psycopg2 import sql
except ImportError:
    psycopg2 = None

try:
    import numpy as np
except ImportError:
    np = None


@dataclass
class VectorConfig:
    type: str
    host: str
    port: int
    ssl: bool = False
    username: Optional[str] = None
    password: Optional[str] = None
    api_key: Optional[str] = None
    environment: Optional[str] = None
    scheme: str = "http"
    database: Optional[str] = None


@dataclass
class SearchResult:
    id: str
    score: float
    content: Optional[str] = None
    metadata: Optional[dict] = None

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "score": self.score,
            "content": self.content,
            "metadata": self.metadata,
        }


class BaseVectorClient(ABC):
    @abstractmethod
    async def configure(self, config: VectorConfig) -> None:
        pass

    @abstractmethod
    async def create_collection(self, name: str, dimension: int) -> None:
        pass

    @abstractmethod
    async def upsert(self, collection: str, documents: list) -> None:
        pass

    @abstractmethod
    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        pass

    @abstractmethod
    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        pass

    @abstractmethod
    async def delete(self, collection: str, id: str) -> None:
        pass

    @abstractmethod
    async def list_collections(self) -> list:
        pass


class QdrantClient(BaseVectorClient):
    def __init__(self):
        self.url = ""
        self.api_key = None

    async def configure(self, config: VectorConfig) -> None:
        protocol = "https" if config.ssl else "http"
        self.url = f"{protocol}://{config.host}:{config.port}"
        self.api_key = config.api_key

    def _headers(self) -> dict:
        headers = {"Content-Type": "application/json"}
        if self.api_key:
            headers["api-key"] = self.api_key
        return headers

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Qdrant")
        response = requests.put(
            f"{self.url}/collections/{name}",
            json={"vectors": {"size": dimension, "distance": "Cosine"}},
            headers=self._headers(),
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Qdrant create collection failed: {response.text}")

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Qdrant")
        points = []
        for i, doc in enumerate(documents):
            points.append({
                "id": doc.get("id", f"{i}"),
                "vector": doc.get("vector", []),
                "payload": {
                    "content": doc.get("content", ""),
                    "metadata": doc.get("metadata", {})
                }
            })
        response = requests.put(
            f"{self.url}/collections/{collection}/points",
            json={"points": points},
            headers=self._headers(),
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Qdrant upsert failed: {response.text}")

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Qdrant")
        payload = {"vector": vector, "limit": limit, "with_payload": True}
        if filter:
            payload["filter"] = filter
        response = requests.post(
            f"{self.url}/collections/{collection}/points/search",
            json=payload,
            headers=self._headers(),
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Qdrant search failed: {response.text}")
        data = response.json()
        results = []
        for p in data.get("result", []):
            payload = p.get("payload", {})
            results.append(SearchResult(
                id=str(p["id"]),
                score=p["score"],
                content=payload.get("content"),
                metadata=payload.get("metadata")
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        return []

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for Qdrant")
        requests.post(
            f"{self.url}/collections/{collection}/points/delete",
            json={"points": [id]},
            headers=self._headers(),
            timeout=10
        )

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Qdrant")
        response = requests.get(f"{self.url}/collections", headers=self._headers(), timeout=10)
        data = response.json()
        return [c["name"] for c in data.get("result", {}).get("collections", [])]


class MilvusClient(BaseVectorClient):
    def __init__(self):
        self.host = ""
        self.port = 0
        self.ssl = False

    async def configure(self, config: VectorConfig) -> None:
        self.host = config.host
        self.port = config.port
        self.ssl = config.ssl

    def _get_address(self) -> str:
        protocol = "https" if self.ssl else "http"
        return f"{protocol}://{self.host}:{self.port}"

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Milvus")
        response = requests.post(
            f"{self._get_address()}/api/v1/collection",
            json={"collection_name": name, "dimension": dimension, "metric_type": "COSINE"},
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Milvus create collection failed: {response.text}")

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Milvus")
        vectors = []
        for i, doc in enumerate(documents):
            vectors.append({
                "id": doc.get("id", str(i)),
                "vector": doc.get("vector", []),
                "content": doc.get("content", "")
            })
        requests.post(
            f"{self._get_address()}/api/v1/collection/{collection}/entities",
            json={"data": vectors},
            headers={"Content-Type": "application/json"},
            timeout=10
        )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Milvus")
        response = requests.post(
            f"{self._get_address()}/api/v1/collection/{collection}/query",
            json={"vector": vector, "limit": limit, "output_fields": ["*"]},
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Milvus search failed: {response.text}")
        data = response.json()
        results = []
        for item in data.get("data", []):
            results.append(SearchResult(
                id=str(item.get("id", "")),
                score=item.get("score", 0),
                content=item.get("content") or item.get("text"),
                metadata=item
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        return []

    async def delete(self, collection: str, id: str) -> None:
        pass

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Milvus")
        response = requests.get(
            f"{self._get_address()}/api/v1/collection",
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        data = response.json()
        return data.get("collection_names", [])


class ChromaClient(BaseVectorClient):
    def __init__(self):
        self.url = ""

    async def configure(self, config: VectorConfig) -> None:
        protocol = "https" if config.ssl else "http"
        self.url = f"{protocol}://{config.host}:{config.port}"

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        requests.post(
            f"{self.url}/v1/collections",
            json={"name": name, "get_or_create": True, "metadata": {"dimension": dimension}},
            headers={"Content-Type": "application/json"},
            timeout=10
        )

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        docs = []
        for doc in documents:
            docs.append({
                "id": doc.get("id", ""),
                "embedding": doc.get("vector", []),
                "document": doc.get("content", ""),
                "metadata": doc.get("metadata", {})
            })
        requests.post(
            f"{self.url}/v1/collections/{collection}/add",
            json={"ids": [d["id"] for d in docs], "embeddings": [d["embedding"] for d in docs],
                  "documents": [d["document"] for d in docs], "metadatas": [d["metadata"] for d in docs]},
            headers={"Content-Type": "application/json"},
            timeout=10
        )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        payload = {"query_embeddings": [vector], "n_results": limit, "include": ["documents", "metadatas", "distances"]}
        if filter:
            payload["where"] = filter
        response = requests.post(
            f"{self.url}/v1/collections/{collection}/query",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Chroma search failed: {response.text}")
        data = response.json()
        results = []
        for i in range(len(data.get("ids", [[]])[0])):
            results.append(SearchResult(
                id=data["ids"][0][i],
                score=1 - (data["distances"][0][i] if data.get("distances") else 0),
                content=data["documents"][0][i] if data.get("documents") else None,
                metadata=data["metadatas"][0][i] if data.get("metadatas") else None
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        response = requests.post(
            f"{self.url}/v1/collections/{collection}/query",
            json={"query_texts": [query], "n_results": limit},
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        data = response.json()
        results = []
        for i in range(len(data.get("documents", [[]])[0])):
            results.append(SearchResult(
                id=data["ids"][0][i],
                score=1 - (data["distances"][0][i] if data.get("distances") else 0),
                content=data["documents"][0][i],
                metadata=data["metadatas"][0][i] if data.get("metadatas") else None
            ))
        return results

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        requests.post(
            f"{self.url}/v1/collections/{collection}/delete",
            json={"ids": [id]},
            headers={"Content-Type": "application/json"},
            timeout=10
        )

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Chroma")
        response = requests.get(f"{self.url}/v1/collections", headers={"Content-Type": "application/json"}, timeout=10)
        data = response.json()
        return [c["name"] for c in data]


class WeaviateClient(BaseVectorClient):
    def __init__(self):
        self.url = ""
        self.headers = {}

    async def configure(self, config: VectorConfig) -> None:
        self.url = f"{config.scheme or 'http'}://{config.host}:{config.port}"
        if config.username and config.password:
            import base64
            auth = base64.b64encode(f"{config.username}:{config.password}".encode()).decode()
            self.headers["Authorization"] = f"Basic {auth}"

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        schema = {
            "class": name,
            "vectorizer": "none",
            "properties": [
                {"name": "content", "dataType": ["text"]},
                {"name": "metadata", "dataType": ["object"]}
            ],
            "vectorIndexConfig": {"distance": "cosine"}
        }
        requests.post(
            f"{self.url}/v1/schema",
            json=schema,
            headers={"Content-Type": "application/json", **self.headers},
            timeout=10
        )

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        for doc in documents:
            obj = {
                "class": collection,
                "vector": doc.get("vector", []),
                "properties": {
                    "content": doc.get("content", ""),
                    "metadata": doc.get("metadata", {})
                }
            }
            requests.post(
                f"{self.url}/v1/objects",
                json=obj,
                headers={"Content-Type": "application/json", **self.headers},
                timeout=10
            )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        query = f"""
        {{
          Get {{
            {collection}(nearVector: {{vector: {vector}, limit: {limit}) {{
              content
              _score
              metadata
            }}
          }}
        }}
        """
        response = requests.post(
            f"{self.url}/v1/graphql",
            json={"query": query},
            headers={"Content-Type": "application/json", **self.headers},
            timeout=10
        )
        data = response.json()
        results = []
        items = data.get("data", {}).get("Get", {}).get(collection, [])
        for item in items:
            results.append(SearchResult(
                id="",
                score=item.get("_score", 0),
                content=item.get("content"),
                metadata=item.get("metadata")
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        gql = f"""
        {{
          Get {{
            {collection}(nearText: {{concepts: ["{query}"], limit: {limit}) {{
              content
              _score
            }}
          }}
        }}
        """
        response = requests.post(
            f"{self.url}/v1/graphql",
            json={"query": gql},
            headers={"Content-Type": "application/json", **self.headers},
            timeout=10
        )
        data = response.json()
        results = []
        items = data.get("data", {}).get("Get", {}).get(collection, [])
        for item in items:
            results.append(SearchResult(
                id="",
                score=item.get("_score", 0),
                content=item.get("content"),
                metadata=None
            ))
        return results

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        requests.delete(f"{self.url}/v1/objects/{collection}/{id}", headers=self.headers, timeout=10)

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Weaviate")
        response = requests.get(f"{self.url}/v1/schema", headers=self.headers, timeout=10)
        data = response.json()
        return [c["class"] for c in data.get("classes", [])]


class PineconeClient(BaseVectorClient):
    def __init__(self):
        self.api_key = ""
        self.environment = ""
        self.index_url = ""

    async def configure(self, config: VectorConfig) -> None:
        self.api_key = config.api_key or ""
        self.environment = config.environment or "us-east-1"

    async def _get_index_url(self, index: str) -> str:
        return f"https://{index}-{self.environment}.pinecone.io"

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Pinecone")
        response = requests.post(
            "https://api.pinecone.io/indexes",
            json={"name": name, "dimension": dimension, "metric": "cosine", "pod_type": "starter"},
            headers={"Content-Type": "application/json", "Api-Key": self.api_key},
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Pinecone create index failed: {response.text}")

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Pinecone")
        vectors = []
        for doc in documents:
            vectors.append({
                "id": doc.get("id", ""),
                "values": doc.get("vector", []),
                "metadata": {"content": doc.get("content", ""), **(doc.get("metadata", {}))}
            })
        url = await self._get_index_url(collection)
        requests.post(
            f"{url}/vectors/upsert",
            json={"vectors": vectors},
            headers={"Content-Type": "application/json", "Api-Key": self.api_key},
            timeout=10
        )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Pinecone")
        url = await self._get_index_url(collection)
        query = {"vector": vector, "top_k": limit, "includeMetadata": True}
        if filter:
            query["filter"] = filter
        response = requests.post(
            f"{url}/vectors/query",
            json=query,
            headers={"Content-Type": "application/json", "Api-Key": self.api_key},
            timeout=10
        )
        if not response.ok:
            raise RuntimeError(f"Pinecone search failed: {response.text}")
        data = response.json()
        results = []
        for item in data.get("matches", []):
            results.append(SearchResult(
                id=item["id"],
                score=item["score"],
                content=item.get("metadata", {}).get("content"),
                metadata=item.get("metadata")
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        return []

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for Pinecone")
        url = await self._get_index_url(collection)
        requests.post(
            f"{url}/vectors/delete",
            json={"ids": [id]},
            headers={"Content-Type": "application/json", "Api-Key": self.api_key},
            timeout=10
        )

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Pinecone")
        response = requests.get("https://api.pinecone.io/indexes", headers={"Api-Key": self.api_key}, timeout=10)
        data = response.json()
        return [idx["name"] for idx in data.get("indexes", [])]


class PGvectorClient(BaseVectorClient):
    def __init__(self):
        self.config = None

    async def configure(self, config: VectorConfig) -> None:
        self.config = config

    def _get_connection(self):
        if not psycopg2:
            raise RuntimeError("psycopg2 library required for PGvector")
        return psycopg2.connect(
            host=self.config.host,
            port=self.config.port,
            database=self.config.database or "postgres",
            user=self.config.username,
            password=self.config.password
        )

    async def create_collection(self, name: str, dimension: int) -> None:
        conn = self._get_connection()
        try:
            cur = conn.cursor()
            cur.execute("CREATE EXTENSION IF NOT EXISTS vector")
            cur.execute(f"CREATE TABLE IF NOT EXISTS {name} (id VARCHAR, embedding vector({dimension}), content TEXT, metadata JSONB)")
            cur.execute(f"CREATE INDEX IF NOT EXISTS {name}_idx ON {name} USING ivfflat (embedding vector_cosine_ops)")
            conn.commit()
        finally:
            conn.close()

    async def upsert(self, collection: str, documents: list) -> None:
        conn = self._get_connection()
        try:
            cur = conn.cursor()
            for doc in documents:
                cur.execute(
                    f"INSERT INTO {collection} (id, embedding, content, metadata) VALUES (%s, %s, %s, %s) ON CONFLICT (id) DO UPDATE SET embedding = %s, content = %s, metadata = %s",
                    [doc.get("id", ""), doc.get("vector", []), doc.get("content", ""), json.dumps(doc.get("metadata", {})),
                     doc.get("vector", []), doc.get("content", ""), json.dumps(doc.get("metadata", {}))]
                )
            conn.commit()
        finally:
            conn.close()

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        conn = self._get_connection()
        try:
            cur = conn.cursor()
            cur.execute(f"SELECT id, (embedding <=> %s) as distance, content, metadata FROM {collection} ORDER BY embedding <=> %s LIMIT %s", [vector, vector, limit])
            rows = cur.fetchall()
            results = []
            for row in rows:
                results.append(SearchResult(
                    id=row[0],
                    score=1 - float(row[1]),
                    content=row[2],
                    metadata=row[3]
                ))
            return results
        finally:
            conn.close()

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        return []

    async def delete(self, collection: str, id: str) -> None:
        conn = self._get_connection()
        try:
            cur = conn.cursor()
            cur.execute(f"DELETE FROM {collection} WHERE id = %s", [id])
            conn.commit()
        finally:
            conn.close()

    async def list_collections(self) -> list:
        conn = self._get_connection()
        try:
            cur = conn.cursor()
            cur.execute("SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '%embedding%'")
            return [row[0] for row in cur.fetchall()]
        finally:
            conn.close()


class OpenSearchClient(BaseVectorClient):
    def __init__(self):
        self.url = ""
        self.auth = None

    async def configure(self, config: VectorConfig) -> None:
        protocol = "https" if config.ssl else "http"
        self.url = f"{protocol}://{config.host}:{config.port}"
        if config.username and config.password:
            import base64
            self.auth = base64.b64encode(f"{config.username}:{config.password}".encode()).decode()

    def _headers(self) -> dict:
        headers = {"Content-Type": "application/json"}
        if self.auth:
            headers["Authorization"] = f"Basic {self.auth}"
        return headers

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        response = requests.put(
            f"{self.url}/{name}",
            json={
                "settings": {"index": {"knn": True}},
                "mappings": {
                    "properties": {
                        "vector_field": {"type": "knn_vector", "dimension": dimension},
                        "content": {"type": "text"},
                        "metadata": {"type": "object", "enabled": True}
                    }
                }
            },
            headers=self._headers(),
            timeout=10
        )
        if not response.ok and response.status_code != 400:
            raise RuntimeError(f"OpenSearch create index failed: {response.text}")

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        for doc in documents:
            requests.put(
                f"{self.url}/{collection}/_doc/{doc.get('id', '')}",
                json={
                    "vector_field": doc.get("vector", []),
                    "content": doc.get("content", ""),
                    "metadata": doc.get("metadata", {})
                },
                headers=self._headers(),
                timeout=10
            )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        response = requests.post(
            f"{self.url}/{collection}/_search",
            json={"size": limit, "query": {"knn": {"vector_field": {"vector": vector, "k": limit}}}},
            headers=self._headers(),
            timeout=10
        )
        data = response.json()
        results = []
        for hit in data.get("hits", {}).get("hits", []):
            results.append(SearchResult(
                id=hit["_id"],
                score=hit["_score"],
                content=hit["_source"].get("content"),
                metadata=hit["_source"].get("metadata")
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        response = requests.post(
            f"{self.url}/{collection}/_search",
            json={"size": limit, "query": {"match": {"content": query}}},
            headers=self._headers(),
            timeout=10
        )
        data = response.json()
        results = []
        for hit in data.get("hits", {}).get("hits", []):
            results.append(SearchResult(
                id=hit["_id"],
                score=hit["_score"],
                content=hit["_source"].get("content"),
                metadata=hit["_source"].get("metadata")
            ))
        return results

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        requests.delete(f"{self.url}/{collection}/_doc/{id}", headers=self._headers(), timeout=10)

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for OpenSearch")
        response = requests.get(f"{self.url}/_cat/indices?format=json", headers=self._headers(), timeout=10)
        data = response.json()
        return [idx["index"] for idx in data if idx.get("index")]


class FaissClient(BaseVectorClient):
    def __init__(self):
        self.indexes = {}
        self.documents = {}

    async def configure(self, config: VectorConfig) -> None:
        pass

    async def create_collection(self, name: str, dimension: int) -> None:
        if np is None:
            raise RuntimeError("numpy library required for FAISS")
        import faiss
        self.indexes[name] = faiss.IndexFlatL2(dimension)
        self.documents[name] = {}

    async def upsert(self, collection: str, documents: list) -> None:
        if np is None:
            raise RuntimeError("numpy library required for FAISS")
        if collection not in self.indexes:
            raise RuntimeError(f"Collection {collection} does not exist")
        for doc in documents:
            vector = np.array(doc.get("vector", []), dtype=np.float32)
            self.indexes[collection].add(vector.reshape(1, -1))
            idx = len(self.documents[collection])
            self.documents[collection][str(idx)] = {"content": doc.get("content", ""), "metadata": doc.get("metadata", {})}

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if np is None:
            raise RuntimeError("numpy library required for FAISS")
        if collection not in self.indexes:
            raise RuntimeError(f"Collection {collection} does not exist")
        import faiss
        query = np.array(vector, dtype=np.float32).reshape(1, -1)
        distances, indices = self.indexes[collection].search(query, limit)
        results = []
        for i, idx in enumerate(indices[0]):
            doc_id = str(idx)
            if doc_id in self.documents[collection]:
                results.append(SearchResult(
                    id=doc_id,
                    score=1 - distances[0][i],
                    content=self.documents[collection][doc_id].get("content"),
                    metadata=self.documents[collection][doc_id].get("metadata")
                ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        return []

    async def delete(self, collection: str, id: str) -> None:
        if collection in self.documents:
            self.documents[collection].pop(id, None)

    async def list_collections(self) -> list:
        return list(self.indexes.keys())


class ElasticsearchClient(BaseVectorClient):
    def __init__(self):
        self.url = ""
        self.auth = None

    async def configure(self, config: VectorConfig) -> None:
        protocol = "https" if config.ssl else "http"
        self.url = f"{protocol}://{config.host}:{config.port}"
        if config.username and config.password:
            import base64
            self.auth = base64.b64encode(f"{config.username}:{config.password}".encode()).decode()

    def _headers(self) -> dict:
        headers = {"Content-Type": "application/json"}
        if self.auth:
            headers["Authorization"] = f"Basic {self.auth}"
        return headers

    async def create_collection(self, name: str, dimension: int) -> None:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        mapping = {
            "mappings": {
                "properties": {
                    "vector_field": {"type": "dense_vector", "dimension": dimension},
                    "content": {"type": "text"},
                    "metadata": {"type": "object"}
                }
            }
        }
        requests.put(f"{self.url}/{name}", json=mapping, headers=self._headers(), timeout=10)

    async def upsert(self, collection: str, documents: list) -> None:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        for doc in documents:
            requests.put(
                f"{self.url}/{collection}/_doc/{doc.get('id', '')}",
                json={"vector_field": doc.get("vector", []), "content": doc.get("content", ""), "metadata": doc.get("metadata", {})},
                headers=self._headers(),
                timeout=10
            )

    async def search(self, collection: str, vector: list, limit: int = 10, filter: Optional[dict] = None) -> list:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        response = requests.post(
            f"{self.url}/{collection}/_search",
            json={"size": limit, "query": {"script_score": {"query": {"match_all": {}}, "script": {"source": "cosineSimilarity(params.query_vector, 'vector_field') + 1.0", "params": {"query_vector": vector}}}},
            headers=self._headers(),
            timeout=10
        )
        data = response.json()
        results = []
        for hit in data.get("hits", {}).get("hits", []):
            results.append(SearchResult(
                id=hit["_id"],
                score=hit["_score"] - 1.0,
                content=hit["_source"].get("content"),
                metadata=hit["_source"].get("metadata")
            ))
        return results

    async def text_search(self, collection: str, query: str, limit: int = 10) -> list:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        response = requests.post(
            f"{self.url}/{collection}/_search",
            json={"size": limit, "query": {"match": {"content": query}}},
            headers=self._headers(),
            timeout=10
        )
        data = response.json()
        results = []
        for hit in data.get("hits", {}).get("hits", []):
            results.append(SearchResult(
                id=hit["_id"],
                score=hit["_score"],
                content=hit["_source"].get("content"),
                metadata=hit["_source"].get("metadata")
            ))
        return results

    async def delete(self, collection: str, id: str) -> None:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        requests.delete(f"{self.url}/{collection}/_doc/{id}", headers=self._headers(), timeout=10)

    async def list_collections(self) -> list:
        if not requests:
            raise RuntimeError("requests library required for Elasticsearch")
        response = requests.get(f"{self.url}/_cat/indices?format=json", headers=self._headers(), timeout=10)
        data = response.json()
        return [idx["index"] for idx in data if idx.get("index") and not idx["index"].startswith(".")]


class VectorMcpServer:
    SUPPORTED_TYPES = [
        "qdrant", "milvus", "chroma", "weaviate", "pinecone",
        "pgvector", "opensearch", "elasticsearch", "faiss"
    ]

    def __init__(self):
        self.client = None
        self.config = None

    def _create_client(self, config: VectorConfig):
        clients = {
            "qdrant": QdrantClient,
            "milvus": MilvusClient,
            "chroma": ChromaClient,
            "weaviate": WeaviateClient,
            "pinecone": PineconeClient,
            "pgvector": PGvectorClient,
            "opensearch": OpenSearchClient,
            "elasticsearch": ElasticsearchClient,
            "faiss": FaissClient,
        }
        client_class = clients.get(config.type)
        if not client_class:
            raise ValueError(f"Unsupported vector database type: {config.type}")
        return client_class()

    async def handle_request(self, method: str, params: dict) -> dict:
        try:
            if method == "configure":
                config = VectorConfig(
                    type=params.get("type", ""),
                    host=params.get("host", ""),
                    port=params.get("port", 0),
                    ssl=params.get("ssl", False),
                    username=params.get("username"),
                    password=params.get("password"),
                    api_key=params.get("apiKey"),
                    environment=params.get("environment"),
                    scheme=params.get("scheme", "http"),
                    database=params.get("database")
                )
                self.client = self._create_client(config)
                await self.client.configure(config)
                self.config = config
                return {"success": True, "message": f"Configured {config.type} at {config.host}:{config.port}"}

            if not self.client:
                return {"error": "Not configured. Call configure first."}

            if method == "create_collection":
                await self.client.create_collection(params["name"], params["dimension"])
                return {"success": True, "message": f"Collection '{params['name']}' created"}

            elif method == "upsert":
                await self.client.upsert(params["collection"], params["vectors"])
                return {"success": True, "message": f"Upserted {len(params['vectors'])} vectors"}

            elif method == "search":
                results = await self.client.search(params["collection"], params["vector"], params.get("limit", 10), params.get("filter"))
                return {"success": True, "results": [r.to_dict() for r in results], "count": len(results)}

            elif method == "text_search":
                results = await self.client.text_search(params["collection"], params["query"], params.get("limit", 10))
                return {"success": True, "results": [r.to_dict() for r in results], "count": len(results)}

            elif method == "delete":
                await self.client.delete(params["collection"], params["id"])
                return {"success": True, "message": f"Deleted {params['id']}"}

            elif method == "list_collections":
                collections = await self.client.list_collections()
                return {"success": True, "collections": collections, "count": len(collections)}

            elif method == "get_stats":
                collections = await self.client.list_collections()
                return {"success": True, "collection": params.get("collection"), "exists": params.get("collection") in collections, "total": len(collections)}

            elif method == "health":
                collections = await self.client.list_collections()
                return {"status": "UP", "type": self.config.type if self.config else "unknown", "collections": len(collections)}

            elif method == "tools":
                return {
                    "tools": [
                        {"name": "configure", "description": "Configure vector database connection", "inputSchema": {"type": "object", "properties": {"type": {"enum": self.SUPPORTED_TYPES}, "host": {}, "port": {}, "ssl": {}, "username": {}, "password": {}, "apiKey": {}, "environment": {}, "database": {}}, "required": ["type", "host", "port"]}},
                        {"name": "create_collection", "description": "Create a new vector collection", "inputSchema": {"type": "object", "properties": {"name": {}, "dimension": {}}, "required": ["name", "dimension"]}},
                        {"name": "upsert", "description": "Insert or update vectors", "inputSchema": {"type": "object", "properties": {"collection": {}, "vectors": {"type": "array"}}, "required": ["collection", "vectors"]}},
                        {"name": "search", "description": "Search vectors by similarity", "inputSchema": {"type": "object", "properties": {"collection": {}, "vector": {"type": "array"}, "limit": {}}, "required": ["collection", "vector"]}},
                        {"name": "text_search", "description": "Text search (for databases with embedding)", "inputSchema": {"type": "object", "properties": {"collection": {}, "query": {}, "limit": {}}, "required": ["collection", "query"]}},
                        {"name": "delete", "description": "Delete a vector", "inputSchema": {"type": "object", "properties": {"collection": {}, "id": {}}, "required": ["collection", "id"]}},
                        {"name": "list_collections", "description": "List all collections", "inputSchema": {"type": "object", "properties": {}}},
                        {"name": "get_stats", "description": "Get collection statistics", "inputSchema": {"type": "object", "properties": {"collection": {}}, "required": ["collection"]}},
                        {"name": "health", "description": "Health check", "inputSchema": {"type": "object", "properties": {}}},
                    ]
                }

            else:
                return {"error": f"Unknown method: {method}"}

        except Exception as e:
            return {"error": str(e)}


def main():
    server = VectorMcpServer()
    print("Vector Database MCP Server started (Python)", file=sys.stderr)
    print(f"Supported: {', '.join(VectorMcpServer.SUPPORTED_TYPES)}", file=sys.stderr)

    for line in sys.stdin:
        try:
            request = json.loads(line.strip())
            method = request.get("method", "")
            params = request.get("params", {})
            result = asyncio_run(server.handle_request(method, params))
            print(json.dumps(result))
        except Exception as e:
            print(json.dumps({"error": str(e)}))
            sys.stdout.flush()


def asyncio_run(coro):
    try:
        import asyncio
        loop = asyncio.new_event_loop()
        return loop.run_until_complete(coro)
    except Exception:
        return {"error": "Failed to run async operation"}


if __name__ == "__main__":
    main()