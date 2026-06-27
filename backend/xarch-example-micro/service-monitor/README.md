# service-monitor

Monitor micro-service — server metrics, cache, scheduled jobs.

| Property | Value |
|---|---|
| Port | 9004 |
| Spring application name | `xarch-service-monitor` |
| Database tables (owned) | `xarch_monitor_job`, `_job_log` |
| Depends on | `:common`, `xarch-*` starters, Nacos, Quartz |

## Controllers

| Controller | Path | Responsibility |
|---|---|---|
| `SysServerController`  | `/monitor/server`   | Server CPU / mem / JVM metrics |
| `SysCacheController`   | `/monitor/cache`    | Cache monitor + clear operations |
| `JobController`        | `/monitor/job`      | Scheduled job CRUD + run |
| `SysJobLogController`  | `/monitor/jobLog`   | Job execution log + clean |

## Build & run

```bash
./gradlew :service-monitor:bootRun
```