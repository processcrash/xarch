pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/aliyun") }
        gradlePluginPortal()
    }
}

rootProject.name = "xarch"

include(
    "xarch-core-spring-boot-starter",
    "xarch-db-spring-boot-starter",
    "xarch-web-spring-boot-starter",
    "xarch-cache-spring-boot-starter",
    "xarch-example",
    // Spring Cloud modules
    "xarch-cloud:xarch-cloud-starter-nacos",
    "xarch-cloud:xarch-cloud-starter-gateway",
    "xarch-cloud:xarch-cloud-starter-mcp",
    // MCP Servers
    "xarch-mcp:xarch-mcp-database",
    "xarch-mcp:xarch-mcp-knowledge",
    "xarch-mcp:xarch-mcp-filesystem"
)