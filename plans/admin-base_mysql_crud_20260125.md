# admin-base MySQL + 用户/角色/权限 CRUD（2026-01-25）

## 目标

- 接入 MySQL 8.0（Spring Boot 3 + MyBatis-Plus），落地 sys_* 表的 CRUD。
- 替换当前后端 in-memory 用户/权限实现，使登录/鉴权/权限注解在 DB 模式下可用。
- 保持最小复杂度：不引入 Flyway/Liquibase（默认用现有 schema.sql + seed.sql 手工导入）。

## 关键实现点（与 requirements 对齐）

- 用户密码：BCrypt 存储（不可逆哈希）。
- 权限校验：`@RequiresPermission` → 403；未标注接口：仅需登录（按需求）。
- JWT：Access 1 天 / Refresh 30 天；刷新下发新的 Access+Refresh；旧 Refresh 仍可用；登出客户端清理。

## 待确认（你回复后我再开始执行）

- [x] MySQL 连接信息：host=172.25.48.1 port=3307 db=admin_base（默认，可通过 env 覆盖） user=root（密码用 env 提供，避免提交明文）。
- [x] 需要提供 `seed.sql`：初始化 `admin/admin123` + admin 角色 + 全量权限字典。
- [x] 接受测试用 H2（scope=test）以保证 `mvn test` 不依赖本机 MySQL。

## 任务清单（TDD 驱动）

- [x] DB 配置：启用 datasource（使用环境变量占位，避免提交明文密码），并配置 MyBatis-Plus（分页需额外引入 `mybatis-plus-jsqlparser`）
- [x] 数据模型：Entity + Mapper（sys_user/sys_role/sys_permission/sys_user_role/sys_role_permission）
- [x] Service：User/Role/Permission 领域服务（含分配角色/分配权限）
- [x] Controller：按 `.kiro/specs/admin-base/api-spec.md` 增补 Users/Roles/Permissions 接口
- [x] Auth：login/me 从 DB 读取 user/roles/permissions；refresh 保持现有规则
- [x] PermissionService：从角色→权限表查询权限集合，供 `PermissionAspect` 使用
- [x] 可验证性：新增最小集成测试（覆盖登录 + 权限拒绝 + CRUD 基本路径）
- [x] 文档：补充本地启动说明（如何初始化 schema/seed，如何配置 env）
- [x] Git：提交并推送到 GitHub `main`
