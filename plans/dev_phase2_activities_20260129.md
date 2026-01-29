# 12common-care 开发计划 Phase 2（公益活动模块）（2026-01-29）

目标：在现有 Spring Boot 3 + Vue 3 架构上，实现“公益活动”全链路（管理员管理 + 用户浏览/报名/签到/捐赠/收藏 + 管理端统计）。

范围：仅公益活动域；不实现众筹/事迹/评论等其它模块（避免范围失控）。

## 待确认问题（开始编码前必须确认；请用“1A 2B ... 开始执行”回复）
1) 活动类型/能力开关：
   - A. 所有活动都支持报名 + 捐赠
   - B. 活动支持开关：signupEnabled / donateEnabled（推荐）

2) 报名人数上限：
   - A. 无上限（最小）
   - B. 有上限 maxParticipants（推荐，便于控制）

3) 签到规则：
   - A. 报名后随时可签到（最小）
   - B. 仅在 startTime~endTime 时间窗可签到（推荐）

4) 取消报名：
   - A. 活动开始前可取消（推荐）
   - B. 不允许取消

5) 捐赠次数：
   - A. 同一活动可多次捐赠（推荐）
   - B. 只能捐一次

6) 统计占比口径（活动报名量占比）：
   - A. 全量历史报名数占比
   - B. 近 7 天报名数占比（推荐）
   - C. 近 30 天报名数占比

7) 活动封面图：
   - A. 必须上传 PUBLIC 图片（复用 sys_file，推荐）
   - B. 可填外链 URL（不推荐）

8) 活动详情内容：
   - A. textarea 文本（最小，推荐）
   - B. 富文本（本期不做）

已确认（2026-01-29）：
- 1B：signupEnabled / donateEnabled
- 2B：maxParticipants
- 3B：仅在 startTime~endTime 时间窗可签到
- 4A：活动开始前可取消报名
- 5A：同一活动可多次捐赠
- 6B：近 7 天报名数占比
- 7A：封面必须为 PUBLIC 图片（复用 sys_file）
- 8B：活动详情为富文本

补充确认（富文本实现细节；开始编码前必须确认；请用“9A 10A 11A 开始执行”回复）：
9) 富文本编辑器选择：
   - A. 引入 wangEditor（Vue3）
   - B. 引入 Quill（Vue3）
   - C. 先用“HTML 源码 textarea”临时方案（最小，但不够友好，不推荐）

10) 富文本存储格式：
   - A. 存 HTML（推荐）
   - B. 存编辑器 JSON（如 delta），同时保留一份 HTML（更复杂）

11) 富文本内嵌图片：
   - A. 本期不支持（推荐，只做封面图；避免范围失控）
   - B. 支持（需要补齐 editor 图片上传对接与安全策略）

已确认（2026-01-29）：
- 9A：富文本编辑器选 wangEditor（Vue3）
- 10A：富文本存储格式存 HTML
- 11B：富文本内嵌图片本期支持（对接上传并插入图片 URL）

## 任务清单
- [x] 后端：活动与报名/签到/捐赠/收藏 API + 权限
- [x] 后端：活动统计 API（报名量占比）
- [x] 前端（后台）：活动管理 + 报名/捐赠记录管理 + 统计图
- [x] 前端（前台）：活动列表/详情 + 报名/取消/签到 + 捐赠 + 收藏 + 我的相关
- [x] 测试与验证：mvn test / npm run build

## 1) 数据表（建议字段，不写死；以“最小可用”为准）
- `biz_activity`：title、cover_file_id(PUBLIC)、content、address、start_time、end_time、signup_enabled、donate_enabled、max_participants、donation_target、donated_amount、enabled、deleted、created_at、updated_at
- `biz_activity_signup`：activity_id、user_id、status(SIGNED/CANCELED/CHECKED_IN)、signed_at、canceled_at、checked_in_at
- `biz_activity_donation`：activity_id、user_id、amount、remark(可选)、created_at
- `biz_activity_favorite`：activity_id、user_id、created_at（唯一约束 activity_id+user_id）

## 2) 权限 code（建议）
- 活动：`activity:list` `activity:manage`
- 报名记录：`activitySignup:list`
- 捐赠记录：`activityDonation:list`
- 收藏记录：`activityFavorite:list`
- 统计：`stats:view`

## 3) 后端接口（建议）
- 前台公开：`GET /api/activities/public`、`GET /api/activities/{id}/public`
- 前台登录：报名/取消/签到/捐赠/收藏 + 我的列表
- 后台管理：活动 CRUD、报名/捐赠/收藏列表、统计接口

## 4) 实施顺序（建议）
- [ ] 先做表结构 + 种子权限（确保 admin 有权限进入页面）
- [ ] 后端：活动 public 列表/详情（前台先可看）
- [ ] 后端：报名/取消/签到/收藏/捐赠（含必要幂等与校验）
- [ ] 后端：管理端列表接口（活动/报名/捐赠/收藏）
- [ ] 前端：后台活动管理页 + 记录列表页
- [ ] 前端：前台活动列表/详情 + 我的报名/捐赠/收藏
- [ ] 统计：报名量占比 API + 管理端 ECharts 饼图

## 5) 验证
- [ ] 后端新增集成测试覆盖：public 列表、报名/取消/签到、捐赠、收藏、统计接口权限
- [ ] `cd backend && mvn test`
- [ ] `cd frontend && npm run build`

完成标准：
- admin 可管理活动并查看报名/捐赠/收藏记录；用户可浏览活动、报名/取消/签到、捐赠、收藏并在“我的”中查看记录；统计饼图可展示报名量占比。
