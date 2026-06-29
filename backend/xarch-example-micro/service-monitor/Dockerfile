# syntax=docker/dockerfile:1.7
# =====================================================================
# service-monitor Dockerfile
# Server info / cache monitor / scheduled jobs micro-service — port 9004
# Multi-stage build using BuildKit cache mounts.
# Build context MUST be the xarch-example-micro/ root so the
# multi-module Gradle build (incl. :common) resolves correctly.
# =====================================================================

# ---------- Stage 1: builder ----------
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build

RUN apk add --no-cache bash curl

COPY . .

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/build/.gradle \
    ./gradlew :service-monitor:bootJar \
        --no-daemon \
        -x test \
        --console=plain

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:25-jre-alpine AS runtime

RUN addgroup -S xarch && adduser -S -G xarch -h /app -s /sbin/nologin xarch

WORKDIR /app

COPY --from=builder --chown=xarch:xarch \
    /build/service-monitor/build/libs/service-monitor-1.0.0.jar /app/app.jar

USER xarch

EXPOSE 9004

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:9004/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "/app/app.jar"]
