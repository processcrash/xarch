# Multi-stage build for Spring Boot with Gradle
# Build stage
FROM gradle:8.10-jdk25 AS builder

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle settings.gradle.kts ./
COPY gradle gradle

# Copy project structure
COPY xarch-core-spring-boot-starter xarch-core-spring-boot-starter
COPY xarch-db-spring-boot-starter xarch-db-spring-boot-starter
COPY xarch-web-spring-boot-starter xarch-web-spring-boot-starter
COPY xarch-cache-spring-boot-starter xarch-cache-spring-boot-starter
COPY xarch-example xarch-example

# Build application
WORKDIR /app/xarch-example
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar
COPY --from=builder /app/xarch-example/build/libs/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]