# xarch - Enterprise Backend Development Framework

## Tech Stack

- **Backend**: Java 25, Spring Boot 4.0, MyBatis 3.0, PageHelper
- **Database**: MySQL 8.0, PostgreSQL (supported)
- **Cache**: Redis 7, Redisson
- **Frontend**: Vue 3.5, Vite 6, Element Plus, Pinia
- **API Documentation**: Knife4j (Swagger 3.0)

## Project Structure

```
xarch/
├── xarch-bom/                      # Bill of Materials
├── xarch-common-core/              # Common core module
├── xarch-db-spring-boot-starter/   # Database starter (MySQL/PostgreSQL)
├── xarch-web-spring-boot-starter/  # Web starter (REST API)
├── xarch-cache-spring-boot-starter/ # Cache starter (Redis)
├── xarch-example/                  # Example application
└── frontend/                       # Vue 3 frontend
    ├── src/
    │   ├── api/                    # API calls
    │   ├── views/                  # Pages
    │   ├── router/                 # Routes
    │   ├── stores/                 # Pinia stores
    │   └── utils/                  # Utilities
    └── nginx/                      # Nginx config
```

## Quick Start

### Backend

```bash
cd xarch-example
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

### Docker

```bash
docker-compose up -d
```

## Starter Usage

Add dependencies to your Spring Boot project:

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

## API Documentation

Access Knife4j at: `http://localhost:8080/doc.html`

## License

MIT