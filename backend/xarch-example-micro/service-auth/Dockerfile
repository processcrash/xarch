# syntax=docker/dockerfile:1.7
# =====================================================================
# service-auth Dockerfile
# Authentication & user micro-service — port 9001
# Multi-stage build using BuildKit cache mounts.
# Build context MUST be the xarch-example-micro/ root so the
# multi-module Gradle build (incl. :common) resolves correctly.
# =====================================================================

# ---------- Stage 1: builder ----------
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build

# Install bash (Alpine ships with ash; gradlew prefers bash) + curl
# for the optional gradle download fallback and BuildKit healthchecks.
RUN apk add --no-cache bash curl

# Copy the entire multi-module project. .dockerignore at the build
# context root excludes build/, .gradle/, IDE files, etc.
COPY . .

# Build only this module. The :common subproject is built transitively
# as a project dependency. Run with --no-daemon to keep the build
# self-contained inside the image layer.
# -x test : skip tests during image build (run them in CI instead)
# --parallel : parallel project execution
# --build-cache : reuse configuration cache across builds
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/build/.gradle \
    ./gradlew :service-auth:bootJar \
        --no-daemon \
        -x test \
        --console=plain

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:25-jre-alpine AS runtime

# Add a non-root user for runtime security.
# -D = no password, -s /sbin/nologin = no interactive shell.
RUN addgroup -S xarch && adduser -S -G xarch -h /app -s /sbin/nologin xarch

WORKDIR /app

# Copy the bootJar from the builder. The Spring Boot Gradle plugin
# produces service-auth-1.0.0.jar (version comes from root build.gradle).
COPY --from=builder --chown=xarch:xarch \
    /build/service-auth/build/libs/service-auth-1.0.0.jar /app/app.jar

# wget is already present in eclipse-temurin:*-alpine JRE images,
# so the HEALTHCHECK below works out of the box.
USER xarch

EXPOSE 9001

# Spring Boot Actuator health endpoint.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:9001/actuator/health || exit 1

# JVM container-aware tuning:
#   -XX:+UseContainerSupport        : honor cgroup memory limits (default in JDK 11+, kept explicit)
#   -XX:+UseG1GC                    : low-pause GC, good default for web workloads
#   -XX:MaxRAMPercentage=75.0       : scale heap to 75% of container memory
#   -XX:+ExitOnOutOfMemoryError     : fail fast so orchestrator can restart the pod
#   -XX:+HeapDumpOnOutOfMemoryError : capture heap on OOM for post-mortem
#   -Djava.security.egd             : faster /dev/urandom for session IDs
#   -Dfile.encoding=UTF-8           : consistent charset across hosts
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "/app/app.jar"]
