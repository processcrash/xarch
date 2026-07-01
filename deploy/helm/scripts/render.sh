#!/usr/bin/env bash
# ===============================================
# Render all Helm profiles for review
# ===============================================
# Usage:
#   ./deploy/helm/scripts/render.sh [profile]
# profiles: dev (default) | staging | prod | all

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CHART_DIR="$(cd "$HERE/../xarch" && pwd)"
OUTPUT_DIR="$CHART_DIR/rendered"
PROFILE="${1:-all}"

mkdir -p "$OUTPUT_DIR"

render_profile() {
    local profile="$1"
    local values_file="$CHART_DIR/values-$profile.yaml"
    local output_file="$OUTPUT_DIR/$profile.yaml"

    if [[ ! -f "$values_file" ]]; then
        echo "ERROR: values file not found: $values_file" >&2
        return 1
    fi

    echo "==> Rendering $profile (using $values_file)"
    helm template xarch "$CHART_DIR" \
        --values "$values_file" \
        > "$output_file"
    echo "    wrote $output_file ($(wc -l < "$output_file") lines)"
}

case "$PROFILE" in
    all)
        for p in dev staging prod; do
            render_profile "$p"
        done
        ;;
    dev|staging|prod)
        render_profile "$PROFILE"
        ;;
    *)
        echo "Unknown profile: $PROFILE (use dev|staging|prod|all)" >&2
        exit 1
        ;;
esac

echo ""
echo "All renders complete. Diff against k8s/base for sanity:"
echo "  diff -r $OUTPUT_DIR $CHART_DIR/../../k8s/base"
