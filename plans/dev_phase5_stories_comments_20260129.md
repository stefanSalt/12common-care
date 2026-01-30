# 12common-care 开发计划 Phase 5（爱心事迹 + 评论）（2026-01-29）

目标：在现有 Spring Boot 3 + Vue 3 基座上，实现“爱心事迹文章 + 评论”最小闭环：
- 管理员：文章管理、评论管理
- 普通用户：文章列表/详情、发表评论、我的评论

约束：
- 业务价值 > 技术细节；最小修改；保持现有代码风格（RBAC 权限、DTO/Service/Controller 分层、MyBatis-Plus）。
- 未再次授权前，不执行任何 git 写命令（add/commit/push）。
- 提交信息统一中文（如 `feat: ...`）。

## 已确认（2026-01-29）
1A) 文章编辑器：复用 wangEditor（富文本存 HTML，支持插图上传）
2A) 文章状态：仅 enabled 启用/禁用（前台仅展示 enabled=1）
3A) 评论审核：不需要审核（发布即展示；管理员可管理）
4A) 评论展示：评论列表公开可见；发表评论需要登录
5A) 我的评论：仅支持删除自己的评论
6A) 评论形式：仅一级评论（不做回复/楼中楼）

## 权限 code（建议）
- 文章：`story:list` `story:manage`
- 评论：`comment:list` `comment:manage`

## 任务清单（执行时逐项勾选）
- [x] MySQL：更新 `.kiro/specs/admin-base/schema.sql`（新增 `biz_story` / `biz_comment`）
- [x] MySQL：更新 `.kiro/specs/admin-base/seed.sql`（新增权限 + 角色授权）
- [x] H2(test)：更新 `backend/src/test/resources/schema.sql`、`backend/src/test/resources/data.sql`
- [x] 后端：Story 模块（实体/DTO/Service/Controller）
  - public：`GET /api/stories/public`、`GET /api/stories/{id}/public`
  - admin：`GET/POST/PUT/DELETE /api/stories`（`story:list` / `story:manage`）
- [x] 后端：Comment 模块（实体/DTO/Service/Controller）
  - public：`GET /api/comments/public?targetType=STORY&targetId=...`
  - user：`POST /api/comments`、`GET /api/comments/my`、`DELETE /api/comments/{id}`（我的评论）
  - admin：`GET /api/comments`（list）、`DELETE /api/comments/{id}/manage`（manage）
- [x] 测试：新增 `StoryCommentModuleTest` 覆盖文章 public、评论发布/列表、权限与“我的评论”
- [x] 前端（前台）：`/stories` 列表、`/stories/:id` 详情 + 评论区、`/my/comments` 我的评论
- [x] 前端（后台）：`/admin/stories` 文章管理、`/admin/comments` 评论管理（菜单按权限显示）
- [x] 验证：`cd backend && mvn test`；`cd frontend && npm run build`
- [ ] 提交推送：`feat: 新增爱心事迹与评论模块`（需要再次授权 git 写命令）
