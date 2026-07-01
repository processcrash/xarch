#!/usr/bin/env bash
# ===============================================
# Helm chart lint + render test
# ===============================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CHART_DIR="$(cd "$HERE/../xarch" && pwd)"

echo "==> helm lint"
helm lint "$CHART_DIR"

echo ""
echo "==> helm template (default values)"
helm template xarch "$CHART_DIR" \
    --debug \
    > /tmp/xarch-lint.yaml 2>&1
echo "    wrote /tmp/xarch-lint.yaml ($(wc -l < /tmp/xarch-lint.yaml) lines)"

echo ""
echo "==> helm template (prod profile)"
helm template xarch "$CHART_DIR" \
    --values "$CHART_DIR/../values-prod.yaml" \
    > /tmp/xarch-prod.yaml 2>&1
echo "    wrote /tmp/xarch-prod.yaml ($(wc -l < /tmp/xarch-prod.yaml) lines)"

echo ""
echo "Lint and template complete."
