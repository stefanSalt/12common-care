# admin-base 设计与落地计划（2026-01-24）

## 目标

- 基于 `.kiro/specs/admin-base/requirements.md` 的需求，完成可实施的技术设计与落地计划。
- 保持最小复杂度：本地文件存储 + MySQL，权限注解控制，WebSocket 通知。

## 已确认的关键决策（来自需求澄清）

- 认证：Access Token 有效期 1 天；Refresh Token 有效期 30 天；刷新下发新的 Access+Refresh；旧 Refresh Token 不撤销（仍可刷新）；登出不做服务端处理。
- 文件：File_Visibility=PUBLIC/PRIVATE，默认 PRIVATE；PRIVATE 仅上传者可访问/删除；PUBLIC 匿名可访问。
- 权限：Permission 采用后台维护字典（CRUD），角色分配权限；接口可用 `@RequiresPermission` 注解控制；未标注接口仅需登录即可访问（有风险但按需求执行）。
- 后端：使用 Lombok 简化样板代码（DTO/实体等），减少样板代码。

## 交付物

- [x] 对齐后的设计文档：`.kiro/specs/admin-base/design.md`（保证与 requirements 一致）
- [x] 接口级别的请求/响应 DTO 规范（示例 + 错误码）：`.kiro/specs/admin-base/api-spec.md`
- [x] 数据库表结构 DDL（MySQL 8.0）：`.kiro/specs/admin-base/schema.sql`
- [x] 最小可运行的后端/前端工程骨架（`backend/` + `frontend/`）

## 待确认问题（影响实现方式）

- [x] Refresh_Token 的实现形式：JWT（无状态、无需落库）。
- [x] PUBLIC 文件的分享链接形态：继续使用 `/api/files/{id}/download`（后端动态放行）。
- [x] WebSocket 鉴权：`?token=...`。

## 任务清单（设计 → 实施）

- [x] 需求-设计一致性检查：补齐 design 中与 requirements 不一致的点（Token/文件匿名访问/权限默认策略等）
- [x] API 设计细化：为每个 endpoint 定义 request/response、分页格式、错误码
- [x] 安全设计：SecurityConfig 白名单、JWT 校验、权限切面策略、匿名下载动态校验
- [x] 数据模型落地：表结构、索引、软删除策略、唯一性约束策略
- [x] 文件模块：存储路径生成规则、元数据、下载鉴权分支（PUBLIC/PRIVATE）
- [x] 通知模块：WebSocket 连接鉴权、离线存储、公告广播
- [x] 前端设计：Pinia 权限状态、路由守卫、`v-permission` 指令、401 自动刷新流程
- [x] 可验证性：根据 design 的“正确性属性”补齐最小测试矩阵（后续落地阶段执行）
