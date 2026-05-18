# xarch - Enterprise Backend Development Framework

A modern enterprise-level backend framework built with Spring Boot 4.0, providing rapid development capabilities through modular starters.

## Project Structure

```
xarch/
├── backend/                                    # Spring Boot Backend (Gradle)
│   ├── xarch-bom/                              # Bill of Materials
│   ├── xarch-core-spring-boot-starter/        # Core utilities & annotations
│   ├── xarch-db-spring-boot-starter/          # Database (MyBatis Plus, Druid)
│   ├── xarch-web-spring-boot-starter/         # Web (REST, Swagger, Sa-Token)
│   ├── xarch-cache-spring-boot-starter/       # Cache (Redis, Redisson)
│   └── xarch-example/                         # Example application (14 controllers)
│
├── vue3-admin/                                 # Vue 3 Frontend
│   ├── src/
│   │   ├── api/                               # API calls
│   │   ├── views/                             # Pages
│   │   ├── router/                            # Routes
│   │   ├── stores/                            # Pinia stores
│   │   └── utils/                             # Utilities
│   ├── nginx/                                 # Nginx config
│   └── Dockerfile
│
├── docker-compose.yml                          # Docker orchestration
└── init.sql                                    # Database initialization
```

## Tech Stack

### Backend

- **Java 25** / Spring Boot 4.0
- **Build**: Gradle (Kotlin DSL)
- **ORM**: MyBatis Plus 3.5+
- **Database**: MySQL 8.0 (PostgreSQL supported)
- **Connection Pool**: Druid
- **Cache**: Redis 7 + Redisson
- **Auth**: Sa-Token (JWT)
- **API Docs**: Knife4j (Swagger 3.0)
- **Pagination**: PageHelper

### Frontend

- **Vue 3.5** + Vite 6
- **Element Plus** UI
- **Pinia** (State Management)
- **TypeScript**
- **Axios** (HTTP Client)

## Module Overview

### xarch-core-spring-boot-starter

Core utilities and annotations used across all modules.

**Annotations:**

- `@XarchLog` - Operation logging
- `@Debounce` - Anti-duplicate submission
- `@NotZero` - Parameter validation

**Utilities:**

- `IdUtil` - ID generation
- `JsonUtil` - JSON serialization
- `ResultUtil` - Response building

**Entities:**

- `PageQuery` - Pagination query
- `BaseUserInfo` - User context
- `LoginUser` - Session user

### xarch-db-spring-boot-starter

Database access layer with MyBatis Plus integration.

**Features:**

- Auto-configuration of DataSource (Druid)
- MyBatis Plus mapper base class
- PageHelper pagination support
- Multi-database support (MySQL, PostgreSQL)

**Configuration:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  database-platform: org.hibernate.dialect.MySQL8Dialect
```

### xarch-web-spring-boot-starter

REST API layer with authentication and documentation.

**Features:**

- Knife4j API documentation at `/doc.html`
- Sa-Token authentication
- CORS configuration
- Global exception handler
- AOP operation logging
- Captcha generation

**Endpoints:**

- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `GET /auth/captcha` - Get captcha

### xarch-cache-spring-boot-starter

Redis caching layer with Redisson distributed locks.

**Features:**

- Auto-configuration of RedisTemplate
- Redisson distributed locks
- Cache key prefix support
- TTL configuration

## Quick Start

### Backend

```bash
cd backend
./gradlew build -x test
cd xarch-example
./gradlew bootRun

# API Docs: http://localhost:8080/doc.html
# Default port: 8080
```

### Frontend

```bash
cd vue3-admin
pnpm install
pnpm dev

# Access: http://localhost:3000
```

### Docker

```bash
# Start all services
docker-compose up -d

# Access points:
# - Frontend: http://localhost
# - Backend API: http://localhost/api
# - API Docs: http://localhost:8080/doc.html
```

## Starter Usage

Add starters to your Spring Boot project's `build.gradle`:

```kotlin
dependencies {
    implementation("com.xarch:xarch-db-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-web-spring-boot-starter:1.0.0")
    implementation("com.xarch:xarch-cache-spring-boot-starter:1.0.0")
}
```

## Example Application (xarch-example)

The example module demonstrates all framework features with 14 controllers:

### Core Controllers

| Controller | Endpoint | Description |
|------------|----------|-------------|
| `UserController` | `/user/*` | User management (CRUD, role assignment) |
| `RoleController` | `/role/*` | Role management (permissions) |
| `MenuController` | `/menu/*` | Menu management (tree structure) |
| `DeptController` | `/dept/*` | Department management (tree structure) |

### System Controllers

| Controller | Endpoint | Description |
|------------|----------|-------------|
| `DictController` | `/dict/*` | Dictionary management |
| `ConfigController` | `/config/*` | System configuration |
| `LoginLogController` | `/loginLog/*` | Login logs |
| `OpLogController` | `/opLog/*` | Operation logs |

### Business Controllers

| Controller | Endpoint | Description |
|------------|----------|-------------|
| `CaptchaController` | `/captcha/*` | Captcha generation |
| `ClientController` | `/client/*` | Client management |
| `MessageController` | `/message/*` | Message center |
| `ResourceController` | `/resource/*` | Resource management |
| `TempFileController` | `/tempFile/*` | Temporary file handling |
| `CommonController` | `/common/*` | Common operations |

## API Response Format

All APIs return `ApiResult<T>`:

```json
{
  "code": "0000",
  "msg": "success",
  "data": { ... },
  "timestamp": 1716038400000
}
```

### Response Codes

| Code | Description |
|------|-------------|
| `0000` | Success |
| `1001` | Parameter error |
| `1002` | Business error |
| `1003` | Auth error |
| `1004` | Not found |
| `1005` | System error |

## Configuration Reference

### Application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: xarch-example

  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123

  redis:
    host: localhost
    port: 6379
    password:
    database: 0

# Sa-Token Configuration
sa-token:
  token-name: Authorization
  timeout: 7200
  activity-timeout: -1

# MyBatis Plus
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.xarch.example.entity

# Knife4j
knife4j:
  enable: true
  production: false
```

## Database Schema

```sql
-- Core tables
CREATE TABLE sys_user (...);
CREATE TABLE sys_role (...);
CREATE TABLE sys_menu (...);
CREATE TABLE sys_dept (...);

-- Dictionary & Config
CREATE TABLE sys_dict (...);
CREATE TABLE sys_dict_data (...);
CREATE TABLE sys_config (...);

-- Logging
CREATE TABLE log_login (...);
CREATE TABLE log_op (...);

-- Business
CREATE TABLE biz_client (...);
CREATE TABLE biz_message (...);
CREATE TABLE biz_resource (...);
CREATE TABLE biz_temp_file (...);
```

## Unit Tests

All 14 controllers have corresponding unit tests:

| Test Class | Coverage |
|------------|----------|
| `UserControllerTest` | User CRUD operations |
| `RoleControllerTest` | Role management |
| `MenuControllerTest` | Menu tree operations |
| `DeptControllerTest` | Department tree |
| `DictControllerTest` | Dictionary operations |
| `ConfigControllerTest` | Config management |
| `LoginLogControllerTest` | Login logs |
| `OpLogControllerTest` | Operation logs |
| `CaptchaControllerTest` | Captcha endpoints |
| `ClientControllerTest` | Client CRUD |
| `MessageControllerTest` | Message center |
| `ResourceControllerTest` | Resource management |
| `TempFileControllerTest` | File operations |
| `CommonControllerTest` | Common utilities |

Run tests:

```bash
cd backend
./gradlew test
```

## Development Guidelines

### Package Naming

- Starters: `com.xarch.starter.*`
- Example: `com.xarch.example.*`

### Layer Structure

```
controller/  - REST endpoints
service/     - Business logic
mapper/      - Data access
entity/      - Domain models
```

### Naming Conventions

- Controllers: `XxxController`
- Services: `XxxService`
- Mappers: `XxxMapper`
- Entities: `Xxx`

## License

MIT License