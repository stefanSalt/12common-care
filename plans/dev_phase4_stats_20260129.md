# 12common-care 开发计划 Phase 4（数据统计模块）（2026-01-29）

目标：在后台新增“数据统计”页面，展示近 N 天收入折线图 + 占比图；权限统一为 `stats:view`；保持现有代码风格与最小改动。

范围（最小闭环）：
- 后端：新增收入趋势接口 `GET /api/stats/income-trend?days=7`
- 前端：新增后台页面 `/admin/stats`，展示：
  - 折线图：近 N 天收入（按天聚合）
  - 饼图：近 N 天活动报名量占比（复用现有 `GET /api/stats/activity-signup-ratio`）
- 不做：导出、按项目/活动筛选、多维指标大屏等（避免范围失控）

## 待确认问题（开始编码前必须确认；请用“1A 2A 3A 4A 5A 开始执行”回复）
1) 收入口径（折线图）：
   - A. 众筹捐款 + 活动捐赠 合计（推荐）
   - B. 仅众筹捐款
   - C. 仅活动捐赠

2) 收入是否需要过滤“禁用/删除/未审核”相关数据：
   - A. 不过滤，按 donation.created_at 全量统计（推荐；当前捐赠表无支付状态字段）
   - B. 需要过滤（请说明过滤规则）

3) 占比图对象：
   - A. 公益活动报名量占比（近 N 天）（推荐；现有接口已支持）
   - B. 众筹项目捐款金额占比（近 N 天）（需新增接口）
   - C. 众筹项目捐款次数占比（近 N 天）（需新增接口）

4) 统计页面入口：
   - A. 后台新增菜单“数据统计” `/admin/stats`（推荐）
   - B. 合并到后台首页

5) 默认统计天数：
   - A. 7 天（推荐）
   - B. 30 天

## 任务清单（执行时逐项勾选）
- [ ] 后端：新增 DTO：`IncomeTrendDto { date, amount }`
- [ ] 后端：扩展 `StatsService` / `DbStatsService` / `StatsController`：`GET /api/stats/income-trend?days=7`（`stats:view`）
- [ ] 后端：新增集成测试（构造众筹/活动捐赠数据；断言 income-trend 返回含 0 填充日期）
- [ ] 前端：扩展 `frontend/src/api/stats.ts`：新增 `getIncomeTrend(days)`
- [ ] 前端：新增 `frontend/src/views/admin/StatsView.vue`（ECharts 折线 + 饼图）
- [ ] 前端：路由 + 菜单接入（权限 `stats:view`）
- [ ] 验证：`cd backend && mvn test`；`cd frontend && npm run build`
- [ ] 提交推送：`feat: 新增数据统计模块`（需要再次授权 git 写命令）

约束提醒：
- 未再次授权前，不执行任何 git 写命令（add/commit/push）。
- 提交信息统一中文（如 `feat: ...`）。

