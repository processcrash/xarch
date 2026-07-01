// Java MCP server: database-mcp
// Mirrors the Node.js sibling at node-mcp-servers/database-mcp/.
// Exposes 8 tools for DB introspection + a config resource + a sql-query prompt.

plugins {
    application
}

dependencies {
    implementation(project(":mcp-runtime"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

application {
    mainClass.set("com.xarch.mcp.database.DatabaseMcpServerMain")
}
