# xarch-storage-spring-boot-starter

A unified storage abstraction for the xarch platform. It exposes a single
`FileStorageService` facade that can talk to:

- Local filesystem
- MinIO (and any S3-compatible object store)
- Aliyun OSS
- AWS S3 (and S3-compatible services like Cloudflare R2, DigitalOcean Spaces, etc.)

The starter is designed to be drop-in: add the dependency, configure the
backend you want to use, and inject `FileStorageService` into your business
code. No more strategy boilerplate per service.

## Quick start

### 1. Add the dependency

```gradle
dependencies {
    implementation project(':xarch-spring-boot-starter:xarch-storage-spring-boot-starter')
}
```

### 2. Pick a default backend in `application.yml`

```yaml
xarch:
  storage:
    default-type: local   # or minio / aliyun_oss / s3
    configs:
      local:
        base-path: ./xarch-files
        public-base-url: /files
      minio:
        endpoint: http://localhost:9000
        access-key: minioadmin
        secret-key: minioadmin
        default-bucket: xarch
```

### 3. Use it

```java
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService storage;

    @PostMapping("/upload")
    public StorageResult upload(@RequestParam("file") MultipartFile file) {
        return storage.upload(file, UploadOptions.builder()
                .objectKeyPrefix("avatars/2026")
                .contentType(file.getContentType())
                .build());
    }
}
```

## Highlights

- **One API, four backends** &mdash; switch the configured `default-type` and your
  application code keeps working.
- **Object-level metadata** &mdash; every upload returns a `StorageResult`
  carrying the bucket, key, access URL, etag, size and content type.
- **Presigned URLs** &mdash; `getPresignedUrl(bucket, key, duration)` works the
  same for every backend.
- **Auto bucket creation** &mdash; MinIO, Aliyun OSS and S3 can be configured to
  create the bucket on first use.
- **CDN friendly** &mdash; every backend supports an optional `cname` to
  rewrite access URLs to a custom domain.
- **Resource-leak safe** &mdash; all `InputStream` lifecycles are managed with
  try-with-resources in the public API and helper base class.

See [docs/STORAGE.md](../../../../docs/STORAGE.md) for the full configuration
reference, performance tips and security guidance.
