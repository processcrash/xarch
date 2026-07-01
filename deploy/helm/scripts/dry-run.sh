#!/usr/bin/env bash
# ===============================================
# Helm install --dry-run all profiles
# ===============================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CHART_DIR="$(cd "$HERE/../xarch" && pwd)"

for profile in dev staging prod; do
    echo "==> Dry-run: $profile"
    helm install xarch "$CHART_DIR" \
        --values "$CHART_DIR/../values-$profile.yaml" \
        --dry-run --debug \
        > "/tmp/xarch-$profile-dryrun.yaml" 2>&1
    echo "    rendered to /tmp/xarch-$profile-dryrun.yaml ($(wc -l < /tmp/xarch-$profile-dryrun.yaml) lines)"
done

echo ""
echo "All dry-runs complete."
