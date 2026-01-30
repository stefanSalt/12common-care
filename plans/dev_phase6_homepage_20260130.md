# 12common-care 开发计划 Phase 6（后台/前台首页完善）（2026-01-30）

目标：在不改动既有业务逻辑的前提下，完善 **前台首页** 与 **后台首页** 的信息架构与模块入口：
- 前台首页：在“轮播图/公告/最新众筹”基础上，补齐“最新爱心事迹”入口，并梳理模块入口
- 后台首页：替换当前示例页，提供常用模块快捷入口 + 公告概览（可跳转到通知/数据统计等页面）

范围（最小闭环）：
- 仅做首页 UI/信息展示与路由跳转；不新增复杂统计口径、不做大屏
- 数据来源复用既有接口：banners/public、notifications、crowdfunding/public、stories/public

约束：
- 最小修改原则；保持现有 Vue3 + ElementPlus + ECharts 代码风格
- 未再次授权前，不执行任何 git 写命令（add/commit/push）
- 提交信息统一中文（如 `feat: ...`）

## 已确认（2026-01-30）
1B) 前台首页：不新增快捷入口（仅增加“最新爱心事迹”）
2A) 前台首页：“最新爱心事迹”展示数量 6 条
3C) 后台首页：不展示快捷入口（仅展示内容概览）
4B) 后台首页：嵌入图表（收入折线 + 报名占比，复用现有 /admin/stats 逻辑）

## 任务清单（执行时逐项勾选）
- [x] 前台首页：`frontend/src/views/home/FrontHomeView.vue`
  - [x] 新增“最新爱心事迹”（调用 `GET /api/stories/public?current=1&size=6`）
- [x] 后台首页：`frontend/src/views/home/HomeView.vue`
  - [x] 替换示例内容为“公告概览 + 图表概览”（无快捷入口）
- [x] 路由/菜单（如需）：保持现有结构，不新增不必要页面
- [x] 验证：`cd frontend && npm run build`
- [ ] 提交推送：`feat: 完善前后台首页`（需要再次授权 git 写命令）
