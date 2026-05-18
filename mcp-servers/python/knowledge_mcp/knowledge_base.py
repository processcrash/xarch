"""
Knowledge Base implementation with RAG (Retrieval Augmented Generation)
In-memory vector store with TF-IDF based semantic search
"""

import json
import re
import uuid
from typing import Any, Dict, List, Optional, Tuple
from dataclasses import dataclass, field
from collections import defaultdict
import math


@dataclass
class Document:
    """Represents a document in the knowledge base"""
    id: str
    title: str
    content: str
    doc_type: Optional[str] = None
    metadata: Dict[str, Any] = field(default_factory=dict)
    embedding: Optional[List[float]] = None
    created_at: float = field(default_factory=time.time)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "title": self.title,
            "content": self.content,
            "type": self.doc_type,
            "metadata": self.metadata,
            "createdAt": self.created_at,
        }


import time


class KnowledgeBase:
    """In-memory knowledge base with TF-IDF based semantic search"""

    def __init__(self):
        self.documents: Dict[str, Document] = {}
        self.index: Dict[str, List[str]] = defaultdict(list)  # term -> document IDs
        self.doc_count = 0

    def _tokenize(self, text: str) -> List[str]:
        """Tokenize text into words"""
        # Convert to lowercase and extract words
        text = text.lower()
        # Remove special characters and split
        words = re.findall(r'\w+', text)
        # Filter out short words and stopwords
        stopwords = {'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
                     'of', 'with', 'by', 'from', 'as', 'is', 'was', 'are', 'were', 'be',
                     'been', 'being', 'have', 'has', 'had', 'do', 'does', 'did', 'will',
                     'would', 'should', 'could', 'may', 'might', 'must', 'can', 'this',
                     'that', 'these', 'those', 'i', 'you', 'he', 'she', 'it', 'we', 'they'}
        return [w for w in words if len(w) > 2 and w not in stopwords]

    def _compute_tf(self, tokens: List[str]) -> Dict[str, float]:
        """Compute term frequency"""
        tf = defaultdict(int)
        for token in tokens:
            tf[token] += 1
        total = len(tokens) or 1
        for term in tf:
            tf[term] = tf[term] / total
        return tf

    def _compute_idf(self, term: str) -> float:
        """Compute inverse document frequency"""
        doc_with_term = sum(1 for doc in self.documents.values() if term in self._tokenize(doc.content))
        if doc_with_term == 0:
            return 0.0
        return math.log(len(self.documents) / doc_with_term + 1)

    def _compute_vector(self, tokens: List[str]) -> Dict[str, float]:
        """Compute TF-IDF vector for a document"""
        tf = self._compute_tf(tokens)
        vector = {}
        for term, tf_score in tf.items():
            idf = self._compute_idf(term)
            vector[term] = tf_score * idf
        return vector

    def _cosine_similarity(self, vec1: Dict[str, float], vec2: Dict[str, float]) -> float:
        """Compute cosine similarity between two vectors"""
        if not vec1 or not vec2:
            return 0.0

        # Get all terms
        terms = set(vec1.keys()) | set(vec2.keys())

        dot_product = sum(vec1.get(t, 0) * vec2.get(t, 0) for t in terms)
        mag1 = math.sqrt(sum(v * v for v in vec1.values()))
        mag2 = math.sqrt(sum(v * v for v in vec2.values()))

        if mag1 == 0 or mag2 == 0:
            return 0.0

        return dot_product / (mag1 * mag2)

    def index_document(
        self,
        title: str,
        content: str,
        id: Optional[str] = None,
        doc_type: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """Index a document into the knowledge base"""
        doc_id = id or str(uuid.uuid4())

        # Tokenize content
        tokens = self._tokenize(content + " " + title)
        vector = self._compute_vector(tokens)

        # Create document
        document = Document(
            id=doc_id,
            title=title,
            content=content,
            doc_type=doc_type,
            metadata=metadata or {},
            embedding=list(vector.values()),
        )

        self.documents[doc_id] = document

        # Update index
        for term in tokens:
            if doc_id not in self.index[term]:
                self.index[term].append(doc_id)

        self.doc_count = len(self.documents)

        return {
            "documentId": doc_id,
            "indexed": True,
            "tokenCount": len(tokens),
        }

    def index_file(
        self,
        title: str,
        content: str,
        file_type: str,
        id: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """Index a file into the knowledge base"""
        metadata = metadata or {}
        metadata["fileType"] = file_type

        return self.index_document(
            id=id,
            title=title,
            content=content,
            doc_type=file_type,
            metadata=metadata,
        )

    def search(
        self,
        query: str,
        top_k: int = 5,
        min_score: float = 0.0,
    ) -> List[Dict[str, Any]]:
        """Search the knowledge base using semantic similarity"""
        query_tokens = self._tokenize(query)
        query_vector = self._compute_vector(query_tokens)

        # Search in inverted index
        candidate_ids = set()
        for token in query_tokens:
            if token in self.index:
                candidate_ids.update(self.index[token])

        # Compute similarity for each candidate
        results = []
        for doc_id in candidate_ids:
            doc = self.documents.get(doc_id)
            if not doc:
                continue

            # Compute similarity
            doc_tokens = self._tokenize(doc.content + " " + doc.title)
            doc_vector = self._compute_vector(doc_tokens)
            score = self._cosine_similarity(query_vector, doc_vector)

            if score >= min_score:
                results.append({
                    "documentId": doc.id,
                    "title": doc.title,
                    "content": doc.content[:200] + "..." if len(doc.content) > 200 else doc.content,
                    "score": round(score, 4),
                    "type": doc.doc_type,
                    "metadata": doc.metadata,
                })

        # Sort by score descending
        results.sort(key=lambda x: x["score"], reverse=True)

        return results[:top_k]

    def get_document(self, document_id: str) -> Optional[Dict[str, Any]]:
        """Get a document by ID"""
        doc = self.documents.get(document_id)
        if doc:
            return doc.to_dict()
        return None

    def delete_document(self, document_id: str) -> Dict[str, bool]:
        """Delete a document from the knowledge base"""
        if document_id not in self.documents:
            return {"success": False, "error": "Document not found"}

        doc = self.documents[document_id]

        # Remove from index
        tokens = self._tokenize(doc.content + " " + doc.title)
        for token in tokens:
            if document_id in self.index[token]:
                self.index[token].remove(document_id)

        # Remove document
        del self.documents[document_id]
        self.doc_count = len(self.documents)

        return {"success": True}

    def list_documents(self) -> Dict[str, Any]:
        """List all documents in the knowledge base"""
        docs = [doc.to_dict() for doc in self.documents.values()]
        return {
            "documents": docs,
            "total": len(docs),
        }

    def update_document(
        self,
        document_id: str,
        title: Optional[str] = None,
        content: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, bool]:
        """Update an existing document"""
        if document_id not in self.documents:
            return {"success": False, "error": "Document not found"}

        doc = self.documents[document_id]

        # Delete old index entries
        old_tokens = self._tokenize(doc.content + " " + doc.title)
        for token in old_tokens:
            if document_id in self.index[token]:
                self.index[token].remove(document_id)

        # Update fields
        if title is not None:
            doc.title = title
        if content is not None:
            doc.content = content
        if metadata is not None:
            doc.metadata.update(metadata)

        # Re-index
        tokens = self._tokenize(doc.content + " " + doc.title)
        for token in tokens:
            if document_id not in self.index[token]:
                self.index[token].append(document_id)

        return {"success": True}

    def get_stats(self) -> Dict[str, Any]:
        """Get knowledge base statistics"""
        type_counts = defaultdict(int)
        for doc in self.documents.values():
            doc_type = doc.doc_type or "unknown"
            type_counts[doc_type] += 1

        return {
            "totalDocuments": self.doc_count,
            "byType": dict(type_counts),
            "indexedTerms": len(self.index),
        }