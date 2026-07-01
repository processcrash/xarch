# Installation Guide

This document walks through installing xarch on dev, staging, and production
Kubernetes clusters.

## 1. Prerequisites

| Tool | Version |
| --- | --- |
| Kubernetes | `>=1.24.0` |
| Helm | `>=3.10` |
| `kubectl` | matches cluster minor |
| A default StorageClass | required for MySQL / Redis / Nacos |
| Nginx Ingress Controller | required when `ingress.enabled=true` |
| cert-manager | required for `letsencrypt-*` ClusterIssuers |

Verify cluster access:

```bash
kubectl cluster-info
kubectl get nodes
helm version
```

## 2. Required secrets

The chart references credentials by `secretKeyRef`. You can either:

- Use the **bootstrap** mode where each sub-chart creates its own Secret
  (development only).
- Provision secrets externally with [Sealed Secrets][sealed] or
  [External Secrets Operator][eso] (recommended for staging / production).

Required Secret keys per workload:

| Secret | Key | Used by |
| --- | --- | --- |
| `xarch-secrets` | `DB_USERNAME` | every microservice, gateway |
| `xarch-secrets` | `DB_PASSWORD` | every microservice, gateway, mysql |
| `xarch-secrets` | `NACOS_USERNAME` | every microservice, gateway |
| `xarch-secrets` | `NACOS_PASSWORD` | every microservice, gateway, nacos |
| `xarch-secrets` | `REDIS_PASSWORD` | every microservice, gateway, redis |
| `mysql-auth` | `mysql-root-password` | mysql |
| `mysql-auth` | `mysql-password` | mysql |
| `redis-auth` | `redis-password` | redis |
| `nacos-auth` | `nacos-password` | nacos |

You can generate the canonical `xarch-secrets` once with:

```bash
kubectl create secret generic xarch-secrets -n xarch \
  --from-literal=DB_USERNAME=root \
  --from-literal=DB_PASSWORD=$(openssl rand -hex 16) \
  --from-literal=NACOS_USERNAME=nacos \
  --from-literal=NACOS_PASSWORD=$(openssl rand -hex 16) \
  --from-literal=REDIS_PASSWORD=$(openssl rand -hex 16)
```

[sealed]: https://github.com/bitnami-labs/sealed-secrets
[eso]: https://external-secrets.io/

## 3. Development

```bash
cd deploy/helm/xarch
helm dependency update
helm install xarch . -n xarch-dev --create-namespace \
  -f ../values-dev.yaml
```

The dev profile uses `latest` image tags, a single replica per workload, and
disables Ingress, observability, HPA, and PDB. MySQL, Redis, and Nacos are
in-cluster with bootstrap Secrets.

Open the gateway locally:

```bash
kubectl port-forward -n xarch-dev svc/xarch-cloud-gateway 8080:8080
curl http://localhost:8080/actuator/health
```

## 4. Staging

```bash
helm install xarch . -n xarch-staging --create-namespace \
  -f ../values-staging.yaml
```

Staging mirrors the production shape with:

- Two replicas per workload
- HPA + PDB enabled
- Single TLS-enabled Ingress host
- Observability (Prometheus + Grafana) on

Apply the TLS certificate (when using cert-manager):

```bash
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: xarch-staging-tls
  namespace: xarch-staging
spec:
  secretName: xarch-staging-tls
  dnsNames:
    - staging.xarch.example.com
  issuerRef:
    name: letsencrypt-staging
    kind: ClusterIssuer
EOF
```

## 5. Production

```bash
helm install xarch . -n xarch --create-namespace \
  -f ../values-prod.yaml
```

Production requirements:

- **External MySQL**: a managed instance (RDS, Cloud SQL, Vitess). Update
  `global.mysql.host` and provision the `xarch-secrets` Secret accordingly.
- **External Redis**: managed Redis (ElastiCache, Memorystore, or
  Keyspaces). Update `global.redis.host`.
- **Nacos cluster mode + external MySQL persistence**: set in `values-prod.yaml`.
- **Image pull secret**: `imagePullSecret.create=true` plus registry credentials.
- **Backup / DR**: see below.

## 6. Domain and DNS

For each environment, create a wildcard (or explicit A/CNAME) record pointing
at the Ingress Controller's external address:

```text
*.xarch.example.com  ->  <ingress-controller-lb>
```

Verify with:

```bash
nslookup api.xarch.example.com
curl -I https://api.xarch.example.com/actuator/health
```

## 7. TLS certificate management

Two supported strategies:

### a) cert-manager (recommended)

```yaml
ingress:
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  tls:
    - secretName: xarch-prod-tls
      hosts:
        - xarch.example.com
```

### b) Manual

```bash
kubectl create secret tls xarch-prod-tls \
  --cert=path/to/cert.pem --key=path/to/key.pem \
  -n xarch
```

Then reference `secretName: xarch-prod-tls` in `ingress.tls`.

## 8. Backup strategy

### MySQL

If running in-cluster, use `cron + mysqldump` or `mariabackup`:

```bash
mysqldump -h mysql.xarch.svc.cluster.local -u root -p \
  --single-transaction --routines --triggers xarch | gzip > backup-$(date +%F).sql.gz
```

For managed MySQL, rely on the cloud provider's automated backups plus a
PITR-enabled configuration.

### Redis

If persistence is on, schedule an RDB dump or copy `appendonly.aof`:

```bash
kubectl exec -n xarch deploy/redis -- redis-cli BGSAVE
kubectl cp xarch/<pod>:/data/dump.rdb ./dump-$(date +%F).rdb
```

For managed Redis, use the provider's snapshot / export features.

### Nacos

Nacos 2.x persists configuration to its embedded Derby DB or to an external
MySQL. The recommended approach is to point Nacos at the same MySQL used by
xarch (see `nacos.externalMysql` in `values-prod.yaml`) and back up that
database. For Derby, schedule a `kubectl cp` of `/home/nacos/data`.

### Kubernetes resources

```bash
helm get values xarch -n xarch > xarch-values-backup.yaml
helm get manifest xarch -n xarch > xarch-manifest-backup.yaml
```

## 9. Disaster recovery

| Scenario | RPO | RTO | Procedure |
| --- | --- | --- | --- |
| Single workload pod lost | 0 | < 1 min | Kubernetes reschedules automatically |
| MySQL PVC lost | last backup | < 30 min | Recreate PVC, restore from mysqldump |
| Redis PVC lost | last backup | < 5 min | Recreate PVC, replay AOF |
| Nacos PVC lost | last backup | < 10 min | Recreate PVC, restore config DB |
| Namespace lost | last backup | < 30 min | `helm install` + restore PVCs from snapshots |
| Cluster lost | last backup | < 1 h | Provision cluster, restore from off-cluster backup (Velero / cloud-native snapshots) |

For cluster-wide DR, integrate [Velero][velero] with the cloud provider's
object storage to take regular snapshots of all PVCs and the etcd.

[velero]: https://velero.io/
