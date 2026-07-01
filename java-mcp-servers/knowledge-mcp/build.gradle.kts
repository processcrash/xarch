// Java MCP server: knowledge-mcp
// Mirrors the Node.js sibling at node-mcp-servers/knowledge-mcp/.
// Provides an in-memory knowledge base with chunked indexing and
// TF-IDF-like keyword search, plus 9 tools, 1 resource, and 1 prompt.

plugins {
    application
}

dependencies {
    implementation(project(":mcp-runtime"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

application {
    mainClass.set("com.xarch.mcp.knowledge.KnowledgeMcpServerMain")
}