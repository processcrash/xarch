# xarch API Documentation

## Base URL

```
Production: http://api.xarch.com
Development: http://localhost:8080
```

## Authentication

All API endpoints (except `/api/auth/*`) require authentication via Bearer token.

```
Authorization: Bearer <token>
```

## Response Format

All responses follow this structure:

```json
{
  "code": "0000",
  "msg": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

### Response Codes

| Code | Description |
|------|-------------|
| 0000 | Success |
| 1001 | Parameter error |
| 1002 | Business exception |
| 1003 | Authentication failed |
| 1004 | Resource not found |
| 1005 | System error |

---

## System Management APIs

### User Management

#### Get User List
```
GET /api/users
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| username | string | No | Username filter |
| status | string | No | 1=Active, 0=Disabled |
| pageNum | int | No | Page number (default: 1) |
| pageSize | int | No | Page size (default: 10) |

**Response:**
```json
{
  "code": "0000",
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "Administrator",
        "email": "admin@example.com",
        "mobile": "15888888888",
        "status": 1,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 100
  }
}
```

#### Get User Detail
```
GET /api/users/{id}
```

#### Create User
```
POST /api/users
```

**Request Body:**
```json
{
  "username": "newuser",
  "password": "password123",
  "nickname": "New User",
  "email": "newuser@example.com",
  "mobile": "15888888889",
  "status": 1
}
```

#### Update User
```
PUT /api/users/{id}
```

#### Delete User
```
DELETE /api/users/{id}
```

#### Assign Roles to User
```
PUT /api/users/{id}/roles
```

**Request Body:**
```json
{
  "roleIds": [1, 2, 3]
}
```

---

### Role Management

#### Get Role List
```
GET /api/roles
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| roleName | string | No | Role name filter |
| pageNum | int | No | Page number |
| pageSize | int | No | Page size |

#### Get Role Detail
```
GET /api/roles/{id}
```

#### Create Role
```
POST /api/roles
```

**Request Body:**
```json
{
  "roleName": "Admin",
  "roleKey": "ADMIN",
  "roleType": 1,
  "status": 1,
  "remark": "Administrator role"
}
```

#### Update Role
```
PUT /api/roles/{id}
```

#### Delete Role
```
DELETE /api/roles/{id}
```

#### Assign Permissions to Role
```
PUT /api/roles/{id}/permissions
```

**Request Body:**
```json
{
  "menuIds": [1, 2, 3, 4, 5]
}
```

---

### Menu Management

#### Get Menu Tree
```
GET /api/menus/treeselect
```

#### Get Role's Menus
```
GET /api/menus/role/{roleId}
```

#### Create Menu
```
POST /api/menus
```

**Request Body:**
```json
{
  "menuName": "User Management",
  "parentId": 0,
  "orderNum": 1,
  "path": "user",
  "component": "system/user/index",
  "menuType": "C",
  "visible": "0",
  "status": "0",
  "perms": "system:user:list"
}
```

---

### Department Management

#### Get Department Tree
```
GET /api/depts
```

#### Get Department Detail
```
GET /api/depts/{id}
```

#### Create Department
```
POST /api/depts
```

**Request Body:**
```json
{
  "deptName": "Technology",
  "parentId": 0,
  "orderNum": 1,
  "leader": "Tech Lead",
  "phone": "15888888888",
  "email": "tech@example.com"
}
```

---

## Authentication APIs

### Login
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "code": "0000",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresTime": 86400000
  }
}
```

### Logout
```
POST /api/auth/logout
```

### Get Current User
```
GET /api/auth/current
```

### Get Captcha
```
GET /api/auth/captcha
```

---

## AI Server Management APIs

### Server Management

#### Get Server List
```
GET /ai/server/page
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| keyword | string | No | Search keyword |
| serverGroup | string | No | Server group filter |
| status | int | No | 0=Disconnected, 1=Connected, 2=Error |
| pageNum | int | No | Page number |
| pageSize | int | No | Page size |

#### Get Server Detail
```
GET /ai/server/{id}
```

#### Create Server
```
POST /ai/server
```

**Request Body:**
```json
{
  "name": "Production Server",
  "host": "192.168.1.100",
  "port": 22,
  "username": "root",
  "authType": "password",
  "password": "password123",
  "serverGroup": "production",
  "osType": "Ubuntu"
}
```

#### Update Server
```
PUT /ai/server
```

#### Delete Server
```
DELETE /ai/server/{id}
```

#### Test Connection
```
POST /ai/server/{id}/test
```

#### Connect Server
```
POST /ai/server/{id}/connect
```

#### Disconnect Server
```
POST /ai/server/{id}/disconnect
```

---

### Command Execution

#### Execute Command
```
POST /ai/server/command
```

**Request Body:**
```json
{
  "serverId": 1,
  "command": "ls -la",
  "sessionId": "session-123"
}
```

**Response:**
```json
{
  "code": "0000",
  "data": {
    "id": 1,
    "serverId": 1,
    "command": "ls -la",
    "output": "total 64\ndrwxr-xr-x  2 root root 4096 Jan 1 00:00 ...",
    "exitCode": 0,
    "duration": 120,
    "status": 1
  }
}
```

#### Execute AI Command
```
POST /ai/server/command/ai
```

**Request Body:**
```json
{
  "serverId": 1,
  "naturalLanguage": "show system information",
  "sessionId": "session-123"
}
```

---

### AI Command Generation

#### Generate Command
```
POST /ai/server/ai/generate
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| serverId | long | Yes | Server ID |
| naturalLanguage | string | Yes | Natural language description |

**Response:**
```json
{
  "code": "0000",
  "data": {
    "command": "uname -a && cat /etc/os-release",
    "category": "System Information",
    "confidence": 0.95,
    "safetyLevel": "LOW"
  }
}
```

#### Validate Command
```
POST /ai/server/ai/validate
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| command | string | Yes | Command to validate |

**Response:**
```json
{
  "code": "0000",
  "data": {
    "isSafe": true,
    "message": "Read-only command",
    "safetyLevel": "LOW"
  }
}
```

#### Get Command Templates
```
GET /ai/server/ai/templates
```

---

### Command Audit

#### Get Audit Logs
```
GET /ai/audit/page
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| serverId | long | No | Server ID filter |
| userId | long | No | User ID filter |
| riskLevel | int | No | Risk level filter |
| approvalStatus | int | No | Approval status filter |
| startTime | datetime | No | Start time |
| endTime | datetime | No | End time |
| pageNum | int | No | Page number |
| pageSize | int | No | Page size |

#### Approve Command
```
POST /ai/audit/{id}/approve
```

**Request Body:**
```json
{
  "comment": "Approved for execution"
}
```

#### Reject Command
```
POST /ai/audit/{id}/reject
```

**Request Body:**
```json
{
  "reason": "Command too risky"
}
```

#### Get Compliance Stats
```
GET /ai/audit/stats
```

---

## File Management APIs

### Upload File
```
POST /resource/upload
```

**Request Body:** multipart/form-data
| Name | Type | Required | Description |
|------|------|----------|-------------|
| file | file | Yes | File to upload |
| sceneCode | string | No | Scene code |
| storageType | string | No | Storage type (local/minio/aliyun_oss) |

### Get File List
```
GET /resource/page
```

### Download File
```
GET /resource/download/{id}
```

### Delete File
```
DELETE /resource/{id}
```

---

## Monitoring APIs

### Get Server Monitor Info
```
GET /monitor/server/info
```

### Get Cache Monitor Info
```
GET /monitor/cache/info
```

### Get Online Users
```
GET /monitor/online/list
```

### Force Logout
```
DELETE /monitor/online/{sessionId}
```

---

## Actuator Endpoints

### Health Check
```
GET /actuator/health
```

### Metrics
```
GET /actuator/metrics
```

### Prometheus Metrics
```
GET /actuator/prometheus
```

---

## WebSocket Endpoints

### SSH Terminal
```
WS /ws/ssh
```

**Message Types:**

1. Create Session:
```json
{
  "type": "create_session",
  "serverId": 1
}
```

2. Send Command:
```json
{
  "type": "command",
  "command": "ls -la"
}
```

3. Resize Terminal:
```json
{
  "type": "resize",
  "cols": 80,
  "rows": 24
}
```

4. Receive Output:
```json
{
  "type": "output",
  "command": "ls -la",
  "exitCode": 0,
  "output": "...",
  "duration": 120
}
```
