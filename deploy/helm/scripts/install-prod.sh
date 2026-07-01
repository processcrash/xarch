#!/usr/bin/env bash
# ===============================================
# One-click install for production
# ===============================================
# Required env vars:
#   IMAGE_REGISTRY      - container registry (e.g. ghcr.io/processcrash)
#   IMAGE_TAG           - image tag to deploy (e.g. 1.0.0)
#   DB_PASSWORD         - master database password
#   NACOS_PASSWORD      - Nacos admin password
#   REDIS_PASSWORD      - Redis password
# Optional:
#   STORAGE_CLASS       - Kubernetes storage class (default: gp3)
#   INGRESS_HOST        - public hostname for ingress
#   TLS_SECRET_NAME     - TLS secret name in the cluster

set -euo pipefail

: "${IMAGE_REGISTRY:?must be set}"
: "${IMAGE_TAG:?must be set}"
: "${DB_PASSWORD:?must be set}"
: "${NACOS_PASSWORD:?must be set}"
: "${REDIS_PASSWORD:?must be set}"
STORAGE_CLASS="${STORAGE_CLASS:-gp3}"
INGRESS_HOST="${INGRESS_HOST:-xarch.example.com}"
TLS_SECRET_NAME="${TLS_SECRET_NAME:-xarch-tls}"

HERE="$(cd "$(dirname "$0")" && pwd)"
CHART_DIR="$(cd "$HERE/../xarch" && pwd)"
VALUES_FILE="$CHART_DIR/../values-prod.yaml"

# Pre-create TLS secret if not already there
if ! kubectl get secret "$TLS_SECRET_NAME" >/dev/null 2>&1; then
    echo "WARN: TLS secret $TLS_SECRET_NAME does not exist; provision cert-manager first" >&2
fi

echo "==> Installing xarch (prod profile) via Helm"
helm upgrade --install xarch "$CHART_DIR" \
    --namespace xarch --create-namespace \
    --values "$VALUES_FILE" \
    --set global.imageRegistry="$IMAGE_REGISTRY" \
    --set global.imageTag="$IMAGE_TAG" \
    --set mysql.auth.rootPassword="$DB_PASSWORD" \
    --set nacos.auth.admin.password="$NACOS_PASSWORD" \
    --set redis.auth.password="$REDIS_PASSWORD" \
    --set ingress.hosts[0].host="$INGRESS_HOST" \
    --set ingress.tls[0].secretName="$TLS_SECRET_NAME" \
    --set persistence.storageClass="$STORAGE_CLASS" \
    --atomic \
    --wait --timeout 30m \
    "$@"

echo ""
echo "==> Production install complete. Verify:"
echo "  kubectl -n xarch get pods"
echo "  kubectl -n xarch get ingress"
echo "  curl -k https://$INGRESS_HOST/actuator/health"
