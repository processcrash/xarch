# xarch - Enterprise Backend Development Framework

## Project Structure

```
xarch/
├── backend/                 # Spring Boot Backend
│   ├── xarch-bom/          # Bill of Materials
│   ├── xarch-common-core/  # Common core module
│   ├── xarch-db-spring-boot-starter/       # DB starter (MySQL/PostgreSQL)
│   ├── xarch-web-spring-boot-starter/      # Web starter (REST API)
│   ├── xarch-cache-spring-boot-starter/    # Cache starter (Redis)
│   └── xarch-example/      # Example application
│
├── vue3-admin/             # Vue 3 Frontend
│   ├── src/
│   │   ├── api/           # API calls
│   │   ├── views/          # Pages
│   │   ├── router/         # Routes
│   │   ├── stores/         # Pinia stores
│   │   └── utils/          # Utilities
│   ├── nginx/              # Nginx config
│   └── Dockerfile
│
├── docker-compose.yml      # Docker orchestration
└── init.sql               # Database initialization
```

## Tech Stack

### Backend
- Java 25, Spring Boot 4.0
- MyBatis 3.0, PageHelper
- MySQL 8.0, PostgreSQL (supported)
- Redis 7, Redisson
- Knife4j (Swagger 3.0 API Docs)

### Frontend
- Vue 3.5, Vite 6
- Element Plus
- Pinia (State Management)
- TypeScript

## Quick Start

### Backend

```bash
cd backend
mvn clean install -DskipTests
cd xarch-example
mvn spring-boot:run
# API Docs: http://localhost:8080/doc.html
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

Add to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>com.xarch</groupId>
    <artifactId>xarch-db-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>com.xarch</groupId>
    <artifactId>xarch-web-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>com.xarch</groupId>
    <artifactId>xarch-cache-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then configure in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123
  redis:
    host: localhost
    port: 6379
```

## License

MIT