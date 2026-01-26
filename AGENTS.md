# base-admin 项目说明（给 AI / 贡献者）

> 目标：让新的 AI/开发者快速理解该仓库的组织结构、技术栈、权限模型与编码风格，便于在不破坏现有一致性的前提下继续扩展。

## 1. 仓库结构

- `backend/`：后端（Spring Boot）
- `frontend/`：前端（Vue）
- `.kiro/specs/admin-base/`：需求/设计/接口/DDL/种子数据（权威来源）
- `plans/`：过程性计划/决策记录（历史参考）
- `.github/workflows/ci.yml`：CI（后端测试 + 前端构建）

## 2. 技术栈与版本

### 2.1 后端
- Java 17
- Spring Boot 3.2.1
- Spring Security（JWT 无状态）
- MyBatis-Plus 3.5.16
- MySQL 8（开发/生产），H2（测试，MySQL MODE）
- JJWT 0.12.6（HS256）
- SpringDoc OpenAPI 2.3.0（Swagger UI）
- Lombok（provided）

### 2.2 前端
- Node.js 22（CI 与本机验证）
- Vue 3.5.24 + Vue Router 4.6.4 + Pinia 3.0.4
- Element Plus 2.13.1 + @element-plus/icons-vue
- Axios 1.13.2
- Vite 7.2.4 + TypeScript 5.9.3

## 3. 运行与开发

### 3.1 数据库初始化（MySQL 8）
- 表结构：`.kiro/specs/admin-base/schema.sql`
- 种子数据：`.kiro/specs/admin-base/seed.sql`
- 默认账号：`admin / admin123`

### 3.2 后端配置（backend）
- 配置：`backend/src/main/resources/application.yml`
- 通过环境变量注入 MySQL 连接（避免提交明文密码）：
  - `MYSQL_HOST`（默认 `172.25.48.1`）
  - `MYSQL_PORT`（默认 `3307`）
  - `MYSQL_DB`（默认 `admin_base`）
  - `MYSQL_USER`（默认 `root`）
  - `MYSQL_PASSWORD`（无默认值）
- JWT：
  - `app.jwt.secret` 必须至少 32 字节（HS256）
  - Access Token 默认 1 天；Refresh Token 默认 30 天（可用 env 覆盖）
- 文件存储：`app.file.storage-path` 默认 `./storage`（即 `backend/storage/`）

启动命令：
```bash
cd backend
mvn test
mvn spring-boot:run
```
访问入口：
- Swagger UI：`/swagger-ui.html`
- 健康检查：`/api/health`
- WebSocket：`/ws/notification?token=<access_token>`

### 3.3 前端开发（frontend）
- Vite 代理：`frontend/vite.config.ts`
  - `/api` → `http://localhost:8080`
  - `/ws` → `ws://localhost:8080`

启动命令：
```bash
cd frontend
npm install
npm run dev
```

构建命令：
```bash
npm run build
```

### 3.4 CI
- 配置：`.github/workflows/ci.yml`
- push/PR 到 `main` 会自动执行：后端 `mvn test`、前端 `npm run build`

## 4. API 统一约定

### 4.1 响应结构

后端所有 JSON 响应统一封装为：

```json
{ "code": 0, "message": "success", "data": {} }
```

- `code = 0` 代表成功
- 鉴权失败：HTTP 401，且 `code = 401`
- 权限不足：HTTP 403，且 `code = 403`
- 参数校验失败：HTTP 400，且 `code = 400`（message 为校验提示拼接）

### 4.2 Long ID 序列化

为避免 JS number 精度丢失，后端会将 `long/Long` 序列化为字符串（见 `backend/src/main/java/com/example/admin/config/JacksonConfig.java`）。
因此前端/接口约定：
- 所有 `id` 类型按 `string` 处理（不要用 `number`）

## 5. 认证与权限模型（核心）

### 5.1 Token 约定
- Access Token：JWT（默认 1 天）
- Refresh Token：JWT（默认 30 天）
- `/api/auth/refresh` 会下发新的 Access + Refresh
- 服务端不维护 Token 撤销列表：旧 Refresh Token 在有效期内仍可刷新
- 登出不需要后端接口：前端清理本地状态即可

JWT claims（关键字段）：
- `token_type`: `access` / `refresh`
- `userId` / `username` / `roles`

### 5.2 后端鉴权（Spring Security + AOP）
- 安全链配置：`backend/src/main/java/com/example/admin/config/SecurityConfig.java`
  - 公开接口：`/api/auth/login`、`/api/auth/register`、`/api/auth/refresh`、`/api/health`、Swagger 相关
  - WebSocket：`/ws/**` 允许访问，但握手拦截器会校验 query 参数 `token`
  - 文件下载：`GET /api/files/*/download` 允许匿名访问；PRIVATE 文件由业务层校验“登录且上传者”
  - 其余接口默认需要登录
- 权限注解：`@RequiresPermission("module:action")`
  - AOP：`PermissionAspect` 会按 `userId → roles → permissions` 从 DB 查询权限，缺失则 403

### 5.3 前端权限（Pinia + 指令 + 菜单）
- 登录/注册接口会返回 `user.permissions`（字符串数组）
- 状态存储：`frontend/src/stores/user.ts`
- 指令：`frontend/src/directives/permission.ts`（`v-permission` 无权限会移除 DOM）
- 后台菜单：`frontend/src/layouts/AdminLayout.vue` 根据权限过滤显示

常见问题排查（菜单不显示）：
1) 确认当前账号是否具备对应权限（例如 “留言管理”需要 `message:list`）
2) 退出登录再登录一次（刷新本地缓存的 `user`）
3) MySQL 是否导入最新 `.kiro/specs/admin-base/seed.sql`，或是否已将权限绑定到 admin 角色

## 6. 数据库模型概览

权威 DDL：`.kiro/specs/admin-base/schema.sql`（MySQL 8）。

- `sys_user`：用户（`deleted` 软删除；`avatar_file_id` 头像文件）
- `sys_role`：角色
- `sys_permission`：权限字典（权限码格式 `module:action`）
- `sys_user_role`：用户-角色关联（覆盖式分配）
- `sys_role_permission`：角色-权限关联（覆盖式分配）
- `sys_file`：文件（`visibility = PUBLIC/PRIVATE`；PUBLIC 允许匿名下载）
- `sys_notification`：通知（`type = SYSTEM/BUSINESS/ANNOUNCEMENT`；WebSocket 推送 + 未读初始化）
- `sys_message`：留言（title/content/contact_email/status/reply_content/replied_at）

约定：
- ID 由应用生成（MyBatis-Plus `IdType.ASSIGN_ID`），不使用自增
- 软删除字段统一为 `deleted`（0 正常 / 1 删除）

## 7. 代码组织与风格

### 7.1 后端分层（`backend/src/main/java/com/example/admin`）
- `controller/`：仅做参数校验 + 权限注解 + 调用 service，返回 `Result<T>`
- `service/`：业务逻辑；需要事务时使用 `@Transactional`
- `mapper/`：MyBatis-Plus `BaseMapper`
- `entity/`：与表字段对应（`SysXxx`），使用 Lombok `@Data`
- `dto/`：API 入参/出参对象（不要直接暴露 entity）
- `security/`：JWT 解析过滤器 + 权限注解/AOP
- `websocket/`：通知 WebSocket（query token 握手）
- `exception/`：`BusinessException` + `GlobalExceptionHandler`
- `common/`：`Result` / `PageResult`

编码约定：
- Controller/Service 使用构造器注入（避免字段注入）
- 入参校验使用 `jakarta.validation`（`@Valid` + `@NotBlank` 等）
- 业务错误抛 `BusinessException(code, message)`；统一由异常处理器转为 `Result.fail`

### 7.2 前端分层（`frontend/src`）
- `api/`：按领域拆分（auth/user/role/permission/file/notification/message）
- `utils/request.ts`：Axios 实例；遇到 401 会自动调用 `/api/auth/refresh` 并重试原请求
- `utils/notificationWs.ts`：通知 WebSocket 客户端（token 存在即连接；断线自动重连）
- `layouts/`：`AdminLayout.vue` / `FrontendLayout.vue`
- `router/index.ts`：路由 meta（`public` / `requiresAuth` / `permission`）
- `views/`：页面组件（`<script setup lang="ts">`）

路由约定：
- 前台入口：`/`（FrontendLayout）
- 后台入口：`/admin`（AdminLayout）
- 登录成功后：admin 默认跳转 `/admin`，普通用户跳转 `/`

UI/文本约定：
- 本项目不考虑国际化：页面可见文本尽量中文
- 表单提交需提供最基本校验（必填/格式）

提交约定：
- 提交信息使用中文（例如 `feat: 新增留言管理`）
- 优先小步提交，避免一次改动过大

## 8. 扩展新模块的最小流程（建议 AI 严格遵守）

1) 先对齐文档（权威来源：`.kiro/specs/admin-base/`）：
- `requirements.md` / `design.md` / `api-spec.md`
- `schema.sql` / `seed.sql`
- 同步更新测试用的 `backend/src/test/resources/schema.sql` / `data.sql`

2) 后端实现：
- 新增 entity/mapper/service/controller
- 需要后台权限的接口加 `@RequiresPermission`
- 写/改 MockMvc 测试，确保 `mvn test` 通过

3) 前端实现：
- `src/api` 增加对应 API 封装
- 新增页面与路由
- 若是后台菜单：在 `AdminLayout.vue` 增加菜单项，并用 `permission` 控制显示

4) 验证闭环：
- backend：`mvn test`
- frontend：`npm run build`
- CI：推送/PR 会自动跑（见 `.github/workflows/ci.yml`）

## 9. 部署提示（最小）
- 后端：`mvn -DskipTests package` 生成 jar（用环境变量配置 MySQL/JWT/存储路径）
- 前端：`npm run build` 产物在 `frontend/dist/`；建议通过反向代理将：
  - `/api` 与 `/ws` 转发到后端
  - 其他路径返回前端静态文件

## 10. 关键参考文档
- 需求：`.kiro/specs/admin-base/requirements.md`
- 设计：`.kiro/specs/admin-base/design.md`
- API：`.kiro/specs/admin-base/api-spec.md`
- DDL：`.kiro/specs/admin-base/schema.sql`
- Seed：`.kiro/specs/admin-base/seed.sql`
