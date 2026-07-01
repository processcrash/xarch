# GPU support for `service-ai`

The `service-ai` workload is the AI inference and embedding component of
xarch. It can be backed by GPU nodes when the cluster has them and the
NVIDIA device plugin is installed.

## Prerequisites

1. Kubernetes `>=1.24`
2. [NVIDIA device plugin for Kubernetes][nvdp] installed
   (`kubectl apply -f nvidia-device-plugin.yml`)
3. A node label identifying GPU nodes, e.g. `gpu=true`
4. The `service-ai` image built with CUDA / ONNX Runtime / TensorRT bindings
5. The NVIDIA runtime class installed
   ([Node Feature Discovery][nfd] recommended)

Verify GPU discovery:

```bash
kubectl get nodes -L nvidia.com/gpu
```

## Enabling GPU scheduling

In your values file, override `serviceAi`:

```yaml
serviceAi:
  enabled: true
  name: service-ai
  port: 9005
  replicaCount: 1
  nodeSelector:
    gpu: "true"
  tolerations:
    - key: nvidia.com/gpu
      operator: Exists
      effect: NoSchedule
  resources:
    requests:
      cpu: 1000m
      memory: 4Gi
      nvidia.com/gpu: 1
    limits:
      cpu: 4000m
      memory: 16Gi
      nvidia.com/gpu: 1
```

> The `microservice` sub-chart passes `nodeSelector`, `tolerations` and
> `affinity` through to the Pod spec verbatim, so any value placed under
> the per-service override is honoured.

## Multi-GPU and MIG

For multi-GPU pods, request more GPUs:

```yaml
resources:
  requests:
    nvidia.com/gpu: 2
  limits:
    nvidia.com/gpu: 2
```

For NVIDIA Multi-Instance GPU (MIG), request MIG partitions instead:

```yaml
resources:
  requests:
    nvidia.com/mig-1g.5gb: 1
  limits:
    nvidia.com/mig-1g.5gb: 1
```

## Time-slicing and sharing GPUs

Time-slicing is configured on the device plugin, not in the chart. See the
[official docs][ts] for the ConfigMap and apply it cluster-wide. Once
enabled, multiple replicas can share a single physical GPU by requesting
fractional resources (`nvidia.com/gpu: 0.5`).

## Observability

GPU metrics are exported by the `dcgm-exporter` (when installed). To scrape
them via Prometheus, add:

```yaml
microservices:
  serviceMonitor:
    enabled: true
    additionalLabels:
      release: prometheus
```

and deploy `dcgm-exporter` via its own chart with a `ServiceMonitor` in the
same namespace.

## Production checklist

- [ ] GPU nodes tagged and tainted
- [ ] Tolerations and nodeSelector applied to `service-ai`
- [ ] Resource requests/limits include `nvidia.com/gpu`
- [ ] PodDisruptionBudget present (`podDisruptionBudget.enabled=true`)
- [ ] HPA enabled (`autoscaling.enabled=true`) — note that HPA does not
      scale GPU workloads on GPU utilization without the
      [Prometheus Adapter][promadapter] for custom metrics
- [ ] Image built with CUDA matching the cluster driver version
- [ ] DCGM exporter deployed and scraped

[nvdp]: https://github.com/NVIDIA/k8s-device-plugin
[nfd]: https://github.com/kubernetes-sigs/node-feature-discovery
[ts]: https://github.com/NVIDIA/k8s-device-plugin#time-sharing
[promadapter]: https://github.com/kubernetes-sigs/prometheus-adapter
