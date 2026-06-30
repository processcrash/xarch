# xarch Storage Guide

The `xarch-storage-spring-boot-starter` ships a single, opinionated file
storage abstraction that unifies local disk, MinIO, Aliyun OSS and AWS S3
behind one service. This document is the canonical reference for
configuration, operations, performance and security.

## Table of contents

1. [Architecture](#architecture)
2. [Configuration](#configuration)
3. [Local storage](#local-storage)
4. [MinIO](#minio)
5. [Aliyun OSS](#aliyun-oss)
6. [AWS S3 (and S3-compatible)](#aws-s3)
7. [Programming model](#programming-model)
8. [Presigned URLs](#presigned-urls)
9. [CDN integration](#cdn-integration)
10. [Performance tips](#performance-tips)
11. [Security](#security)
12. [Troubleshooting](#troubleshooting)

## Architecture

```
+--------------------------+        +----------------------------+
|  REST controller / biz   |  -->   |  FileStorageService        |
+--------------------------+        +-------------+--------------+
                                                  |
                                                  v
                                     +---------------------------+
                                     |  StorageProviderFactory   |
                                     +-------------+-------------+
                                                   |
            +---------------------+----------------+----------------+
            |                     |                |                |
            v                     v                v                v
   +-----------------+   +-----------------+ +-------------+ +-----------+
   | LocalStorage    |   | MinioStorage    | | AliyunOss   | | S3Storage |
   +-----------------+   +-----------------+ +-------------+ +-----------+
```

- `FileStorageService` is the only type application code should depend on.
- `StorageProviderFactory` resolves the right provider for each request.
- Each `StorageProvider` is a thin, focused adapter around the underlying
  SDK and exposes a single uniform contract:
  `putObject / getObject / deleteObject / exists / getPresignedUrl / getAccessUrl`.

The starter uses Spring Boot's `@AutoConfiguration` so it activates
automatically when present on the classpath.

## Configuration

All properties live under the `xarch.storage` prefix:

```yaml
xarch:
  storage:
    default-type: local
    configs:
      local:      { enabled: true,  base-path: ./xarch-files, public-base-url: /files }
      minio:      { enabled: false, endpoint: ..., access-key: ..., secret-key: ... }
      aliyun-oss: { enabled: false, endpoint: ..., access-key-id: ..., access-key-secret: ... }
      s3:         { enabled: false, region: ..., access-key-id: ..., secret-access-key: ... }
```

The `default-type` accepts the codes `local`, `minio`, `aliyun_oss`, `s3`
(case-insensitive). Unknown codes fall back to `local` so misconfiguration
will not break the application.

Each `BackendConfig` may also specify a default bucket, a key prefix and a
CDN/cname domain that are reused by the corresponding provider.

## Local storage

```yaml
xarch:
  storage:
    configs:
      local:
        base-path: /var/xarch/files
        public-base-url: https://static.example.com/files
```

- The `base-path` is created on startup if it does not exist.
- Path traversal is blocked: object keys resolving outside of `base-path`
  raise a `StorageException`.
- The `public-base-url` is prepended to all access URLs. If you serve
  static files via a Spring `WebMvcConfigurer` or a reverse proxy, set
  the public URL to match.

Disable with `xarch.storage.configs.local.enabled=false` (the factory will
then fall back to whichever backend is configured as default).

## MinIO

```yaml
xarch:
  storage:
    configs:
      minio:
        endpoint: http://minio.local:9000
        access-key: minioadmin
        secret-key: minioadmin
        region: us-east-1
        default-bucket: xarch
        cname: https://cdn.example.com
        auto-create-bucket: true
```

Features:

- Wraps the MinIO Java SDK 8.5.10.
- Auto-creates the bucket on first use (toggle via
  `auto-create-bucket`).
- `cname` is preferred for access URLs, otherwise the standard
  `endpoint/bucket/key` form is used.
- The provider also works against any S3-compatible service that speaks
  the MinIO protocol (Ceph RADOS Gateway, SeaweedFS, etc.).

## Aliyun OSS

```yaml
xarch:
  storage:
    configs:
      aliyun-oss:
        endpoint: https://oss-cn-hangzhou.aliyuncs.com
        access-key-id: <your AccessKeyId>
        access-key-secret: <your AccessKeySecret>
        security-token: <optional, only for STS>
        bucket-name: xarch
        cname: https://oss-cdn.example.com
        auto-create-bucket: true
```

Features:

- Wraps the Aliyun OSS Java SDK 3.17.4.
- Supports STS temporary credentials via `security-token`.
- ETag is taken from the SDK response (MD5 hex of the object).
- Access URL is `https://<bucket>.<endpoint>/<key>` unless a `cname` is
  configured.

## AWS S3

```yaml
xarch:
  storage:
    configs:
      s3:
        region: us-east-1
        access-key-id: <AWS access key id>
        secret-access-key: <AWS secret access key>
        endpoint:   # leave blank for AWS, set for S3-compatible
        default-bucket: xarch
        cname: https://cdn.example.com
        path-style-access: true
        auto-create-bucket: true
```

- Uses AWS SDK v2 (`software.amazon.awssdk:s3:2.25.0`).
- `path-style-access: true` is required for most S3-compatible services.
- Setting `endpoint` lets you talk to R2, DigitalOcean Spaces, MinIO
  gateways, etc.

## Programming model

The high-level `FileStorageService` is the recommended API. It accepts a
`MultipartFile` and an `UploadOptions` record:

```java
public record UploadOptions(
        String bucket,
        String objectKeyPrefix,
        StorageType storageType,
        boolean generateUniqueKey,
        String contentType,
        Map<String,String> metadata,
        String originalFilename
) { }
```

Example:

```java
StorageResult result = storageService.upload(file, UploadOptions.builder()
        .bucket("avatars")
        .objectKeyPrefix("user-" + userId)
        .storageType(StorageType.MINIO)
        .contentType(file.getContentType())
        .build());

// result.accessUrl(), result.objectKey(), result.size(), ...
```

For raw bytes:

```java
storageService.upload(bytes, "report.pdf", "application/pdf",
        UploadOptions.builder().storageType(StorageType.S3).build());
```

Downloads, deletes and presigned URLs:

```java
try (InputStream in = storageService.download(StorageType.MINIO, "xarch", key)) {
    // ...
}
storageService.delete(StorageType.LOCAL, "xarch", key);
String url = storageService.presignedUrl(StorageType.ALIYUN_OSS, "xarch", key, Duration.ofMinutes(10));
```

## Presigned URLs

All remote backends support `getPresignedUrl(bucket, key, duration)`. The
duration is a `java.time.Duration`; the SDK takes care of converting it to
the appropriate unit (seconds for MinIO/S3, milliseconds for Aliyun OSS).

Local storage does not implement signing; the call falls back to the
public access URL. The decision is logged at `DEBUG` level.

Typical use cases:

- Time-limited download links sent by email.
- Browser-direct uploads from the SPA.
- Sharing files with external users without exposing the bucket.

## CDN integration

Set `cname` to your CDN endpoint. The provider will use it for both the
permanent access URL and the presigned URL, when supported. With Aliyun
OSS you can also bind a custom domain to the bucket and put Cloudflare or
Aliyun CDN in front of it for global acceleration.

## Performance tips

- **Large files**: MinIO, Aliyun OSS and S3 all support multipart uploads
  natively. The starter exposes a `putObject(InputStream, size, contentType)`
  that streams content; for very large files (>100 MB) consider using the
  underlying SDK directly via `provider.getClient()`.
- **Connection reuse**: the `MinioClient`, `OSS` and `S3Client` instances
  are pooled internally and are safe to share across threads &mdash; keep
  the provider bean as a singleton.
- **Avoid buffering in memory**: by default, the local provider reads the
  full upload into memory to compute the etag. For multi-gigabyte files
  consider a content-addressed key (so duplicate uploads dedupe naturally)
  and use `Files.copy` directly via the provider's `copyToPath` helper.
- **Bucket locality**: co-locate your application and storage in the same
  region to minimise upload latency and avoid egress costs.
- **Warm-up**: the SDKs do lazy connection setup. The first call after
  startup pays a small one-off cost; trigger a dummy health-check during
  application warm-up if you need predictable first-request latency.

## Security

- **Credentials**: never commit access keys to source control. Use Spring
  Cloud Config, environment variables or a secrets manager.
- **Bucket policies**: prefer private buckets + presigned URLs over
  public-read policies. The starter generates presigned URLs with the
  caller-supplied duration; default to short durations (5&ndash;15 min).
- **Path traversal**: the local provider normalises object keys and
  refuses to escape its base path. Remote providers are safe by
  construction (keys are not used as filesystem paths).
- **Content sniffing**: serve `Content-Disposition: attachment` for
  unknown content types. Use the returned `contentType` from
  `StorageResult` rather than the original filename.
- **Audit logging**: every provider logs put / get / delete / presign
  events at `INFO`. For full audit trail, configure a SLF4J appender
  with structured logging.
- **Least privilege**: grant the storage credentials only the
  `s3:PutObject` / `s3:GetObject` / `s3:DeleteObject` actions on the
  buckets they need to access.

## Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `StorageException: No storage provider registered for type S3` | No provider bean for the requested type | Make sure the SDK is on the classpath and `xarch.storage.configs.s3.enabled=true` |
| `Failed to ensure bucket exists: ...` | Credentials lack `s3:CreateBucket` | Either grant the permission or set `auto-create-bucket: false` and pre-create the bucket |
| Presigned URL always returns 403 | Clock skew between the application and the storage backend | Synchronise clocks with NTP; skew of more than ~5 minutes breaks SigV4 |
| Access URL returns 404 even though `exists` is true | `cname` or CDN cache not picking up the new object | Force a CDN purge or wait for TTL |
| Local provider writes succeed but access returns 404 | `public-base-url` not matching the static-resource mapping | Align the Spring `WebMvcConfigurer.addResourceLocations` with `public-base-url` |
| Upload fails with `AccessDenied` on Aliyun OSS | RAM policy does not include `oss:PutObject` | Update the RAM policy to grant the action on the target bucket |
