# service-file

File management micro-service.

| Property | Value |
|---|---|
| Port | 9003 |
| Spring application name | `xarch-service-file` |
| Database tables (owned) | `xarch_file_resource`, `_temp_file`, `_storage_config` |
| Depends on | `:common`, `xarch-*` starters, Nacos, Feign clients |

## Controllers

| Controller | Path | Responsibility |
|---|---|---|
| `FileController`     | `/file`            | Unified upload / download / preview + storage config CRUD |
| `ResourceController` | `/api/resources`   | Resource page, single + batch upload, delete |
| `TempFileController` | `/api/temp-files`  | Temp file upload + CRUD |
| `ExcelController`    | `/api/excel`       | Excel import / export |

## Cross-service calls (exported)

`FileFeignClient` — `POST /file/upload` (multipart) for peer services
that need to attach files.

## Storage backends

The service is wired to support local, MinIO and Aliyun OSS out of the
box. Backend implementations live behind the `StorageFactory` (to be
extracted from the monolith).

## Build & run

```bash
./gradlew :service-file:bootRun
```