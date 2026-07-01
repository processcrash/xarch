// Java MCP server: vector-mcp
// Mirrors the Node.js sibling at node-mcp-servers/vector-mcp/.
// Exposes 9 tools for vector CRUD + similarity search over a configurable
// in-memory store (swap-in Qdrant/Milvus/pgvector adapters welcome).

plugins {
    application
}

dependencies {
    implementation(project(":mcp-runtime"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

application {
    mainClass.set("com.xarch.mcp.vector.VectorMcpServerMain")
}