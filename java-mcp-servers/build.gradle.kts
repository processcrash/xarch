// Root build for java-mcp-servers
// Each subproject is a standalone CLI that speaks the MCP stdio protocol
// (same wire format as the Node.js and Python siblings under
// node-mcp-servers/ and py-mcp-servers/). Each server can be wired into
// Claude Desktop, Cursor, or any MCP-compatible client.

plugins {
    java
    application
}

allprojects {
    group = "com.xarch.mcp"
    version = "1.0.0"

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        mavenCentral()
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }
}

subprojects {
    plugins.apply("application")

    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    dependencies {
        // JSON for JSON-RPC 2.0 wire format
        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.0")

        // Logging (stderr only — never pollute stdout which is the MCP channel)
        implementation("org.slf4j:slf4j-api:2.0.16")
        implementation("org.slf4j:slf4j-simple:2.0.16")

        // Test
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        testImplementation("org.assertj:assertj-core:3.26.3")
        testImplementation("org.mockito:mockito-core:5.14.2")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
        }
    }
}
