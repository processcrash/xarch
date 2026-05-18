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

include("xarch-starter-core", "xarch-starter-db", "xarch-starter-web", "xarch-starter-cache", "xarch-example")