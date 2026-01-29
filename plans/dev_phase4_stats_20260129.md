# 12common-care 开发计划 Phase 4（数据统计模块）（2026-01-29）

目标：在后台新增“数据统计”页面，展示近 N 天收入折线图 + 占比图；权限统一为 `stats:view`；保持现有代码风格与最小改动。

范围（最小闭环）：
- 后端：新增收入趋势接口 `GET /api/stats/income-trend?days=7`
- 前端：新增后台页面 `/admin/stats`，展示：
  - 折线图：近 N 天收入（按天聚合）
  - 饼图：近 N 天活动报名量占比（复用现有 `GET /api/stats/activity-signup-ratio`）
- 不做：导出、按项目/活动筛选、多维指标大屏等（避免范围失控）

## 已确认（2026-01-29）
1A) 收入口径：众筹捐款 + 活动捐赠 合计
2A) 过滤规则：不过滤，按 donation.created_at 全量统计
3A) 占比图对象：公益活动报名量占比（近 N 天）
4A) 页面入口：后台新增菜单“数据统计” `/admin/stats`
5A) 默认统计天数：7 天

## 任务清单（执行时逐项勾选）
- [x] 后端：新增 DTO：`IncomeTrendDto { date, amount }`
- [x] 后端：扩展 `StatsService` / `DbStatsService` / `StatsController`：`GET /api/stats/income-trend?days=7`（`stats:view`）
- [x] 后端：新增集成测试（构造众筹/活动捐赠数据；断言 income-trend 返回含 0 填充日期）
- [x] 前端：扩展 `frontend/src/api/stats.ts`：新增 `getIncomeTrend(days)`
- [x] 前端：新增 `frontend/src/views/admin/StatsView.vue`（ECharts 折线 + 饼图）
- [x] 前端：路由 + 菜单接入（权限 `stats:view`）
- [x] 验证：`cd backend && mvn test`；`cd frontend && npm run build`
- [ ] 提交推送：`feat: 新增数据统计模块`（需要再次授权 git 写命令）

约束提醒：
- 未再次授权前，不执行任何 git 写命令（add/commit/push）。
- 提交信息统一中文（如 `feat: ...`）。
