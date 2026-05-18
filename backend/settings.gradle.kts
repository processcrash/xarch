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
    "xarch-example"
)