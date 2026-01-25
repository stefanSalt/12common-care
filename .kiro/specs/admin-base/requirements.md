# 需求文档

## 简介

本文档定义了一个个人使用的管理系统基座的功能需求，用于开发通用的后台+前台的ToC系统。系统采用 Spring Boot 3 + Vue 3 技术栈，实现用户管理、角色管理、认证授权、文件管理和消息通知等核心功能。

## 术语表

- **System**: 管理系统基座，包含后端API服务和前端管理界面
- **User**: 系统用户实体，包含用户基本信息
- **Role**: 角色实体，用于权限分组管理
- **Permission**: 接口权限标识，格式为 `模块:操作`
- **JWT_Token (Access_Token)**: JSON Web Token，用于用户身份认证与受保护接口访问
- **Refresh_Token**: 用于刷新 JWT_Token 的刷新令牌
- **File_Manager**: 文件管理模块，处理文件上传下载
- **File_Visibility**: 文件可见性，取值为 PUBLIC/PRIVATE
- **Notification_Service**: 消息通知服务，处理实时推送和系统公告
- **WebSocket_Handler**: WebSocket 连接处理器，管理实时通信

## 需求

### 需求 1：用户认证

**用户故事：** 作为系统用户，我希望能够安全地登录系统，以便访问受保护的功能。

#### 验收标准

1. WHEN 用户提交有效的用户名和密码 THEN System SHALL 返回包含用户信息、JWT_Token 和 Refresh_Token 的响应
2. WHEN 用户提交无效的用户名或密码 THEN System SHALL 返回认证失败错误信息
3. WHEN 用户携带有效的 JWT_Token 访问受保护接口 THEN System SHALL 允许访问并返回请求的资源
4. WHEN 用户携带过期的 JWT_Token 访问受保护接口 THEN System SHALL 返回 401 未授权错误
5. WHEN 用户携带无效格式的 JWT_Token 访问受保护接口 THEN System SHALL 返回 401 未授权错误
6. THE System SHALL 将 JWT_Token 的默认有效期设置为 1 天
7. THE System SHALL 将 Refresh_Token 的默认有效期设置为 30 天
8. THE System SHALL 在 JWT_Token 中包含用户ID、用户名、角色列表和过期时间
9. WHEN JWT_Token 被解析 THEN System SHALL 验证签名完整性
10. WHEN 用户携带有效的 Refresh_Token 请求刷新 THEN System SHALL 返回新的 JWT_Token 和新的 Refresh_Token
11. THE System SHALL 不要求在服务端处理刷新后的旧 Refresh_Token（不维护 Refresh_Token 撤销列表），旧 Refresh_Token 在有效期内仍可用于刷新
12. WHEN 用户携带过期或无效的 Refresh_Token 请求刷新 THEN System SHALL 返回 401 未授权错误
13. THE System SHALL 不要求在服务端处理登出（不维护 Token 撤销列表），登出由客户端清理本地状态完成

### 需求 2：用户管理

**用户故事：** 作为管理员，我希望能够管理系统用户，以便控制谁可以访问系统。

#### 验收标准

1. WHEN 管理员创建新用户时提供有效信息 THEN System SHALL 创建用户并返回用户信息
2. WHEN 管理员创建用户时用户名已存在 THEN System SHALL 返回用户名重复错误
3. WHEN 管理员查询用户列表 THEN System SHALL 返回分页的用户列表数据
4. WHEN 管理员根据ID查询用户 THEN System SHALL 返回该用户的详细信息
5. WHEN 管理员更新用户信息 THEN System SHALL 保存更新并返回更新后的用户信息
6. WHEN 管理员删除用户 THEN System SHALL 将用户标记为已删除状态
7. WHEN 管理员为用户分配角色 THEN System SHALL 建立用户与角色的关联关系
8. THE System SHALL 使用不可逆哈希方式存储用户密码

### 需求 3：角色管理

**用户故事：** 作为管理员，我希望能够管理角色和权限，以便实现细粒度的访问控制。

#### 验收标准

1. WHEN 管理员创建新角色时提供有效信息 THEN System SHALL 创建角色并返回角色信息
2. WHEN 管理员创建角色时角色编码已存在 THEN System SHALL 返回角色编码重复错误
3. WHEN 管理员查询角色列表 THEN System SHALL 返回所有角色数据
4. WHEN 管理员更新角色信息 THEN System SHALL 保存更新并返回更新后的角色信息
5. WHEN 管理员删除角色 THEN System SHALL 将角色标记为已删除状态
6. WHEN 管理员为角色分配权限 THEN System SHALL 建立角色与权限的关联关系
7. WHEN 查询角色详情 THEN System SHALL 返回角色信息及其关联的权限列表

### 需求 4：接口权限控制

**用户故事：** 作为系统架构师，我希望通过注解方式控制接口权限，以便简化权限管理代码。

#### 验收标准

1. WHEN 用户访问标注了 @RequiresPermission 的接口 THEN System SHALL 检查用户是否拥有对应权限
2. WHEN 用户拥有所需权限 THEN System SHALL 允许访问该接口
3. WHEN 用户缺少所需权限 THEN System SHALL 返回 403 禁止访问错误
4. WHEN 接口未标注权限注解 THEN System SHALL 仅验证用户已登录即可访问
5. THE System SHALL 从 JWT_Token 中提取用户角色并查询对应权限列表

### 需求 5：文件管理

**用户故事：** 作为用户，我希望能够上传和管理文件，以便在系统中存储和使用文件资源。

#### 验收标准

1. WHEN 用户上传文件 THEN File_Manager SHALL 保存文件到本地存储并返回文件访问路径
2. WHEN 用户上传的文件超过大小限制 THEN File_Manager SHALL 返回文件过大错误
3. WHEN 用户上传不允许的文件类型 THEN File_Manager SHALL 返回文件类型不支持错误
4. WHEN 用户请求下载文件 THEN File_Manager SHALL 返回文件流数据
5. WHEN 用户请求下载不存在的文件 THEN File_Manager SHALL 返回文件不存在错误
6. WHEN 用户删除文件 THEN File_Manager SHALL 删除文件记录和物理文件
7. WHEN 用户查询文件列表 THEN File_Manager SHALL 返回当前用户的分页文件列表数据
8. THE File_Manager SHALL 为每个上传的文件生成唯一的存储路径
9. THE File_Manager SHALL 为每个文件记录关联上传者用户ID与 File_Visibility；当上传未指定 File_Visibility 时默认 PRIVATE
10. WHEN 用户请求下载 PRIVATE 文件 THEN File_Manager SHALL 仅允许上传者访问该文件
11. WHEN 用户请求下载 PUBLIC 文件 THEN File_Manager SHALL 允许匿名访问该文件
12. WHEN 用户删除文件 THEN File_Manager SHALL 仅允许上传者删除该文件

### 需求 6：消息通知

**用户故事：** 作为用户，我希望能够接收系统通知，以便及时了解重要信息。

#### 验收标准

1. WHEN 用户建立 WebSocket 连接 THEN WebSocket_Handler SHALL 验证用户身份并建立连接
2. WHEN 用户 WebSocket 连接使用无效 Token THEN WebSocket_Handler SHALL 拒绝连接
3. WHEN 系统发送通知给指定用户 THEN Notification_Service SHALL 通过 WebSocket 实时推送消息
4. WHEN 目标用户不在线 THEN Notification_Service SHALL 将消息存储待用户上线后推送
5. WHEN 管理员发布系统公告 THEN Notification_Service SHALL 向所有在线用户广播消息
6. WHEN 用户查询通知列表 THEN Notification_Service SHALL 返回用户的通知记录
7. WHEN 用户标记通知为已读 THEN Notification_Service SHALL 更新通知状态

### 需求 7：前端权限控制

**用户故事：** 作为前端开发者，我希望能够根据用户权限动态控制界面元素，以便提供合适的用户体验。

#### 验收标准

1. WHEN 用户登录成功 THEN System SHALL 将用户权限列表存储到 Pinia 状态管理中
2. WHEN 渲染需要权限的按钮 THEN System SHALL 根据用户权限决定是否显示该按钮
3. WHEN 用户访问需要认证的页面但未登录 THEN System SHALL 重定向到登录页面
4. WHEN 用户 Token 过期 THEN System SHALL 清除本地状态并重定向到登录页面
5. THE System SHALL 提供权限指令 v-permission 用于按钮级别权限控制

### 需求 8：API 文档

**用户故事：** 作为开发者，我希望能够查看 API 文档，以便了解接口的使用方式。

#### 验收标准

1. THE System SHALL 通过 Swagger/OpenAPI 自动生成 API 文档
2. WHEN 开发者访问文档页面 THEN System SHALL 展示所有可用接口及其参数说明
3. THE System SHALL 在文档中包含接口的请求示例和响应示例

### 需求 9：权限管理

**用户故事：** 作为管理员，我希望能够维护权限字典，以便为角色分配权限并与后端接口权限标识保持一致。

#### 验收标准

1. WHEN 管理员创建新权限时提供有效信息 THEN System SHALL 创建权限并返回权限信息
2. WHEN 管理员创建权限时权限标识已存在 THEN System SHALL 返回权限标识重复错误
3. WHEN 管理员查询权限列表 THEN System SHALL 返回所有权限数据
4. WHEN 管理员更新权限信息 THEN System SHALL 保存更新并返回更新后的权限信息
5. WHEN 管理员删除权限 THEN System SHALL 将权限标记为已删除状态
