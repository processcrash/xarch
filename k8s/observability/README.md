# xarch Observability Stack

Production-ready monitoring for the xarch microservices platform
(Spring Boot 4.0 + JDK 25 + Nacos + MySQL/PostgreSQL + Redis, deployed to
the `xarch` Kubernetes namespace).

This directory ships only the **configuration** for the monitoring stack.
The actual `Deployment`, `Service`, `ConfigMap` manifests for Prometheus
and Grafana are produced by `kustomize` and live alongside this folder
(so they are decoupled from the configuration and easy to overlay per
environment).

## Layout

```
k8s/observability/
├── README.md
├── kustomization.yaml                            # kustomize entrypoint
├── prometheus.yml                                # Prometheus scrape + rule config
├── alerts/
│   └── prometheus-alerts.yaml                    # Prometheus-format alert rules
└── grafana/provisioning/
    ├── datasources/
    │   └── prometheus.yaml                       # Auto-provisions Prometheus DS
    ├── dashboards/
    │   ├── dashboards.yaml                       # Dashboard provider
    │   └── xarch-overview.json                   # Platform overview dashboard
    └── alerting/
        ├── alert-rules.yaml                      # Grafana managed alerts
        ├── contact-points.yaml                   # Webhook receiver
        └── policies.yaml                         # Notification routing
```

## Deploy

```bash
# From the repository root
kubectl apply -k k8s/observability/

# Verify the config maps were created
kubectl -n xarch get configmap -l app.kubernetes.io/part-of=xarch
```

The `kustomization.yaml` exposes five ConfigMaps:

| ConfigMap                   | Mount path inside the pod                                                |
|-----------------------------|--------------------------------------------------------------------------|
| `prometheus-config`         | `/etc/prometheus/prometheus.yml`, `/etc/prometheus/alerts/*.yaml`        |
| `grafana-datasources`       | `/etc/grafana/provisioning/datasources/`                                 |
| `grafana-dashboards-provider` | `/etc/grafana/provisioning/dashboards/dashboards.yaml`                 |
| `grafana-dashboards-xarch`  | `/var/lib/grafana/dashboards/` (loaded by the provider above)            |
| `grafana-alerting`          | `/etc/grafana/provisioning/alerting/`                                    |

> Tip: when running Prometheus standalone (e.g. `docker run`), mount the
> `alerts/` directory at `/etc/prometheus/alerts/` so the rule_files glob
> picks it up.

## Access Grafana

```bash
kubectl -n xarch port-forward svc/grafana 3000:3000
```

Open <http://localhost:3000>.

Default credentials: **`admin` / `admin`**.

> **Security:** the bundled `admin/admin` password is for local evaluation
> only. Override it in production by setting the
> `grafana-admin-password` Secret (referenced by the Grafana Deployment)
> and re-applying with kustomize.

## Dashboards

| Title                     | UID              | Description                                                  |
|---------------------------|------------------|--------------------------------------------------------------|
| xarch Platform Overview   | `xarch-overview` | One screen for service health, latency, JVM, DB, MCP, nodes  |

### Panels in `xarch-overview`

| #  | Title                       | Type        | Source metric                                          |
|----|-----------------------------|-------------|--------------------------------------------------------|
| 1  | Service Health              | stat        | `up`                                                   |
| 2  | Request Rate (req/s)        | timeseries  | `rate(http_server_requests_seconds_count[5m])`        |
| 3  | Error Rate (5xx)            | timeseries  | `http_server_requests_seconds_count{status=~"5.."}`    |
| 4  | P99 Latency                 | timeseries  | `histogram_quantile(0.99, ...)`                        |
| 5  | JVM Memory Used (heap)      | timeseries  | `jvm_memory_used_bytes{area="heap"}`                   |
| 6  | JVM CPU Usage               | timeseries  | `jvm_cpu_recent_utilization`                           |
| 7  | GC Pause Duration (max)     | timeseries  | `rate(jvm_gc_pause_seconds_max[5m])`                   |
| 8  | Thread Count (live)         | timeseries  | `jvm_threads_live_threads`                             |
| 9  | Hikari Active Connections   | timeseries  | `hikaricp_connections_active` / `_max`                 |
| 10 | Pod Restarts                | table       | `kube_pod_container_status_restarts_total`             |
| 11 | Pod CPU Usage               | timeseries  | `rate(container_cpu_usage_seconds_total[5m])`          |
| 12 | Pod Memory Usage            | timeseries  | `container_memory_usage_bytes`                         |
| 13 | Nacos Health                | stat        | `up{job="nacos"}`                                      |
| 14 | MCP Server Status           | table       | `up{job=~"xarch-mcp-.*"}` + `mcp_request_duration_p99`|
| 15 | Node CPU Usage              | timeseries  | `100 - rate(node_cpu_seconds_total{mode="idle"}[5m])`  |
| 16 | Node Memory Usage           | timeseries  | `(1 - MemAvail/MemTotal) * 100`                        |

Template variables: `$instance`, `$namespace` (default `xarch`), `$service`.

## Alert rules

Two layers, intentionally redundant:

1. **Prometheus rules** (`alerts/prometheus-alerts.yaml`) - evaluated by
   Prometheus, intended for Alertmanager / long-term routing.
2. **Grafana managed rules** (`grafana/provisioning/alerting/alert-rules.yaml`)
   - evaluated by Grafana, useful when Alertmanager is not yet wired.

### Prometheus groups

| Group             | Alert                            | Severity | Threshold                             |
|-------------------|----------------------------------|----------|---------------------------------------|
| ApplicationHealth | XarchServiceDown                 | critical | `up==0` for 2m                        |
| ApplicationHealth | XarchServiceCrashLooping         | warning  | restart rate > 0 for 10m              |
| ApplicationHealth | XarchPodNotReady                 | warning  | `kube_pod_status_ready==0` for 5m     |
| Jvm               | JvmMemoryUsageHigh               | warning  | heap > 85% for 5m                     |
| Jvm               | JvmMemoryUsageCritical           | critical | heap > 95% for 2m                     |
| Jvm               | JvmCpuUsageHigh                  | warning  | CPU > 80% for 10m                     |
| Jvm               | JvmThreadDeadlock                | critical | deadlocked > 0                        |
| Jvm               | JvmGcPauseLong                   | warning  | gc pause > 0.5s for 5m                |
| HttpRequests      | Http5xxErrorRate                 | critical | 5xx > 0.1 req/s for 2m                |
| HttpRequests      | Http4xxErrorRate                 | warning  | 4xx > 5 req/s for 5m                  |
| HttpRequests      | HttpRequestLatencyP99            | warning  | p99 > 2s for 5m                       |
| Database          | HikariConnectionPoolExhausted    | warning  | active/max > 0.9 for 2m               |
| Database          | HikariConnectionAcquisitionSlow  | warning  | acquire > 1s for 5m                   |
| McpServers        | McpServerUnhealthy               | critical | `up==0` for 3m                        |
| McpServers        | McpServerHighLatency             | warning  | p99 > 5s for 5m                       |
| Nacos             | NacosDown                        | critical | `up==0` for 2m                        |
| DiskAndResources  | NodeDiskSpaceLow                 | warning  | free < 10% for 10m                    |
| DiskAndResources  | NodeCpuUsageHigh                 | warning  | usage > 85% for 10m                   |
| DiskAndResources  | NodeMemoryUsageHigh              | warning  | usage > 85% for 5m                    |

## Adding a custom alert

### Option A - Prometheus (preferred for long-term routing)

1. Add a new rule to `alerts/prometheus-alerts.yaml`:

   ```yaml
   - name: MyGroup
     interval: 30s
     rules:
       - alert: MyNewAlert
         expr: up{job="my-service"} == 0
         for: 2m
         labels:
           severity: warning
           team: platform
         annotations:
           summary: "my-service is down"
           description: "..."
           runbook_url: "https://wiki.xarch.internal/runbooks/my-new-alert"
   ```

2. Commit, then re-apply:

   ```bash
   kubectl -n xarch rollout restart deploy/prometheus
   ```

3. Verify in Prometheus UI -> Alerts.

### Option B - Grafana managed alert

1. Edit `grafana/provisioning/alerting/alert-rules.yaml` and add a new
   entry under `groups[0].rules`. Each rule needs a unique `uid`, the
   Prometheus datasource `PBFA97CFB590B2093` (or the UID returned by your
   Grafana instance - export it from the Datasource page if you have
   multiple Prometheus sources), and the metric expression.
2. Add severity / team labels so the policy can route it correctly.
3. Re-apply:

   ```bash
   kubectl -n xarch rollout restart deploy/grafana
   ```

The provisioning is **editable** (`editable: true`) so Grafana UI
changes do not break reloads. To force pure-config, set `editable:
false`.

## Verifying

```bash
# Prometheus config
kubectl -n xarch exec deploy/prometheus -- \
  promtool check config /etc/prometheus/prometheus.yml

# Prometheus rules
kubectl -n xarch exec deploy/prometheus -- \
  promtool check rules /etc/prometheus/alerts/prometheus-alerts.yaml

# Grafana provisioning is loaded - check the pod log
kubectl -n xarch logs deploy/grafana | grep -i provisioning

# Targets up in Prometheus
open http://localhost:9090/targets      # after port-forward
```

## Notes

- The `kubernetes-pods` job only scrapes pods annotated with
  `prometheus.io/scrape: "true"`. The static jobs under each xarch
  service exist as a defensive layer in case annotations are missing.
- Drop rules remove Go-runtime metrics (`go_*`, `process_*`) - they are
  noisy and unused.
- The `McpServerHighLatency` rule assumes MCP servers export
  `mcp_request_duration_seconds_p99` - the rule will simply be silent
  until that metric is wired in (add it to the MCP micrometer registry).
