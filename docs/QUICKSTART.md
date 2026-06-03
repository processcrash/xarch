# xarch Developer Quick Start Guide

## Prerequisites

Before starting, ensure you have the following installed:

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 25+ (or 17+) | Java runtime |
| Node.js | 20+ | Frontend build |
| pnpm | 8+ | Frontend package manager |
| Docker | 24+ | Container runtime |
| Docker Compose | 2.20+ | Container orchestration |
| MySQL | 8.0 | Database |
| Redis | 7 | Cache |

---

## Project Structure Overview

```
xarch/
├── backend/                    # Spring Boot 4.0 + Spring Cloud
│   ├── xarch-spring-boot-starter/     # Starter 模块
│   │   ├── xarch-core-spring-boot-starter/
│   │   ├── xarch-db-spring-boot-starter/
│   │   ├── xarch-web-spring-boot-starter/
│   │   ├── xarch-cache-spring-boot-starter/
│   │   └── xarch-mcp/
│   ├── xarch-spring-cloud/      # Spring Cloud 模块
│   │   └── xarch-cloud/
│   │       ├── xarch-cloud-starter-nacos/
│   │       ├── xarch-cloud-starter-gateway/
│   │       └── xarch-cloud-starter-mcp/
│   └── xarch-example/          # 示例应用
├── vue3-admin/                 # Vue 3 前端
├── mcp-servers/                 # MCP Servers (Node.js/Python)
├── k8s/                        # Kubernetes 配置
└── docs/                       # 文档
```

---

## Backend Development Setup

### 1. Initialize Database

```bash
# Start MySQL and Redis via Docker
docker run -d --name xarch-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=xarch \
  mysql:8.0

docker run -d --name xarch-redis \
  -p 6379:6379 \
  redis:7-alpine

# Import schema
mysql -h localhost -u root -proot123 xarch < docs/db/init-mysql.sql
```

### 2. Build Backend

```bash
cd backend

# Build all modules (skip tests for faster build)
./gradlew build -x test

# Or build specific module
./gradlew :xarch-example:build -x test
```

### 3. Run Backend

```bash
cd backend/xarch-example

# Run with Gradle
../gradlew bootRun

# Or run JAR directly
java -jar build/libs/xarch-example-1.0.0.jar
```

**Backend will start on:** http://localhost:8080

### 4. Verify Backend

```bash
# Health check
curl http://localhost:8080/actuator/health

# API docs (Knife4j)
curl http://localhost:8080/doc.html
```

---

## Frontend Development Setup

### 1. Install Dependencies

```bash
cd vue3-admin
pnpm install
```

### 2. Configure API Base URL

Edit [src/utils/http.ts](vue3-admin/src/utils/http.ts):

```typescript
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
```

### 3. Run Frontend

```bash
pnpm dev
```

**Frontend will start on:** http://localhost:3000

### 4. Default Login

- Username: `admin`
- Password: `admin123`

---

## Docker Compose Development

For a quick full-stack setup:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down
```

**Service URLs:**

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080 |
| API Docs | http://localhost:8080/doc.html |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

---

## Module Development Guide

### Creating a New Starter Module

1. Create module directory:
```bash
mkdir backend/xarch-spring-boot-starter/xarch-my-starter
```

2. Add build.gradle:
```groovy
plugins {
    id 'java-library'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    // Add your dependencies
}
```

3. Create starter class:
```java
// src/main/java/com/xarch/starter/my/MyAutoConfiguration.java
@Configuration
public class MyAutoConfiguration {
    // Your auto-configuration
}
```

4. Create spring.factories:
```properties
# src/main/resources/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.xarch.starter.my.MyAutoConfiguration
```

### Adding a New Controller

1. Create controller in xarch-example:
```java
// src/main/java/com/xarch/example/controller/XxxController.java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @GetMapping
    public ApiResult<List<Xxx>> list() {
        return ApiResult.success(xxxService.list());
    }
}
```

2. Create service interface:
```java
// src/main/java/com/xarch/example/service/XxxService.java
public interface IXxxService {
    List<Xxx> list();
}
```

3. Create service implementation:
```java
// src/main/java/com/xarch/example/service/impl/XxxServiceImpl.java
@Service
public class XxxServiceImpl implements IXxxService {
    @Autowired
    private XxxMapper xxxMapper;

    @Override
    public List<Xxx> list() {
        return xxxMapper.selectList(null);
    }
}
```

4. Create mapper:
```java
// src/main/java/com/xarch/example/mapper/XxxMapper.java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {
}
```

### Adding a New Entity

```java
// src/main/java/com/xarch/example/entity/Xxx.java
@Data
@Table("sys_xxx")
public class Xxx {
    @Id(auto = true)
    private Long id;

    private String name;
    private Integer status;
}
```

### Adding CRUD API with MyBatisFlex

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @Autowired
    private XxxService xxxService;

    @GetMapping("/page")
    public ApiResult<PageResult<Xxx>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Xxx> page = new Page<>(pageNum, pageSize);
        Page<Xxx> result = xxxService.page(page);
        return ApiResult.success(new PageResult<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("/{id}")
    public ApiResult<Xxx> get(@PathVariable Long id) {
        return ApiResult.success(xxxService.getById(id));
    }

    @PostMapping
    public ApiResult<Void> create(@RequestBody Xxx xxx) {
        xxxService.save(xxx);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Xxx xxx) {
        xxx.setId(id);
        xxxService.updateById(xxx);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        xxxService.removeById(id);
        return ApiResult.success();
    }
}
```

---

## Frontend Development Guide

### Creating a New Page

1. Create API module:
```typescript
// src/api/xxx.ts
import http from '@/utils/http'

export function getXxxPage(params: any) {
  return http.get('/api/xxx/page', { params })
}

export function getXxx(id: number) {
  return http.get(`/api/xxx/${id}`)
}

export function createXxx(data: any) {
  return http.post('/api/xxx', data)
}

export function updateXxx(id: number, data: any) {
  return http.put(`/api/xxx/${id}`, data)
}

export function deleteXxx(id: number) {
  return http.delete(`/api/xxx/${id}`)
}
```

2. Create list view:
```vue
<!-- src/views/xxx/XxxList.vue -->
<template>
  <div class="xxx-list">
    <!-- Search bar -->
    <el-form inline :model="queryParams">
      <el-form-item label="Name">
        <el-input v-model="queryParams.name" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">Search</el-button>
        <el-button @click="handleReset">Reset</el-button>
      </el-form-item>
    </el-form>

    <!-- Table -->
    <el-table :data="dataList">
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="status" label="Status">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <pagination
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getXxxPage, deleteXxx } from '@/api/xxx'

const queryParams = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})
const dataList = ref([])
const total = ref(0)

function handleQuery() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.name = ''
  handleQuery()
}

async function loadData() {
  const res = await getXxxPage(queryParams)
  dataList.value = res.data.list
  total.value = res.data.total
}

loadData()
</script>
```

### Adding Menu Entry

Edit [src/router/index.ts](vue3-admin/src/router/index.ts):

```typescript
{
  path: '/xxx',
  component: Layout,
  children: [
    {
      path: 'list',
      name: 'XxxList',
      component: () => import('@/views/xxx/XxxList.vue'),
      meta: { title: 'XXX Management' }
    }
  ]
}
```

---

## Testing Guide

### Backend Unit Tests

```bash
cd backend

# Run all tests
./gradlew test

# Run specific test class
./gradlew :xarch-example:test --tests "com.xarch.example.controller.UserControllerTest"

# Run with coverage
./gradlew test jacocoTestReport
```

### Frontend Tests

```bash
cd vue3-admin

# Run unit tests
pnpm test

# Run e2e tests (if configured)
pnpm test:e2e
```

### Integration Tests with Testcontainers

```bash
cd backend/xarch-example

# Run integration tests (requires Docker)
./gradlew integrationTest
```

---

## Debugging

### Backend Debugging

**IntelliJ IDEA:**
1. Set breakpoint in controller/service
2. Run in debug mode: `../gradlew bootRun --debug`
3. Connect debugger on port 5005

**VSCode:**
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug xarch-example",
      "request": "launch",
      "mainClass": "com.xarch.example.XarchExampleApplication",
      "projectName": "xarch-example"
    }
  ]
}
```

### Frontend Debugging

```bash
# Run with verbose logging
pnpm dev --verbose

# Inspect network requests
# Open Chrome DevTools > Network tab > filter by "localhost:8080"
```

---

## Common Issues

### Port Already in Use

```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process
taskkill /PID <pid> /F
```

### Database Connection Failed

1. Check MySQL is running:
```bash
docker ps | grep mysql
```

2. Verify credentials in application.yml:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xarch
    username: root
    password: root123
```

### Frontend Build Errors

```bash
# Clear node_modules and reinstall
rm -rf node_modules pnpm-lock.yaml
pnpm install

# Clear vite cache
rm -rf node_modules/.vite
```

### Gradle Build Errors

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Stop Gradle daemon
./gradlew --stop
```

---

## Code Style

### Backend (Java)

- Use 4 spaces for indentation
- Follow Spring Boot conventions
- Use meaningful method names
- Add Javadoc for public APIs

### Frontend (Vue/TypeScript)

- Use Composition API with `<script setup lang="ts">`
- Follow Vue 3 style guide
- Use TypeScript for type safety
- Prefer arrow functions

---

## Resources

| Resource | URL |
|----------|-----|
| Project Docs | [docs/](docs/) |
| API Documentation | http://localhost:8080/doc.html |
| Git Repository | https://github.com/processcrash/xarch |
| Vue 3 Docs | https://vuejs.org/ |
| Element Plus | https://element-plus.org/ |
| Spring Boot | https://spring.io/projects/spring-boot/ |
| MyBatis Plus | https://baomidou.com/ |
