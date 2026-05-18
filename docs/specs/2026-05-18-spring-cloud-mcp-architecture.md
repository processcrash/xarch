# xarch Spring Cloud + MCP Server Architecture Design

## 1. Overview

**Project**: xarch - AI-Enabled Enterprise Backend Framework with Spring Cloud Support

**Goal**: Extend xarch with Spring Cloud microservices architecture, Nacos 3.2 service registry (including MCP services), database MCP server integration, and enterprise knowledge base MCP server.

---

## 2. Architecture Components

### 2.1 Spring Cloud Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (Spring Cloud Gateway)     │
│                         Port: 8080                          │
└─────────────────────────────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Nacos 3.2 │       │  MCP Server │       │  Business   │
│  (Registry  │       │  Registry   │       │  Services   │
│  + Config)  │       │             │       │             │
│   Port:8848 │       │             │       │             │
└─────────────┘       └─────────────┘       └─────────────┘
         │                    │                    │
         │            ┌──────┴──────┐            │
         │            ▼             ▼            ▼
         │     ┌───────────┐  ┌───────────┐  ┌───────────┐
         │     │ Database  │  │ Knowledge │  │ Filesystem│
         │     │   MCP     │  │    MCP    │  │    MCP    │
         │     │  Server   │  │  Server   │  │  Server   │
         │     └───────────┘  └───────────┘  └───────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database Services                         │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │  MySQL  │ │PostgreSQL│ │MongoDB │ │  Redis  │           │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Module Structure

```
backend/
├── xarch-bom/                              # Bill of Materials
├── xarch-core-spring-boot-starter/         # Core (unchanged)
├── xarch-db-spring-boot-starter/           # Database (unchanged)
├── xarch-web-spring-boot-starter/         # Web (unchanged)
├── xarch-cache-spring-boot-starter/       # Cache (unchanged)
├── xarch-cloud/                            # NEW: Spring Cloud Module
│   ├── xarch-cloud-starter-nacos/         # Nacos integration
│   ├── xarch-cloud-starter-gateway/        # API Gateway
│   └── xarch-cloud-starter-mcp/            # MCP Client/Server
├── xarch-mcp/                              # NEW: MCP Servers Module
│   ├── xarch-mcp-database/                 # Database MCP Server
│   ├── xarch-mcp-knowledge/                # Knowledge Base MCP Server
│   └── xarch-mcp-filesystem/               # Filesystem MCP Server
└── xarch-example/                          # Example application
```

---

## 3. Nacos 3.2 MCP Service Design

### 3.1 MCP Service Registration

```yaml
# MCP Service Registration to Nacos
mcp:
  servers:
    - name: database-mcp
      type: database
      host: localhost
      port: 9090
      capabilities:
        - mysql
        - postgresql
        - mongodb
      nacos:
        namespace: mcp-services
        group: MCP
    - name: knowledge-mcp
      type: knowledge-base
      host: localhost
      port: 9091
      capabilities:
        - vector-search
        - document-processing
        - rag
      nacos:
        namespace: mcp-services
        group: MCP
```

### 3.2 MCP Protocol Handler

Each MCP Server implements:
- `/mcp/health` - Health check endpoint
- `/mcp/capabilities` - List capabilities
- `/mcp/tools` - List available tools
- `/mcp/execute` - Execute tool call

---

## 4. Database MCP Server

### 4.1 Supported Databases

| Database | Driver | Connection String |
|----------|--------|-------------------|
| MySQL | mysql-connector-java | jdbc:mysql://host:3306/db |
| PostgreSQL | postgresql | jdbc:postgresql://host:5432/db |
| MongoDB | mongodb-driver | mongodb://host:27017/db |
| Redis | lettuce | redis://host:6379 |
| Microsoft SQL Server | mssql-jdbc | jdbc:sqlserver://host:1433 |

### 4.2 Tools Provided

- `query_execute` - Execute SQL/query
- `schema_get` - Get database schema
- `table_list` - List tables
- `table_describe` - Describe table structure
- `index_list` - List indexes

---

## 5. Knowledge Base MCP Server

### 5.1 Features

- Document ingestion (PDF, Markdown, TXT, HTML)
- Text chunking and embedding (using Spring AI)
- Vector storage (in-memory or Milvus)
- Semantic search
- RAG (Retrieval Augmented Generation)

### 5.2 Tools Provided

- `kb_index_document` - Index a document
- `kb_search` - Semantic search
- `kb_get_document` - Retrieve document
- `kb_delete` - Delete from index

---

## 6. Implementation Order

1. **xarch-cloud module** - Spring Cloud infrastructure
2. **Nacos integration** - Service registry with MCP support
3. **Database MCP Server** - Multi-database support
4. **Knowledge Base MCP Server** - RAG implementation
5. **Filesystem MCP Server** - File operations

---

## 7. Configuration

```yaml
# Nacos Configuration
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: xarch-cloud
        group: DEFAULT_GROUP
      config:
        namespace: xarch-config
        group: DEFAULT_GROUP

# MCP Configuration
mcp:
  server:
    enabled: true
    port: 9090
  nacos:
    enabled: true
    service-name: xarch-mcp-database
```

---

## 8. Dependencies

```kotlin
// Spring Cloud Dependencies
implementation("org.springframework.cloud:spring-cloud-starter-gateway")
implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

// MCP Dependencies
implementation("org.springframework.ai:spring-ai-mcp")
implementation("org.springframework.ai:spring-ai-ollama")
```

---

## 9. Testing Strategy

- Unit tests for each MCP tool handler
- Integration tests for Nacos registration
- Database connection tests for each DB type
- Knowledge base indexing and search tests