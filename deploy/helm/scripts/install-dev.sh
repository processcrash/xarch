#!/usr/bin/env bash
# ===============================================
# One-click install for development
# ===============================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CHART_DIR="$(cd "$HERE/../xarch" && pwd)"
VALUES_FILE="$CHART_DIR/../values-dev.yaml"

echo "==> Installing xarch (dev profile) via Helm"
helm upgrade --install xarch "$CHART_DIR" \
    --namespace xarch-dev --create-namespace \
    --values "$VALUES_FILE" \
    --set global.imageRegistry="${IMAGE_REGISTRY:-ghcr.io/processcrash}" \
    --wait --timeout 10m \
    "$@"

echo ""
echo "==> Useful commands:"
echo "  kubectl -n xarch-dev get pods"
echo "  kubectl -n xarch-dev get svc"
echo "  kubectl -n xarch-dev port-forward svc/xarch-gateway 8080:9000"
echo "  kubectl -n xarch-dev port-forward svc/xarch-nacos 8848:8848"
