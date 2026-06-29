# Docker guide for xarch-example-micro

This document explains how to build, run, scan and operate the six
Spring Cloud micro-services that make up `xarch-example-micro`.

| Service            | Port  | Image tag                                  | Health endpoint                       |
|--------------------|-------|--------------------------------------------|---------------------------------------|
| `service-auth`     | 9001  | `xarch-example-micro/service-auth:1.0.0`   | `http://localhost:9001/actuator/health` |
| `service-system`   | 9002  | `xarch-example-micro/service-system:1.0.0` | `http://localhost:9002/actuator/health` |
| `service-file`     | 9003  | `xarch-example-micro/service-file:1.0.0`   | `http://localhost:9003/actuator/health` |
| `service-monitor`  | 9004  | `xarch-example-micro/service-monitor:1.0.0`| `http://localhost:9004/actuator/health` |
| `service-ai`       | 9005  | `xarch-example-micro/service-ai:1.0.0`     | `http://localhost:9005/actuator/health` |
| `service-message`  | 9006  | `xarch-example-micro/service-message:1.0.0`| `http://localhost:9006/actuator/health` |

---

## 1. Prerequisites

* Docker Engine 23+ (BuildKit is on by default since 23.0).
* Docker Compose v2 (`docker compose ...`, not `docker-compose`).
* A working network that can reach `maven.aliyun.com` and
  `repo.maven.apache.org` from inside the builder stage (the Gradle
  build runs on first build with no populated cache).
* 4 GB free RAM on the host (Gradle daemon + JDK 25 image).
* 8 GB free disk for images, Gradle cache and intermediate layers.

Verify your environment:

```bash
docker --version        # Docker version 24.0.0+ recommended
docker compose version  # v2.20.0+
docker buildx version   # BuildKit CLI
```

---

## 2. Build images

The repository ships with a wrapper script that builds all six images
in one shot. Each image is built with BuildKit, so the Gradle
dependency cache and the per-build `.gradle` directory are mounted as
caches and survive between builds.

```bash
cd backend/xarch-example-micro
./build-images.sh
```

Useful environment variables:

| Variable        | Default                        | Meaning                                     |
|-----------------|--------------------------------|---------------------------------------------|
| `TAG`           | `1.0.0`                        | Image version tag                           |
| `IMAGE_PREFIX`  | `xarch-example-micro`          | Image name prefix                           |
| `SERVICES`      | `auth system file monitor ai message` | Subset to build (space-separated)     |
| `DOCKER_BUILDKIT` | `1` (set by the script)      | Enable BuildKit (required for cache mounts) |

Examples:

```bash
# Build a single service
SERVICES=auth ./build-images.sh

# Build with a custom tag
TAG=1.2.0-rc1 ./build-images.sh

# Build with a different image prefix
IMAGE_PREFIX=registry.example.com/xarch ./build-images.sh
```

### Building one image manually

If you don't want to use the wrapper:

```bash
cd backend/xarch-example-micro
DOCKER_BUILDKIT=1 docker build \
  -t xarch-example-micro/service-auth:1.0.0 \
  -f service-auth/Dockerfile .
```

The build context MUST be the `xarch-example-micro/` root, because
each Dockerfile runs `./gradlew :service-<name>:bootJar` which needs
to resolve the `:common` subproject and the parent
`build.gradle`/`settings.gradle`.

### BuildKit features used

* `--mount=type=cache,target=/root/.gradle`  — caches the downloaded
  Gradle distribution and dependency jars between builds. The first
  build is slow, subsequent builds are 5-10x faster.
* `--mount=type=cache,target=/build/.gradle` — caches the per-project
  Gradle working directory (incremental compile cache, configuration
  cache, etc.).
* `# syntax=docker/dockerfile:1.7` front-matter — enables the modern
  Dockerfile frontend (required for cache mounts).
* `-x test` — skips tests during image build. Run tests in CI before
  the build (`./gradlew test`), not inside the image.
* `--no-daemon` — leaves no orphan Gradle process inside the builder
  layer (the daemon is killed when the layer ends).

---

## 3. Run the stack

### Start everything

```bash
cd backend/xarch-example-micro
docker compose up -d
docker compose ps
docker compose logs -f service-auth
```

### Start only infrastructure

Useful while you are developing the services locally with
`./gradlew :service-auth:bootRun`:

```bash
docker compose up -d nacos mysql redis
```

### Bring everything down

```bash
docker compose down              # keep volumes
docker compose down -v           # also delete named volumes (DB data, etc.)
```

### Restart one service

```bash
docker compose restart service-auth
docker compose up -d --no-deps --build service-auth   # rebuild + restart
```

---

## 4. Image layout

Every service uses the same two-stage layout.

### Stage 1: `builder`

* Base: `eclipse-temurin:25-jdk-alpine`
* Installs `bash` and `curl` (Alpine ships with `ash`; `gradlew` prefers `bash`).
* Copies the entire `xarch-example-micro/` directory.
* Runs `./gradlew :service-<name>:bootJar --no-daemon -x test`.
* Produces `service-<name>/build/libs/service-<name>-1.0.0.jar`.

### Stage 2: `runtime`

* Base: `eclipse-temurin:25-jre-alpine` (JRE only — typically 70-80 MB
  smaller than the JDK image).
* Creates a non-root user `xarch:xarch` (uid assigned by Alpine).
* Copies the boot jar from the builder into `/app/app.jar`.
* Drops to the `xarch` user.
* Adds a `HEALTHCHECK` that polls the Spring Boot Actuator endpoint
  every 30s.
* Exposes the service port and runs Java with container-aware JVM
  flags.

---

## 5. JVM tuning

The runtime `ENTRYPOINT` uses exec form (JSON array), which means the
JVM runs as PID 1 inside the container and receives Unix signals
correctly (e.g. `docker stop` triggers a clean `SIGTERM` shutdown).

The flags are:

| Flag                              | Why                                                                  |
|-----------------------------------|----------------------------------------------------------------------|
| `-XX:+UseContainerSupport`        | Honor cgroup memory/CPU limits. Default in JDK 11+, kept explicit.   |
| `-XX:+UseG1GC`                    | Low-pause GC — good default for web workloads.                       |
| `-XX:MaxRAMPercentage=75.0`       | Scale heap to 75% of the container memory limit. The other 25% is   |
|                                   | reserved for Metaspace, code cache, threads, direct memory, etc.     |
| `-XX:+ExitOnOutOfMemoryError`     | Fail fast on OOM so the orchestrator can restart the pod.            |
| `-XX:+HeapDumpOnOutOfMemoryError` | Capture `java_pid<pid>.hprof` on OOM for post-mortem.                |
| `-Djava.security.egd=file:/dev/./urandom` | Avoid JVM startup stalls on `/dev/random`.                  |
| `-Dfile.encoding=UTF-8`           | Consistent charset across hosts.                                     |

When deploying to Kubernetes / Docker Swarm, set the container memory
limit and Java will size the heap automatically. For example, in
compose:

```yaml
services:
  service-auth:
    deploy:
      resources:
        limits:
          memory: 1024M
```

→ Java will set `-Xmx` to ~768 MB (75% of 1 GB).

---

## 6. Health checks

Every image has a `HEALTHCHECK` directive. Docker reports the
container state as `healthy` only when the Actuator endpoint returns
HTTP 200. The compose file repeats the same check so that dependent
services (e.g. `service-system`) wait for `service-auth` to be
`healthy` before they start.

| Service            | Health URL                                  |
|--------------------|---------------------------------------------|
| `service-auth`     | `http://localhost:9001/actuator/health`     |
| `service-system`   | `http://localhost:9002/actuator/health`     |
| `service-file`     | `http://localhost:9003/actuator/health`     |
| `service-monitor`  | `http://localhost:9004/actuator/health`     |
| `service-ai`       | `http://localhost:9005/actuator/health`     |
| `service-message`  | `http://localhost:9006/actuator/health`     |

Inspect from the host:

```bash
curl -fsS http://localhost:9001/actuator/health
# {"status":"UP"}
```

The `wget -q --spider` style used inside the container is preferable
to `curl` because `wget` is present in `eclipse-temurin:*-alpine` and
has no dependency on `libcurl`/ca-certs.

---

## 7. Image size and JVM tuning notes

Approximate sizes once Gradle and dependency caches are warm:

| Stage  | Base image                            | Size    |
|--------|---------------------------------------|---------|
| builder | `eclipse-temurin:25-jdk-alpine`      | ~470 MB |
| runtime | `eclipse-temurin:25-jre-alpine`      | ~260 MB |

The runtime image is what gets pushed to a registry and what runs in
production. To shrink it further:

* Switch to `eclipse-temurin:25-jre-jammy` (Ubuntu) only if a
  specific library is missing on Alpine. Alpine is preferred for size.
* Use `jlink` to build a custom JRE that contains only the JDK
  modules the application uses.
* Use Spring Boot's CDS / AppCDS (`-XX:ArchiveClassesAtExit=...`) or
  the `spring-boot:build-image` Gradle task for native-image
  experiments (requires GraalVM).

Inspect an image:

```bash
docker image inspect xarch-example-micro/service-auth:1.0.0 \
  --format '{{.Size}} / {{len .RootFS.Layers}} layers'
```

---

## 8. Security

* **Non-root user**: the runtime stage adds a `xarch:xarch` user and
  drops to it with `USER xarch`. No process inside the container can
  write outside `/app` or escalate privileges.
* **JRE-only base**: the runtime image has no `javac`, no `jar`, no
  Maven/Gradle. The attack surface is the JRE plus the boot jar.
* **No secrets baked in**: no `application.yml` with passwords, no
  Nacos credentials, no DB passwords. Configure them at runtime via
  environment variables or Spring Cloud Config served by Nacos.
* **HEALTHCHECK only**: there is no shell (`/bin/sh` is still present
  in Alpine JRE images; if you need to disable it, switch to a
  `distroless` base).

Recommended next steps:

* Scan with [Trivy](https://github.com/aquasecurity/trivy):

  ```bash
  trivy image --severity HIGH,CRITICAL \
    xarch-example-micro/service-auth:1.0.0
  ```
* Sign with [Cosign](https://github.com/sigstore/cosign):

  ```bash
  cosign sign --key cosign.key \
    xarch-example-micro/service-auth:1.0.0
  ```
* Drop the Linux capabilities you do not need:

  ```yaml
  services:
    service-auth:
      cap_drop: [ALL]
      security_opt: [no-new-privileges:true]
      read_only: true
      tmpfs: [/tmp]
  ```

---

## 9. Production deployment tips

1. **Pin the base image to a specific patch version** in your CI:
   change `eclipse-temurin:25-jre-alpine` to
   `eclipse-temurin:25.0.1_8-jre-alpine` (or whatever the current
   patch is) so a base-image refresh cannot break your build.
2. **Use a content-addressable image tag** (e.g. the Git SHA):
   ```bash
   GIT_SHA=$(git rev-parse --short HEAD)
   TAG=${TAG:-1.0.0-${GIT_SHA}} ./build-images.sh
   ```
3. **Push to a registry** that supports image scanning (GHCR, ECR,
   Harbor). Set `IMAGE_PREFIX=ghcr.io/your-org/xarch-example-micro`
   when building.
4. **Run as a Deployment / StatefulSet** in Kubernetes with:
   * `resources.requests` and `resources.limits` for CPU and memory.
   * `readinessProbe` and `livenessProbe` pointing at
     `/actuator/health` and `/actuator/health/liveness` respectively.
   * `securityContext.runAsNonRoot: true` and
     `securityContext.runAsUser: 1000` (matches the `xarch` uid).
   * `podDisruptionBudget` for graceful rollouts.
5. **Externalise configuration**: use Nacos Config (already wired
   through the `nacos-config` starter) for environment-specific
   `application.yml`. The Docker image ships only the defaults.
6. **Collect logs to stdout/stderr** — Spring Boot's
   `LogbackConfiguration` already does this. Ship them with a
   sidecar (Fluent Bit, Vector) or a node-level log collector.
7. **Set graceful shutdown**: add `server.shutdown=graceful` and
   `spring.lifecycle.timeout-per-shutdown-phase=20s` to the
   `application-docker.yml` profile so in-flight requests are not
   dropped during a rolling update.

---

## 10. Troubleshooting

**`./gradlew: not found`** when building inside Docker
→ The image needs `bash`. The Dockerfile installs it via
`apk add --no-cache bash`. If you forked the Dockerfile, double-check
that line.

**`Could not resolve org.springframework.boot:...`** during build
→ The build context is wrong. The `Dockerfile` must be invoked with
the `xarch-example-micro/` directory as the build context, not the
`service-<name>/` directory, because Gradle needs `settings.gradle`
and the `:common` module on the classpath.

**Health check fails on a fresh start**
→ The container enters `start_period=60s` before the first health
check. Spring Boot + Nacos discovery can take 30-60s to be fully
ready. Increase `start_period` if your network is slow.

**Port already in use on the host**
→ `docker compose ps` shows what is bound. Either stop the conflicting
process or change the host port mapping in `docker-compose.yml`
(`"9001:9001"` → `"19001:9001"`).

**Image is huge**
→ Confirm the second stage base is `25-jre-alpine` (not `-jdk`). Check
with `docker history xarch-example-micro/service-auth:1.0.0` — the
largest layers should be the `COPY --from=builder` lines.

**`wget: not found` in the healthcheck**
→ `wget` is shipped in `eclipse-temurin:*-alpine`. If you switched to
`*-jammy`, install it explicitly with `apt-get update && apt-get
install -y --no-install-recommends wget`.

---

## 11. File map

```
xarch-example-micro/
├── build.gradle
├── settings.gradle
├── docker-compose.yml          # infra + 6 services
├── build-images.sh             # build wrapper
├── DOCKER.md                   # this file
├── README.md
├── MIGRATION.md
├── common/                     # shared library (no Dockerfile)
├── service-auth/
│   ├── Dockerfile              # port 9001
│   ├── .dockerignore
│   ├── build.gradle
│   ├── bootstrap.yml
│   └── src/
├── service-system/
│   ├── Dockerfile              # port 9002
│   ├── .dockerignore
│   └── ...
├── service-file/
│   ├── Dockerfile              # port 9003
│   ├── .dockerignore
│   └── ...
├── service-monitor/
│   ├── Dockerfile              # port 9004
│   ├── .dockerignore
│   └── ...
├── service-ai/
│   ├── Dockerfile              # port 9005
│   ├── .dockerignore
│   └── ...
└── service-message/
    ├── Dockerfile              # port 9006
    ├── .dockerignore
    └── ...
```
