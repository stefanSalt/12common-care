# 12common-care 功能重新设计（功能-页面-权限-数据实体对照）（2026-01-29）

本文件目的：在 base-admin（Spring Boot 3 + Vue 3）现有架构上，按“最小修改原则”落地你提供的功能清单，并补齐后台/前台路由与权限点。

## 1) 角色与权限模型（建议）
- 角色：`admin`（管理员，已存在）、`user`（普通用户，已存在）
- 鉴权：登录态使用 JWT（已存在）；前端路由使用 `requiresAuth`
- 授权：后台接口用 `@RequiresPermission("xxx")`；前端菜单/按钮用 `v-permission`
- 建议补齐：访问 `/admin/**` 时额外校验 `admin` 角色或关键 permission（避免普通用户误入后台）

## 2) 管理员端模块（菜单/路由/权限/数据）
说明：以下为新增业务模块；基础系统模块（用户/角色/权限/公告/留言/文件/个人信息）沿用现有实现。

| 模块 | 路由(前端) | 权限code(建议) | 后端接口(建议) | 主要表(建议) |
|---|---|---|---|---|
| 仪表盘（统计 + 公告入口） | `/admin` | `stats:view` | `GET /api/stats/*` | （聚合查询） |
| 轮播图管理 | `/admin/banners` | `banner:list` `banner:manage` | `GET/POST/PUT/DELETE /api/banners` | `biz_banner` |
| 众筹项目（审核/管理） | `/admin/crowdfunds` | `crowdfund:list` `crowdfund:audit` | `GET/POST/PUT/DELETE /api/crowdfunds` + 审核 | `biz_crowdfund_project` |
| 众筹捐款（管理） | `/admin/crowdfund-donations` | `crowdfundDonation:list` | `GET /api/crowdfund-donations` | `biz_crowdfund_donation` |
| 公益活动（管理） | `/admin/activities` | `activity:list` `activity:manage` | `GET/POST/PUT/DELETE /api/activities` | `biz_activity` |
| 活动捐赠（管理） | `/admin/activity-donations` | `activityDonation:list` | `GET /api/activity-donations` | `biz_activity_donation` |
| 爱心事迹（文章管理） | `/admin/stories` | `story:list` `story:manage` | `GET/POST/PUT/DELETE /api/stories` | `biz_story` |
| 评论信息（管理） | `/admin/comments` | `comment:list` `comment:manage` | `GET/PUT /api/comments` | `biz_comment` |
| 活动收藏（管理） | `/admin/activity-favorites` | `favorite:list` | `GET /api/activity-favorites` | `biz_activity_favorite` |
| 管理员信息 | `/admin/admin-users` | `user:list` `user:update` | 复用 `/api/users`（建议支持按角色筛选） | `sys_user` 等 |
| 用户信息 | `/admin/users` | `user:list` `user:update` | 复用 `/api/users` | `sys_user` 等 |

## 3) 普通用户端模块（路由/登录要求/数据）
说明：前台统一用 `FrontendLayout`；“我的* / 个人中心”类页面 `requiresAuth`。

| 模块 | 路由(前端) | 是否登录 | 后端接口(建议) | 主要表(建议) |
|---|---|---|---|---|
| 登录/注册 | `/login` `/register` | 否 | 复用 `/api/auth/*` | `sys_user` |
| 个人信息/改密 | `/profile` | 是 | 复用 `/api/auth/me*` | `sys_user` |
| 首页（轮播图/最新众筹/公告） | `/` | 否（公告需登录） | `GET /api/banners/public` `GET /api/crowdfunds` `GET /api/notifications`（仅登录可见，前端过滤 type=ANNOUNCEMENT） | `biz_banner` 等 |
| 爱心众筹：列表/详情 | `/crowdfunds` `/crowdfunds/:id` | 否 | `GET /api/crowdfunds*` | `biz_crowdfund_project` |
| 爱心众筹：捐款 | `/crowdfunds/:id` | 是 | `POST /api/crowdfunds/{id}/donate` | `biz_crowdfund_donation` |
| 爱心众筹：发布项目（与后台审核联动） | `/crowdfunds/new` `/my/crowdfunds` | 是 | `POST/GET /api/crowdfunds` | `biz_crowdfund_project` |
| 公益活动：列表/详情 | `/activities` `/activities/:id` | 否 | `GET /api/activities*` | `biz_activity` |
| 公益活动：捐赠/报名/签到/收藏 | `/activities/:id` `/my/activity-*` | 是 | `POST /api/activities/{id}/donate|signup|check-in` `POST/DELETE /api/activity-favorites` | `biz_activity_signup` `biz_activity_donation` `biz_activity_favorite` |
| 爱心事迹：列表/详情/评论 | `/stories` `/stories/:id` | 评论需登录 | `GET /api/stories*` `POST /api/comments` | `biz_story` `biz_comment` |
| 留言板 | `/messages` | 是 | 复用 `/api/messages*` | `sys_message` |
| 我的捐款记录（众筹） | `/my/crowdfund-donations` | 是 | `GET /api/crowdfund-donations?mine=1` | `biz_crowdfund_donation` |
| 我的报名 | `/my/activity-signups` | 是 | `GET /api/activity-signups?mine=1` | `biz_activity_signup` |
| 我的评论 | `/my/comments` | 是 | `GET /api/comments?mine=1` | `biz_comment` |
| 我的收藏（活动） | `/my/activity-favorites` | 是 | `GET /api/activity-favorites?mine=1` | `biz_activity_favorite` |

## 4) 数据实体（建议字段摘要）
- `biz_crowdfund_project`：发起用户、目标金额、已筹金额、审核状态/原因、封面图（fileId）
- `biz_crowdfund_donation`：项目、捐款用户、金额、时间
- `biz_activity`：活动信息（时间/地点/状态）、募捐目标/已募金额（如需要）、封面图（fileId）
- `biz_activity_signup`：活动报名（报名/取消/签到状态 + 时间）
- `biz_activity_donation`：活动捐赠记录
- `biz_activity_favorite`：活动收藏记录
- `biz_story`：爱心事迹文章（标题/内容/封面/状态）
- `biz_comment`：评论（目标类型+目标ID+内容+状态）
- `biz_banner`：轮播图（图片 fileId、跳转链接、排序、启用状态）

注：图片统一复用 `sys_file`（建议 PUBLIC），业务表仅存 `file_id`。

## 5) 数据统计（ECharts）口径（建议）
- 近 7 天“收入”折线：`sum(众筹捐款.amount + 活动捐赠.amount)` 按 `created_at` 天维度聚合
- “占比图”：近 7 天（或近 30 天）活动报名量占比（按 activity_id 聚合 count，展示 TopN + 其他）

## 6) 已确认口径（2026-01-29）
1) 系统公告：1A 仅登录可见（复用 `sys_notification` 的 ANNOUNCEMENT）
2) 轮播图：2B 后台可管理（`biz_banner` + 管理页面）
3) 众筹项目：3A 普通用户可发布/管理我的项目，管理员审核
4) 统计占比图：4B 公益活动报名量占比
