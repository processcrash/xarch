# Multi-stage build for Spring Boot application
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

# Copy pom files
COPY pom.xml .
COPY xarch-bom/pom.xml xarch-bom/
COPY xarch-common-core/pom.xml xarch-common-core/
COPY xarch-db-spring-boot-starter/pom.xml xarch-db-spring-boot-starter/
COPY xarch-web-spring-boot-starter/pom.xml xarch-web-spring-boot-starter/
COPY xarch-cache-spring-boot-starter/pom.xml xarch-cache-spring-boot-starter/
COPY xarch-example/pom.xml xarch-example/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY xarch-common-core/src xarch-common-core/src
COPY xarch-db-spring-boot-starter/src xarch-db-spring-boot-starter/src
COPY xarch-web-spring-boot-starter/src xarch-web-spring-boot-starter/src
COPY xarch-cache-spring-boot-starter/src xarch-cache-spring-boot-starter/src
COPY xarch-example/src xarch-example/src

# Build
RUN mvn package -pl xarch-example -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar
COPY --from=builder /app/xarch-example/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]