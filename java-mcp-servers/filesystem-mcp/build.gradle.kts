// Java MCP server: filesystem-mcp
// Mirrors the Node.js sibling at node-mcp-servers/filesystem-mcp/.
// Exposes 10 tools for file operations + 2 resources + 1 prompt.

plugins {
    application
}

dependencies {
    implementation(project(":mcp-runtime"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation("org.mockito:mockito-core:5.14.2")
}

application {
    mainClass.set("com.xarch.mcp.filesystem.FilesystemMcpServerMain")
}
