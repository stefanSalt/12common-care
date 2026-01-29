# 12common-care 开发计划 Phase 3（爱心众筹模块）（2026-01-29）

目标：在现有 Spring Boot 3 + Vue 3 基座上，实现“爱心众筹”全链路（用户发布/浏览/捐款 + 管理员审核/管理 + 前台首页展示最新众筹）。

范围：仅“众筹项目 + 众筹捐款 + 首页最新众筹展示”；不实现事迹/评论/大盘统计等其它模块（避免范围失控）。

## 已确认（2026-01-29）
你已选择 Phase3 模块：**1 众筹项目 + 众筹捐款**

规则确认：
1A) 用户发布众筹需要管理员审核：需要（PENDING -> APPROVED/REJECTED）
2A) 众筹状态：仅 PENDING / APPROVED / REJECTED（最小闭环）
3A) 必须截止时间：endTime 必填
4A) 捐款次数：同一众筹可多次捐款
5A) 众筹详情：富文本 + 插图（复用 wangEditor + PUBLIC 上传），存 HTML
6B) 捐款记录展示：在项目详情公开展示

补充确认（2026-01-29，开始执行）：
7A) 封面图与富文本插图：必须为 PUBLIC 文件（复用 sys_file）
8A) targetAmount：必填（用于进度可视化）
9A) 超过 endTime：自动视为已结束，禁止继续捐款
10A) 用户可编辑：PENDING/REJECTED 可编辑并再次提交审核
11A) 公开捐款记录：展示“捐款人昵称/用户名 + 金额 + 时间”，允许匿名

## 任务清单（执行时逐项勾选）
- [x] 数据表 + 种子权限（MySQL schema/seed + H2 test schema/data）
- [ ] 后端：众筹项目 CRUD + 审核（管理员）（已实现：管理员列表/审核；待补：删除/禁用等管理项）
- [x] 后端：众筹公开列表/详情（未登录可访问）
- [x] 后端：用户发布/编辑/提交审核（按确认规则）
- [x] 后端：捐款接口 + 公开捐款榜单（项目详情公开展示 latestDonations，按 6B/11A）
- [x] 后端：我的捐款（用户端）
- [x] 前端（前台）：众筹列表/详情 + 捐款 + 我的捐款
- [ ] 前端（前台首页）：展示“最新众筹”（与轮播图/公告同页）
- [ ] 前端（后台）：众筹审核/管理 + 捐款记录管理
- [ ] 测试与验证：mvn test / npm run build（已完成：后端 mvn test、前端 npm run build）

## 数据表建议（最小可用）
- `biz_crowdfunding_project`：
  - title、cover_file_id(PUBLIC)、content(HTML)、target_amount、raised_amount、
  - start_time(可选)、end_time(必填)、status(PENDING/APPROVED/REJECTED)、enabled、deleted、
  - created_by、created_at、updated_at
- `biz_crowdfunding_donation`：
  - project_id、user_id、amount、is_anonymous(若 11A)、remark(可选)、created_at

## 权限 code（建议）
- 众筹：`crowdfunding:list` `crowdfunding:manage` `crowdfunding:review`
- 捐款记录：`crowdfundingDonation:list`

## 接口草案（建议）
- 前台公开：
  - `GET /api/crowdfunding/public`（列表）
  - `GET /api/crowdfunding/{id}/public`（详情 + 公开捐款榜单，若 6B）
- 前台登录：
  - `POST /api/crowdfunding`（发布草稿/提交审核，按 10）
  - `PUT /api/crowdfunding/{id}`（编辑，按 10）
  - `POST /api/crowdfunding/{id}/donations`（捐款，按 4A/9）
  - `GET /api/crowdfunding/my/donations`（我的捐款）
- 后台管理：
  - `GET /api/crowdfunding`（管理端列表）
  - `PUT /api/crowdfunding/{id}/review`（approve/reject）
  - `GET /api/crowdfunding/donations`（捐款记录列表）

## 验证
- 后端新增集成测试覆盖：公开列表/详情、发布/审核、捐款（多次）、截止时间拦截、公开捐款榜单、权限校验
- `cd backend && mvn test`
- `cd frontend && npm run build`

约束提醒：
- 未再次授权前，不执行任何 git 写命令（add/commit/push）。
- 后续提交信息统一中文（如 `feat: 新增...`）。
