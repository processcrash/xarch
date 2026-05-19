# 部署手册

## 部署架构

```
                    ┌─────────────┐
                    │   Browser   │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │    Nginx    │
                    │   (HTTPS)   │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
    │  Vue3   │       │  API    │       │  API    │
    │  Front  │◄────▶│ Gateway │◄────►│ Services│
    └─────────┘       └─────────┘       └────┬────┘
                                              │
                    ┌──────────────────────────┼──────────────┐
                    │                          │              │
               ┌────▼────┐              ┌────▼────┐   ┌────▼────┐
               │ Nacos   │              │   DB    │   │  Redis  │
               │ 3.2     │              │  (PG)   │   │          │
               └─────────┘              └─────────┘   └─────────┘
```

---

## 环境要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 50 GB | 100 GB+ |
| OS | Ubuntu 20.04 / CentOS 7+ | Ubuntu 22.04 LTS |

---

## Docker 部署 (推荐)

### 1. 准备环境

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | bash

# 安装 Docker Compose
sudo apt-get install docker-compose
```

### 2. 配置

编辑 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: xarch
      POSTGRES_USER: xarch
      POSTGRES_PASSWORD: xarch123
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/xarch
      SPRING_REDIS_HOST: redis

  frontend:
    build: ./vue3-admin
    ports:
      - "80:80"

volumes:
  postgres_data:
```

### 3. 启动

```bash
docker-compose up -d

# 检查状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 验证

- 前端：http://localhost
- 后端 API：http://localhost/api
- API 文档：http://localhost:8080/doc.html

---

## Kubernetes 部署

### 1. 准备集群

```bash
# 检查 kubectl
kubectl version

# 检查集群
kubectl cluster-info
```

### 2. 部署基础设施

```bash
# 部署 PostgreSQL
kubectl apply -f k8s/base/postgresql.yaml

# 部署 Redis
kubectl apply -f k8s/base/redis.yaml

# 部署 Nacos
kubectl apply -f k8s/base/nacos.yaml
```

### 3. 部署应用

```bash
# 部署后端
kubectl apply -f k8s/overlays/production/backend.yaml

# 部署前端
kubectl apply -f k8s/overlays/production/frontend.yaml

# 部署 Nginx Ingress
kubectl apply -f k8s/overlays/production/ingress.yaml
```

### 4. 检查状态

```bash
# 查看所有 Pods
kubectl get pods

# 查看服务
kubectl get svc

# 查看日志
kubectl logs -f deployment/xarch-backend
```

---

## 手动部署

### 1. 构建

```bash
# 后端构建
cd backend
./gradlew build -x test

# 前端构建
cd vue3-admin
pnpm build
```

### 2. 配置 Nginx

```nginx
server {
    listen 80;
    server_name xarch.example.com;

    # 前端静态文件
    location / {
        root /var/www/xarch;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 支持
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 3. 启动服务

```bash
# 启动后端
nohup java -jar xarch-example/build/libs/xarch-example.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/xarch \
  --spring.redis.host=localhost \
  --server.port=8080 \
  > app.log 2>&1 &

# 检查状态
curl http://localhost:8080/actuator/health
```

---

## 监控部署

### 1. Spring Boot Admin

```bash
cd backend/xarch-spring-cloud/xarch-cloud-admin-server
./gradlew bootRun

# 访问：http://localhost:8090
# 默认用户：admin / admin123
```

### 2. Loki + Grafana

```bash
cd logging
docker-compose up -d

# 访问 Grafana：http://localhost:3001
# 默认用户：admin / admin123
```

### 3. Prometheus

```bash
# Prometheus 配置已包含在 docker-compose 中
# 访问：http://localhost:9090
```

---

## 安全配置

### 1. 防火墙

```bash
# Ubuntu
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable

# CentOS
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 2. HTTPS 配置

```nginx
server {
    listen 443 ssl http2;
    server_name xarch.example.com;

    ssl_certificate /etc/nginx/ssl/server.crt;
    ssl_certificate_key /etc/nginx/ssl/server.key;
    ssl_protocols TLSv1.2 TLSv1.3;

    # ... 其他配置
}
```

---

## 备份策略

### 数据库备份

```bash
# PostgreSQL 备份
pg_dump -U xarch -d xarch > backup_$(date +%Y%m%d).sql

# 自动化备份脚本
0 2 * * * /opt/backup/backup.sh  # 每天凌晨 2 点
```

### 文件备份

```bash
# 备份上传文件
tar -czf files_backup_$(date +%Y%m%d).tar.gz /var/xarch/uploads
```

---

## 扩展阅读

- [安装指南](INSTALL.md)
- [架构设计](ARCHITECTURE.md)
- [开发规范](DEVELOPMENT.md)