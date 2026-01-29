# 12common-care 开发计划 Phase 1（轮播图 + 前台首页接入 + 管理端权限闸门）（2026-01-29）

目标：在不改变 base-admin 代码风格与基础架构的前提下，优先落地你已确认的口径：
- 公告仅登录可见（复用 sys_notification ANNOUNCEMENT）
- 轮播图后台可管理（biz_banner）
- 众筹用户可发布/管理员审核（后续 Phase）
- 统计占比图为“公益活动报名量占比”（后续 Phase）

已确认实现细节（2026-01-29）：
- 轮播图图片：复用文件上传（`/api/files/upload?visibility=PUBLIC`），业务表仅存 `imageFileId`
- /admin 闸门：仅允许具备 `admin` 角色的用户访问

范围声明：本 Phase 只做「轮播图」模块闭环 + 前台首页展示，并补齐 /admin 路由的最小权限闸门；不提前实现众筹/活动等其它模块。

## 任务清单
- [x] 后端：轮播图（banners）
- [x] 后端：权限与种子数据
- [x] 前端（后台）：轮播图管理页 + 菜单
- [x] 前端（前台）：首页展示轮播图 + 公告（仅登录可见）
- [x] 前端：/admin 路由最小权限闸门
- [x] 测试与验证：mvn test / npm run build

## 1) 后端：轮播图（banners）
- [x] 数据库：新增表 `biz_banner`
- [x] Entity/Mapper：遵循 MyBatis-Plus 风格
- [x] Service：list/create/update/delete
- [x] Controller：REST（/api/banners + /api/banners/public）
- [x] 权限：
  - [x] 列表：`banner:list`
  - [x] 写操作：`banner:manage`

## 2) 后端：权限与种子数据
- [x] `.kiro/specs/admin-base/schema.sql` 增加 biz_banner
- [x] `.kiro/specs/admin-base/seed.sql` 增加权限 + admin 角色授权
- [x] `backend/src/test/resources/schema.sql` + `data.sql` 同步（保证测试通过）

## 3) 前端（后台）：轮播图管理
- [x] 路由：`/admin/banners`
- [x] 菜单：AdminLayout 增加“轮播图”入口（受 v-permission 控制）
- [x] 页面：表格 + 新增/编辑弹窗（Element Plus）

## 4) 前端（前台）：首页展示
- [x] 替换当前“前台占位页面”为真实首页（FrontendLayout 下）
- [x] 展示轮播图（从 /api/banners/public 拉取）
- [x] 公告：仅当已登录时展示（从 /api/notifications 拉取并过滤 ANNOUNCEMENT）

## 5) 前端：/admin 路由最小权限闸门
- [x] 路由守卫：访问 /admin/** 必须登录且具备 admin 角色
- [x] 未满足时：跳转到 / （或提示无权限）

## 6) 验证
- [x] 后端：`cd backend && mvn test`
- [x] 前端：`cd frontend && npm run build`

完成标准：
- 管理员可在后台维护轮播图；普通用户在首页可看到轮播图；公告仅登录可见；/admin 路由不会被普通用户直接打开。
