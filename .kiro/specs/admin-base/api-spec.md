# API 规格（admin-base）

## 1. 通用约定

### 1.1 Base URL

- HTTP API: `/api`
- WebSocket: `/ws/notification`

### 1.2 认证

- HTTP 请求使用 Access Token：`Authorization: Bearer <access_token>`
- WebSocket 连接使用 Query 参数：`/ws/notification?token=<access_token>`

### 1.3 统一响应与状态码

- 成功：HTTP 200 + `{"code":0,"message":"success","data":...}`
- 业务错误：HTTP 200 + `code!=0`
- 参数校验失败：HTTP 400
- 未认证：HTTP 401
- 权限不足：HTTP 403
- 服务端异常：HTTP 500
- 为避免 JS number 精度丢失，后端会将所有 `Long` 类型的 ID 字段序列化为 **字符串**（例如 `"2015350336044470273"`）。

`Result<T>` / `PageResult<T>` 结构见 `.kiro/specs/admin-base/design.md`。

## 2. Token 规则

### 2.1 Claim 约定

两类 Token 均为 JWT（签名算法与密钥由后端配置决定），并使用 claim `token_type` 区分用途：

- Access Token（用于访问受保护接口）
  - `token_type=access`
  - `exp`: 1 天
  - 必含：`userId`、`username`、`roles`（字符串数组）
- Refresh Token（用于刷新 Access Token）
  - `token_type=refresh`
  - `exp`: 30 天
  - 必含：`userId`

### 2.2 刷新策略

- `/api/auth/refresh` 入参为 `refreshToken`
- 成功返回新的 `token` 与新的 `refreshToken`
- 旧 `refreshToken` 不撤销，仍可在有效期内继续刷新（服务端不维护 Refresh_Token 撤销列表）
- 服务端不处理登出；登出由客户端清理本地状态完成

## 3. DTO 定义（摘要）

### 3.1 Auth

#### LoginRequest

```json
{ "username": "stefan", "password": "******" }
```

#### LoginResponseData

```json
{
  "token": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "user": {
    "id": "1",
    "avatarFileId": "10001",
    "username": "stefan",
    "nickname": "Stefan",
    "email": "stefan@example.com",
    "phone": "13800000000",
    "status": 1,
    "roles": [{ "id": "10", "code": "admin", "name": "管理员" }],
    "permissions": ["user:list", "user:create"]
  }
}
```

#### RefreshRequest / RefreshResponseData

```json
{ "refreshToken": "eyJhbGciOi..." }
```

```json
{ "token": "eyJhbGciOi...", "refreshToken": "eyJhbGciOi..." }
```

### 3.2 分页

分页查询统一使用 query 参数：
- `current`: 当前页（从 1 开始）
- `size`: 每页大小

响应 `data` 为 `PageResult<T>`，字段为 `records/total/current/size`。

## 4. 接口定义（HTTP）

### 4.1 认证接口

#### POST /api/auth/login（公开）

- Request：`LoginRequest`
- Response：`Result<LoginResponseData>`
- Errors：
  - 1001 用户不存在
  - 1003 密码错误

#### POST /api/auth/refresh（公开）

- Request：`RefreshRequest`
- Response：`Result<RefreshResponseData>`
- Errors：
  - 401 Refresh_Token 过期/无效

#### GET /api/auth/me（登录）

- Response：`Result<LoginResponseData.user>`（仅返回 `user` 结构）

#### PUT /api/auth/me（登录）

- 用途：更新自己的个人信息（不需要额外权限，仅需登录）。
- Request：

```json
{ "nickname": "Stefan", "email": "stefan@example.com", "phone": "13800000000" }
```

- Response：`Result<LoginResponseData.user>`

#### POST /api/auth/me/avatar（登录）

- 用途：上传头像（复用文件模块，头像文件默认 **PUBLIC**）。
- Content-Type：`multipart/form-data`
- Form fields：
  - `file`: 头像文件
- Response：`Result<LoginResponseData.user>`（其中 `avatarFileId` 指向 `sys_file.id`，头像可通过 `/api/files/{id}/download` 访问）

### 4.2 用户管理（需要权限）

#### GET /api/users（权限：user:list）

- Query：`current`、`size`
- Response：`Result<PageResult<User>>`

#### GET /api/users/{id}（权限：user:list）

- Response：`Result<User>`

#### POST /api/users（权限：user:create）

```json
{
  "username": "alice",
  "password": "******",
  "nickname": "Alice",
  "email": "alice@example.com",
  "phone": "13900000000",
  "status": 1
}
```

- Response：`Result<User>`
- Errors：
  - 1002 用户名已存在

#### PUT /api/users/{id}（权限：user:update）

```json
{
  "nickname": "Alice-2",
  "email": "alice2@example.com",
  "phone": "13900000001",
  "status": 1
}
```

- Response：`Result<User>`

#### DELETE /api/users/{id}（权限：user:delete）

- Response：`Result<Void>`

#### PUT /api/users/{id}/roles（权限：user:update）

```json
{ "roleIds": [10, 11] }
```

- Response：`Result<Void>`

### 4.3 角色管理（需要权限）

#### GET /api/roles（权限：role:list）

- Response：`Result<Role[]>`

#### GET /api/roles/{id}（权限：role:list）

- Response：`Result<Role>`（包含 `permissions` 字段）

#### POST /api/roles（权限：role:create）

```json
{ "code": "admin", "name": "管理员", "description": "系统管理员" }
```

- Response：`Result<Role>`
- Errors：
  - 1005 角色编码已存在

#### PUT /api/roles/{id}（权限：role:update）

```json
{ "name": "管理员", "description": "系统管理员" }
```

- Response：`Result<Role>`

#### DELETE /api/roles/{id}（权限：role:delete）

- Response：`Result<Void>`

#### PUT /api/roles/{id}/permissions（权限：role:update）

```json
{ "permissionIds": [100, 101] }
```

- Response：`Result<Void>`

### 4.4 权限管理（需要权限）

#### GET /api/permissions（权限：permission:list）

- Response：`Result<Permission[]>`

#### POST /api/permissions（权限：permission:create）

```json
{ "code": "user:list", "name": "用户列表", "description": "查看用户列表" }
```

- Response：`Result<Permission>`
- Errors：
  - 1007 权限标识已存在

#### PUT /api/permissions/{id}（权限：permission:update）

```json
{ "name": "用户列表", "description": "查看用户列表" }
```

- Response：`Result<Permission>`

#### DELETE /api/permissions/{id}（权限：permission:delete）

- Response：`Result<Void>`

### 4.5 文件管理（登录 / 动态权限）

#### POST /api/files/upload（登录）

- Content-Type：`multipart/form-data`
- Form fields：
  - `file`: 文件
  - `visibility`: `PUBLIC` / `PRIVATE`（可选，默认 `PRIVATE`）

- Response：`Result<FileInfo>`

#### GET /api/files（登录）

- Query：`current`、`size`
- Response：`Result<PageResult<FileInfo>>`（仅返回当前用户上传的文件）

#### GET /api/files/{id}/download（动态）

- PUBLIC：允许匿名下载
- PRIVATE：需要登录且必须为上传者

#### DELETE /api/files/{id}（登录且为上传者）

- Response：`Result<Void>`
- Errors：
  - 1010 无权访问该文件

### 4.6 通知（登录 / 权限）

#### GET /api/notifications（登录）

- Query：`current`、`size`
- Response：`Result<PageResult<Notification>>`

#### PUT /api/notifications/{id}/read（登录）

- Response：`Result<Void>`

#### POST /api/notifications/announce（权限：notification:announce）

```json
{ "title": "系统公告", "content": "今晚 23:00 维护" }
```

- Response：`Result<Void>`
