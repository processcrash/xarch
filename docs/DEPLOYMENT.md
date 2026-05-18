# xarch Deployment Guide

## Overview

xarch supports multiple deployment methods:
- Docker Compose (development/small production)
- Kubernetes (production)

## Prerequisites

### Hardware Requirements

| Environment | CPU | Memory | Disk |
|-------------|-----|--------|------|
| Development | 2 cores | 4GB | 20GB |
| Small Production | 4 cores | 8GB | 50GB |
| Medium Production | 8 cores | 16GB | 100GB |
| Large Production | 16 cores | 32GB | 200GB |

### Software Requirements

- Java 25 (or 17+)
- Node.js 20+
- Docker 24+
- Docker Compose 2.20+
- Kubernetes 1.28+ (for K8s deployment)

---

## Docker Compose Deployment

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-repo/xarch.git
cd xarch

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f backend
```

### Services

| Service | Port | Description |
|---------|------|-------------|
| MySQL | 3306 | Database |
| Redis | 6379 | Cache |
| Backend | 8080 | Spring Boot API |
| Frontend | 80 | Vue.js Admin |

### Accessing Services

- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Default Credentials

| Service | Username | Password |
|---------|----------|---------|
| MySQL | root | root123 |
| Backend | admin | admin123 |

---

## Kubernetes Deployment

### Prerequisites

```bash
# Install kubectl
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/linux/amd64/kubectl"

# Install Helm
curl -fsSL https://get.helm.sh/helm-v3.12.0-linux-amd64.tar.gz | tar -xz
mv linux-amd64/helm /usr/local/bin/
```

### Namespace Creation

```bash
kubectl create namespace xarch
```

### Database Deployment

```bash
# Deploy MySQL
kubectl apply -f k8s/base/mysql.yaml

# Deploy Redis
kubectl apply -f k8s/base/redis.yaml

# Deploy Nacos (with MySQL persistence)
kubectl apply -f k8s/base/nacos-mysql-persistence.yaml
```

### Application Deployment

```bash
# Build backend image
docker build -t xarch-backend:latest ./backend

# Or use pre-built image
# Edit k8s/overlays/prod/kustomization.yaml

# Deploy backend
kubectl apply -k k8s/overlays/prod
```

### Verify Deployment

```bash
# Check pods
kubectl get pods -n xarch

# Check services
kubectl get svc -n xarch

# View logs
kubectl logs -n xarch -l app.kubernetes.io/name=xarch-example
```

### Ingress Configuration

```bash
# Apply ingress
kubectl apply -f k8s/base/ingress.yaml

# Edit /etc/hosts for local testing
echo "127.0.0.1 api.xarch.com" >> /etc/hosts
```

---

## Environment Configuration

### Backend Configuration

Create `application-prod.yml`:

```yaml
spring:
  application:
    name: xarch-example

  datasource:
    url: jdbc:mysql://mysql:3306/xarch?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${MYSQL_PASSWORD}

  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD}

  cloud:
    nacos:
      server-addr: nacos:8848
      discovery:
        namespace: xarch-cloud
        group: DEFAULT_GROUP

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Frontend Configuration

Update `vue3-admin/src/utils/http.ts`:

```typescript
const baseURL = process.env.VUE_APP_API_BASE_URL || 'http://api.xarch.com'
```

---

## Production Best Practices

### Security

1. **Enable HTTPS**
   ```yaml
   # ingress.yaml
   tls:
     - hosts:
         - api.xarch.com
       secretName: xarch-tls
   ```

2. **Use Secrets for Credentials**
   ```bash
   kubectl create secret generic xarch-secrets \
     --from-literal=MYSQL_PASSWORD=your-secure-password \
     --from-literal=REDIS_PASSWORD=your-secure-password \
     -n xarch
   ```

3. **Enable NACOS Authentication**
   ```yaml
   env:
     - name: NACOS_AUTH_ENABLE
       value: "true"
   ```

### Performance

1. **Enable Redis Cache**
   ```yaml
   spring:
     data:
       redis:
         lettuce:
           pool:
             max-active: 50
             max-idle: 20
             min-idle: 5
   ```

2. **Configure Connection Pool**
   ```yaml
   spring:
     datasource:
       druid:
         initial-size: 10
         max-active: 50
         min-idle: 5
   ```

### Monitoring

1. **Deploy Prometheus**
   ```bash
   kubectl apply -f k8s/observability/prometheus.yml
   ```

2. **Deploy Grafana**
   ```bash
   kubectl apply -f k8s/observability/grafana.yaml
   ```

3. **Configure Alerts**
   ```yaml
   # prometheus/rules/xarch-alerts.yaml
   groups:
     - name: xarch
       rules:
         - alert: HighErrorRate
           expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
           for: 5m
           labels:
             severity: critical
           annotations:
             summary: High error rate detected
   ```

### Backup

1. **Database Backup**
   ```bash
   kubectl exec -it mysql-0 -n xarch -- \
     mysqldump -u root -p xarch > backup.sql
   ```

2. **Configure PVC Snapshot**
   ```yaml
   apiVersion: snapshot.storage.k8s.io/v1
   kind: VolumeSnapshot
   metadata:
     name: mysql-backup
   spec:
     volumeSnapshotClassName: csi-aws-vsc
     source:
       persistentVolumeClaimName: mysql-pvc
   ```

---

## Troubleshooting

### Common Issues

#### 1. Pod Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n xarch

# Check events
kubectl get events -n xarch --sort-by='.lastTimestamp'
```

#### 2. Database Connection Failed

```bash
# Check MySQL logs
kubectl logs mysql-0 -n xarch

# Test connection
kubectl run -it --rm debug --image=mysql:8.0 --restart=Never -n xarch -- \
  mysql -h mysql -u root -p
```

#### 3. Nacos Service Not Found

```bash
# Check Nacos status
kubectl exec nacos-0 -n xarch -- curl localhost:8848/nacos/v1/console/health/readiness

# Check service endpoints
kubectl get endpoints nacos -n xarch
```

#### 4. Frontend Cannot Connect to Backend

```bash
# Check ingress
kubectl describe ingress xarch-ingress -n xarch

# Check backend service
kubectl get svc xarch-backend -n xarch
```

### Logs Collection

```bash
# All pods
kubectl logs -n xarch --tail=100 -f

# Specific service
kubectl logs -n xarch -l app.kubernetes.io/name=xarch-example -f

# Previous logs (restart)
kubectl logs -n xarch -l app.kubernetes.io/name=xarch-example --previous
```

---

## Scaling

### Horizontal Pod Autoscaling

```bash
# Create HPA for backend
kubectl autoscale deployment xarch-backend \
  -n xarch \
  --cpu-percent=70 \
  --min=2 \
  --max=10

# Check HPA status
kubectl get hpa -n xarch
```

### Vertical Pod Autoscaling

```yaml
# backend-vpa.yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: xarch-backend-vpa
  namespace: xarch
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: xarch-backend
  updatePolicy:
    updateMode: "Auto"
```

---

## Upgrade

### Rolling Update

```bash
# Update image version
kubectl set image deployment/xarch-backend \
  backend=xarch-backend:v1.1.0 \
  -n xarch

# Check rollout status
kubectl rollout status deployment/xarch-backend -n xarch
```

### Backup Before Upgrade

```bash
# Backup database
kubectl exec mysql-0 -n xarch -- \
  mysqldump -u root -p${MYSQL_PASSWORD} xarch > xarch-backup-$(date +%Y%m%d).sql

# Backup configurations
kubectl get configmap -n xarch -o yaml > configs-backup.yaml
```
