# xarch Helm Chart

Production-grade Helm chart for deploying the [xarch](https://github.com/processcrash/xarch)
AI-Enabled Enterprise Backend Framework to Kubernetes.

## Overview

This chart is an **umbrella chart** that packages the full xarch stack:

| Component | Sub-chart | Default | Description |
| --- | --- | --- | --- |
| `service-auth`    | `microservice` | enabled | Authentication & user service (port 9001) |
| `service-system`  | `microservice` | enabled | System / business service (port 9002) |
| `service-file`    | `microservice` | enabled | File storage service (port 9003) |
| `service-monitor` | `microservice` | enabled | Monitoring bridge (port 9004) |
| `service-ai`      | `microservice` | enabled | AI / MCP service (port 9005) |
| `service-message` | `microservice` | enabled | Messaging service (port 9006) |
| `xarch-cloud-gateway` | `gateway` | enabled | Spring Cloud Gateway (port 8080) |
| `nacos` | `nacos` | enabled | Service registry / config center |
| `mysql` | `mysql` | enabled | MySQL 8 single instance |
| `redis` | `redis` | enabled | Redis 7 cache |
| `observability` | `observability` | disabled | Prometheus + Grafana |

## Directory layout

```
deploy/helm/
├── README.md                 # this file
├── INSTALL.md                # step-by-step install for dev/staging/prod
├── GPU.md                    # GPU support for service-ai
├── values-dev.yaml           # dev overrides
├── values-staging.yaml       # staging overrides
├── values-prod.yaml          # production overrides
├── scripts/                  # helper shell scripts
└── xarch/                    # the umbrella chart
    ├── Chart.yaml
    ├── values.yaml
    ├── values.schema.json
    ├── templates/            # umbrella-level manifests
    │   ├── namespace.yaml
    │   ├── configmap-global.yaml
    │   ├── secret-registry.yaml
    │   ├── networkpolicy-default.yaml
    │   ├── resourcequota.yaml
    │   ├── limitrange.yaml
    │   ├── rbac.yaml
    │   ├── ingress.yaml
    │   ├── NOTES.txt
    │   └── tests/test-connection.yaml
    └── charts/               # local sub-charts
        ├── common/           # library chart, helpers only
        ├── microservice/     # generic Spring Boot microservice
        ├── gateway/          # Spring Cloud Gateway
        ├── mysql/            # MySQL 8
        ├── redis/            # Redis 7
        ├── nacos/            # Nacos 2.x
        └── observability/    # Prometheus + Grafana
```

## Prerequisites

- Kubernetes `>=1.24.0`
- Helm `>=3.10`
- A default StorageClass (or override `global.storageClass`)
- An Ingress Controller (only when `ingress.enabled=true`)
- cert-manager (recommended when terminating TLS at the Ingress)

## Installation

Add local sub-charts (no remote repo required) and render:

```bash
cd deploy/helm/xarch
helm dependency update
```

Install with one of the bundled profiles:

```bash
# Development
helm install xarch ./xarch -n xarch-dev --create-namespace \
  -f ./xarch/values.yaml -f ./values-dev.yaml

# Staging
helm install xarch ./xarch -n xarch-staging --create-namespace \
  -f ./xarch/values.yaml -f ./values-staging.yaml

# Production
helm install xarch ./xarch -n xarch --create-namespace \
  -f ./xarch/values.yaml -f ./values-prod.yaml
```

The release name is `xarch` by default; override with `--release-name`.

## Upgrade

```bash
helm upgrade xarch ./xarch -n xarch \
  -f ./xarch/values.yaml -f ./values-prod.yaml
```

## Rollback

```bash
helm history xarch -n xarch
helm rollback xarch 2 -n xarch
```

## Uninstall

```bash
helm uninstall xarch -n xarch
```

By default the namespace and PVCs are **not** removed. Delete them explicitly
when no longer needed:

```bash
kubectl delete namespace xarch
kubectl delete pvc -n xarch --all
```

## Customization

All values are documented in [`xarch/values.yaml`](xarch/values.yaml). Common
knobs:

- `global.imageRegistry` — registry prefix for every image
- `microservices.image.tag` — pin a single version for all six services
- `microservices.resourcePreset` — pick `small` / `medium` / `large`
- `microservices.autoscaling.enabled` — turn on HPA
- `gateway.replicaCount` — number of gateway pods
- `ingress.hosts` — list of `(host, paths)` rules
- `nacos.mode` — `standalone` or `cluster`
- `observability.enabled` — include Prometheus + Grafana

## Per-environment values

Three ready-made profiles are shipped:

- [`values-dev.yaml`](values-dev.yaml) — `latest` images, 1 replica, no
  ingress, no observability, no PDB, small resource preset.
- [`values-staging.yaml`](values-staging.yaml) — pinned version, 2 replicas,
  HPA on, observability on, single ingress host, basic TLS.
- [`values-prod.yaml`](values-prod.yaml) — pinned versions, 3+ replicas, HPA
  + PDB on, full observability, external MySQL / Redis, image pull secrets,
  cluster-mode Nacos with external MySQL persistence.

## Linting and rendering

```bash
helm lint ./xarch
helm template xarch ./xarch -f ./values-dev.yaml > /tmp/xarch-dev.yaml
helm install xarch ./xarch -n xarch --dry-run --debug -f ./values-prod.yaml
```

The `scripts/` directory contains convenience wrappers:

```bash
./scripts/render.sh        # render all profiles
./scripts/install-dev.sh   # one-click dev install
./scripts/install-prod.sh  # one-click prod install
./scripts/dry-run.sh       # helm install --dry-run for all profiles
```

## Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Pods stuck in `Init` waiting for Nacos | Nacos not yet ready | Wait ~30s, check `kubectl logs -l app.kubernetes.io/name=nacos` |
| `ImagePullBackOff` | Missing pull secret | `kubectl create secret docker-registry ghcr-secret ...` or set `imagePullSecret.create=true` |
| CrashLoopBackOff on gateway | Wrong DB password Secret | Check `kubectl get secret -n xarch xarch-secrets` matches the values |
| HPA never scales | `autoscaling.enabled=false` | Set the flag and re-render |
| Ingress returns 404 | Wrong `ingressClassName` | Verify with `kubectl get ingressclasses` |
| 503 from gateway | Back-end service has no endpoints | `kubectl get pods -l app.kubernetes.io/component=...` |

See [`INSTALL.md`](INSTALL.md) for detailed installation, backup and
disaster-recovery procedures.
