#!/usr/bin/env bash
# =====================================================================
# build-images.sh — build Docker images for all 6 xarch-example-micro
# micro-services. The build context for each service is the directory
# this script lives in (xarch-example-micro/), which is also where the
# multi-module Gradle build (settings.gradle, common/, etc.) lives.
#
# Requires:  Docker 23+ with BuildKit enabled (DOCKER_BUILDKIT=1 is the
#            default in modern Docker, so no flag is needed).
# Usage:     ./build-images.sh
#            DOCKER_BUILDKIT=1 ./build-images.sh          # explicit
#            TAG=1.2.0 ./build-images.sh                  # override tag
#            SERVICES="auth system" ./build-images.sh     # subset
# =====================================================================
set -euo pipefail

# Resolve script directory so the script works regardless of CWD.
cd "$(dirname "$0")"

TAG="${TAG:-1.0.0}"
IMAGE_PREFIX="${IMAGE_PREFIX:-xarch-example-micro}"

# All services in dependency-friendly build order.
DEFAULT_SERVICES=(auth system file monitor ai message)
SERVICES=(${SERVICES:-${DEFAULT_SERVICES[@]}})

# Sanity: refuse to run if Docker is not on PATH.
if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker CLI not found in PATH" >&2
  exit 1
fi

# BuildKit is required for --mount=type=cache in our Dockerfiles.
# Modern Docker has it on by default; set it explicitly to be safe.
export DOCKER_BUILDKIT=1

echo "==> Build context: $(pwd)"
echo "==> Image prefix:  ${IMAGE_PREFIX}"
echo "==> Image tag:     ${TAG}"
echo "==> Services:      ${SERVICES[*]}"
echo

for svc in "${SERVICES[@]}"; do
  image="${IMAGE_PREFIX}/service-${svc}:${TAG}"
  dockerfile="service-${svc}/Dockerfile"
  echo "==> Building ${image}"
  echo "    dockerfile: ${dockerfile}"
  docker build \
    --tag "${image}" \
    --file "${dockerfile}" \
    --label "org.opencontainers.image.source=https://github.com/processcrash/xarch" \
    --label "org.opencontainers.image.title=xarch-example-micro/service-${svc}" \
    --label "org.opencontainers.image.version=${TAG}" \
    .
  echo
done

echo "==> All images built successfully."
echo "    Inspect with:    docker images | grep ${IMAGE_PREFIX}"
echo "    Start stack:     docker compose up -d"
echo "    Or one service:  docker run --rm -p 9001:9001 ${IMAGE_PREFIX}/service-auth:${TAG}"
