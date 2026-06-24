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
    "xarch-spring-boot-starter:xarch-core-spring-boot-starter",
    "xarch-spring-boot-starter:xarch-db-spring-boot-starter",
    "xarch-spring-boot-starter:xarch-web-spring-boot-starter",
    "xarch-spring-boot-starter:xarch-cache-spring-boot-starter",
    "xarch-example",
    // Spring Cloud modules
    "xarch-spring-cloud:xarch-cloud:xarch-cloud-starter-nacos",
    "xarch-spring-cloud:xarch-cloud:xarch-cloud-starter-gateway",
    "xarch-spring-cloud:xarch-cloud:xarch-cloud-starter-mcp",
    // MCP Servers
    "xarch-spring-boot-starter:xarch-mcp:xarch-mcp-database",
    "xarch-spring-boot-starter:xarch-mcp:xarch-mcp-knowledge",
    "xarch-spring-boot-starter:xarch-mcp:xarch-mcp-filesystem",
    "xarch-spring-boot-starter:xarch-mcp:xarch-mcp-vector",
    // Business example modules
    "examples:cms",
    "examples:oa",
    "examples:crm"
)