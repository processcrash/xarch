# ===============================================
# go-ai-agent-base — production Dockerfile
# ===============================================
# Multi-stage build: compile in a golang image, ship a tiny distroless runtime.

# ---- build stage -------------------------------------------------------
FROM golang:1.23-alpine AS build

WORKDIR /src

# Cache module downloads
COPY go.mod go.sum ./
RUN go mod download

COPY . .

# Build server + agent binaries statically.
ARG VERSION=dev
ARG COMMIT=unknown
RUN CGO_ENABLED=0 GOOS=linux go build \
        -ldflags="-s -w -X main.version=${VERSION} -X main.commit=${COMMIT}" \
        -o /out/server ./cmd/server
RUN CGO_ENABLED=0 GOOS=linux go build \
        -ldflags="-s -w -X main.version=${VERSION} -X main.commit=${COMMIT}" \
        -o /out/agent ./cmd/agent

# ---- runtime stage -----------------------------------------------------
FROM gcr.io/distroless/static-debian12:nonroot

WORKDIR /app

# Distroless ships with a nonroot user (uid 65532).
COPY --from=build /out/server /app/server
COPY --from=build /out/agent /app/agent
COPY --from=build /src/configs /app/configs

ENV PATH="/app"

EXPOSE 8080

# Healthcheck pings /api/v1/health.
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD ["/app/server", "--healthcheck"]

# Default to the HTTP server; override with CMD=["/app/agent","chat"]
# to drop into the CLI.
ENTRYPOINT ["/app/server"]
CMD []