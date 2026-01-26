# 留言管理（前台留言 / 后台回复）（2026-01-26）

## 目标

- 前台：用户可提交留言。
- 后台：管理员可查看留言列表，并对留言进行回复（反馈）。
- 前台：用户可查看自己留言的处理状态与管理员回复。
- 回复后：给用户发送系统通知（复用现有通知模块 + WebSocket）。

## 约束

- 最小修改：复用现有 Spring Boot 3 + MyBatis-Plus + JWT + 权限注解 + Vue3/Element Plus 工程结构。
- 不做国际化：页面可见文本尽量中文。
- 不引入额外系统（短信/邮件/验证码/第三方 IM），除非你明确要求。
- 后续 git commit/push 需你明确授权后再执行。

## 待确认（开始编码前必须确认；请用“1A 2B 3A ... 开始执行”回复）

1) 前台留言是否需要登录：
- A. 必须登录后才能留言（推荐，最简单）
- B. 允许匿名留言（需要额外字段：联系方式/称呼等；接口需公开）

2) 留言字段范围（最小）：
- A. 仅“留言内容 content”（推荐）
- B. 增加“标题 title”
- C. 增加“联系方式 contact”（用于匿名或管理员回访）

3) 管理员回复形态：
- A. 单次回复（写入 replyContent/repliedAt，后续再次回复覆盖）（推荐）
- B. 多轮对话（留言/回复多条记录）（更复杂，不推荐）

4) 用户侧是否需要查看回复：
- A. 前台提供“我的留言”列表（可看到状态/回复）（推荐）
- B. 不提供列表，仅管理员后台处理（最小）

5) 回复后的通知：
- A. 不做通知（最小）
- B. 回复后给用户发一条系统通知（复用现有通知模块 Notification + WebSocket）（推荐，体验更好）

6) 权限模型（后台）：
- A. 新增权限：`message:list`（查看留言）、`message:reply`（回复留言）（推荐）
- B. 不新增权限，仅限制为管理员角色可访问（需要额外做 role 判断，侵入性略高）

已确认（2026-01-26）：
- 1A：留言必须登录
- 2B+2C：留言包含 title + contact（邮箱）+ content
- 3A：管理员回复为单次回复（覆盖）
- 4A：前台提供“我的留言”列表（查看状态/回复）
- 5B：回复后发送系统通知（复用通知模块 + WebSocket）
- 6A：新增权限 `message:list` / `message:reply`

## 计划（确认后执行）

- [x] 设计/文档对齐
  - [x] `.kiro/specs/admin-base/api-spec.md`：补齐留言相关接口与 DTO 示例
  - [x] `.kiro/specs/admin-base/design.md`：补齐留言模块说明（权限、流程）
  - [x] `.kiro/specs/admin-base/schema.sql`：新增留言表结构
  - [x] `.kiro/specs/admin-base/seed.sql`：补齐 message 权限并绑定 admin 角色

- [x] 后端：留言模块
  - [x] Entity/Mapper：`sys_message`（或 `sys_feedback`）表 + MyBatis-Plus mapper
  - [x] Service：创建留言、分页查询、回复、查询我的留言
  - [x] 回复后通知：复用 `NotificationService.sendToUser(...)` 给留言用户发系统通知
  - [x] Controller：
    - [x] `POST /api/messages`（前台提交，需登录）
    - [x] `GET /api/messages/my`（用户查看自己的留言，需登录）
    - [x] `GET /api/messages`（后台列表，需权限）
    - [x] `PUT /api/messages/{id}/reply`（后台回复，需权限）
  - [x] 校验：参数非空、越权访问、留言不存在等

- [x] 后端：测试
  - [x] `backend/src/test/resources/schema.sql`：新增 `sys_message`（H2）
  - [x] `backend/src/test/resources/data.sql`：若选择 6A，补齐 message 权限/绑定
  - [x] 新增模块测试：前台提交流程 + 后台查看/回复流程 + 权限拒绝

- [x] 前端：前台留言
  - [x] 新增路由与页面（例如 `/message`）
  - [x] 表单：留言内容（按 2）+ 最基本校验
  - [x] “我的留言”列表（按 4A）

- [x] 前端：后台留言管理
  - [x] Admin 菜单增加“留言管理”（按权限显示）
  - [x] 页面：列表分页 + 查看详情 + 回复（弹窗/抽屉）

- [x] 验证闭环
  - [x] 后端：`mvn test` 通过
  - [x] 前端：`npm run build` 通过
  - [x] 手工验收 checklist：前台留言 → 后台回复 → 前台看到回复/收到通知
