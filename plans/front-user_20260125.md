# 前台用户注册与个人中心增强（2026-01-25）

## 目标

- 后端新增用户注册接口，并在注册成功后自动赋予“用户”角色。
- 前端新增注册页面，并打通注册→登录→前台个人中心（个人信息修改、密码修改）。
- 保持最小修改：沿用现有 JWT/Refresh 规则、BCrypt、MyBatis-Plus 结构，不引入验证码/短信/邮箱验证等额外系统。

## 需求摘要（来自你本轮需求）

- 添加用户注册接口和页面。
- 注册成功后自动赋予用户角色。
- 完善前台个人信息修改 / 密码修改等功能。
- 不做国际化：页面文本尽量不要出现英文。

## 已有能力（现状）

- 登录/刷新/我：`/api/auth/login`、`/api/auth/refresh`、`/api/auth/me`
- 个人信息：`PUT /api/auth/me`（昵称/邮箱/手机号）
- 头像：`POST /api/auth/me/avatar`（复用文件模块，头像文件 PUBLIC）
- 前台个人信息页：`/profile`（已存在 ProfileView）

## 待确认问题（开始编码前必须确认；请用“1A 2B 3A ... 开始执行”回复）

1) 注册成功后的行为：
   - A. 仅注册成功，跳转到登录页（最保守）
   - B. 注册成功后自动登录（后端返回 token+refreshToken+user）（推荐，体验更顺）

2) 自动赋予的角色：
   - A. 固定赋予角色 code=`user` name=`普通用户`（推荐；若不存在则在 seed/test 数据中补齐）
   - B. 你指定角色 code/name：________

3) 注册表单字段范围：
   - A. username/password/nickname（推荐，最小）
   - B. 再加 email/phone

4) 密码修改策略：
   - A. 必须提供 oldPassword，校验通过才允许改（推荐）
   - B. 不校验旧密码（风险高，不推荐）

5) 改密后的登录态处理：
   - A. 不处理（JWT 无撤销；已签发 token 继续有效直到过期）（推荐，最小）
   - B. 强制前端退出并重新登录（仅前端行为）

已确认（2026-01-25）：
- 1B：注册成功后自动登录（返回 token + refreshToken + user）
- 2A：默认赋予角色 code=`user` name=`普通用户`
- 3A：注册字段 username/password/nickname
- 4A：修改密码需校验 oldPassword
- 5A：改密后不处理登录态（token 继续有效到过期）

## 计划（确认后执行）

- [x] 设计/文档对齐
  - [x] `.kiro/specs/admin-base/api-spec.md` 补齐：register / change-password
  - [x] `.kiro/specs/admin-base/design.md` 补齐：注册与默认角色策略

- [x] 后端：注册
  - [x] 新增 `POST /api/auth/register`（permitAll）
  - [x] 新增 DTO：RegisterRequest / RegisterResponse（按你确认的 1/3）
  - [x] DB：创建用户（BCrypt）+ 赋予默认角色（sys_user_role）
  - [x] 校验：用户名唯一（沿用 1002）

- [x] 后端：修改密码
  - [x] 新增 `PUT /api/auth/me/password`
  - [x] DTO：ChangePasswordRequest（oldPassword/newPassword）
  - [x] 校验旧密码 + 更新 BCrypt hash

- [x] 前端：注册页
  - [x] 新增 `/register` 路由 + 页面（Element Plus 表单）
  - [x] 注册成功后的跳转/自动登录（按你确认的 1）
  - [x] 前台 Header：未登录时显示“登录/注册”

- [x] 前端：个人中心完善
  - [x] ProfileView 增加“修改密码”区块（old/new/confirm）
  - [x] 保存后提示中文；避免英文文案

- [x] 数据初始化（可选但推荐）
  - [x] `.kiro/specs/admin-base/seed.sql` 增加 user 角色（及必要关联）
  - [x] `backend/src/test/resources/data.sql` 增加 user 角色（保证测试稳定）

- [x] 可验证性
  - [x] 后端：新增集成测试（注册→登录/自动登录→改密→旧密码失败/新密码成功）
  - [x] 前端：`npm run build` 通过
  - [x] 手工联调 checklist：注册→登录→个人信息→改密→重新登录
