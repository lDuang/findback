# 前后端对齐与UI落地指引

面向部署在 `http://47.243.228.60` 的后端，便于你按此文档补齐接口与配置；前端可按本计划迭代为简约风并接入真实数据。未涉及任何 Java 构建/运行命令。

## 部署与基础配置
- 反向代理：前端期望后端挂载在 `/api`，静态上传文件可通过 `/uploads/**` 直接访问。Nginx 示例：
  - `location /api/ { proxy_pass http://127.0.0.1:8080/; }`
  - `location /uploads/ { alias /path/to/uploads/; autoindex off; }`
- 跨域：如前后端不同源，请在后端允许前端域名的 CORS，放行 `X-User-Id`、`X-User-Role`。
- 文件存储：`app.upload-dir` 指向实际可写目录，URL 前缀保持 `/uploads/`。

## 数据模型（字段约定）
- User：`id`、`username`、`password`、`role`（`ADMIN`/`USER`）。
- Announcement：`id`、`title`、`content`、`createdAt`。
- Banner：`id`、`title`、`imageUrl`、`linkUrl`。
- LostItem：`id`、`userId`、`title`、`description`、`status`（`OPEN|CLAIMED|RESOLVED|CLOSED`）、`location`、`imageUrl`、`createdAt`。
- Claim：`id`、`itemId`、`userId`、`status`（`PENDING|APPROVED|REJECTED|CLAIMED|RESOLVED`）、`reason`、`evidenceUrl`、`createdAt`。
- MediaFile（上传返回）：`id`、`filename`、`url`、`uploaderId`。

## 接口期望（后端按此校对/补齐）
- 认证
  - `POST /api/auth/login` `{username,password}` → `{userId,username,role}`（role 返回大写）。
  - 请求头：业务接口需 `X-User-Id`、`X-User-Role`，管理员接口需 `role=ADMIN`。
- 公告
  - `GET /api/announcements` 公开。
  - `POST /api/announcements`、`DELETE /api/announcements/{id}` 仅管理员（当前后端仅校验登录，建议补充 `ensureAdmin`）。
- Banner
  - `GET /api/banners` 公开；前端首页将展示轮播/胶囊式。
- 物品
  - `GET /api/items?userId&status`；非管理员只能看自己的（已有校验）。
  - `GET /api/items/{id}`。
  - `GET /api/items/{id}/claims` 需登录；管理员或物品所属人看全部，否则仅看自己（已有逻辑）。
  - `POST /api/items` 创建；`PUT /api/items/{id}` 更新；`PUT /api/items/{id}/status`；`DELETE /api/items/{id}`。均需本人或管理员。
- 认领
  - `GET /api/claims?userId&itemId`；非管理员仅看自己或自己物品下的认领（已有逻辑）。
  - `POST /api/claims` 需登录；默认 `status=PENDING`。
  - `PUT /api/claims/{id}` 或 `/api/claims/{id}/status` 更新状态；允许管理员/认领人/物品发布人。
  - `DELETE /api/claims/{id}` 仅本人或管理员。
- 上传
  - `POST /api/files/upload` 表单字段 `file`；允许匿名，响应 `{id,url,filename,uploaderId}`，URL 可直接访问。

## 后端待完善建议
- 权限收紧：`POST /announcements` 建议也调用 `ensureAdmin`；若需要 Banner 维护，新增 `/api/admin/banners`（CRUD，仅管理员）。
- 响应一致性：所有 404/权限/参数错误统一返回 JSON 消息字段 `message`，便于前端展示。
- 密码存储：当前是明文匹配，建议后续改为加盐哈希（暂不强制以便快速联调）。
- 上传 URL：确认生产环境 `ServletUriComponentsBuilder` 生成的域名是否为 `47.243.228.60`；若有反代，建议直接返回相对路径 `/uploads/{file}`。

## 前端改造计划（简约风 + 真数据）
- 配置
  - `frontend/src/api/client.ts`：如果前端直接访问远端，`baseURL` 改为 `http://47.243.228.60/api`；若走同源反代则保持 `/api`。
  - 新增 `.env.production` 里 `VITE_API_BASE=/api` 以便切换。
- 主题与版式
  - 统一浅色简约：背景改为纯色或轻微纹理（去掉大面积渐变），主色建议墨蓝/墨绿系（如 `#1f3a5f` + 辅色 `#2f855a`），弱化阴影。
  - Element Plus 主题变量：调整按钮/链接主色、卡片圆角、表格间距。
  - 全局留白统一：容器 max-width 1200，内边距 24px，卡片间距 16px。
- 首页 Home
  - 去除重渐变，改为双栏极简介绍 + Banner 轮播（调用 `/banners`），下方展示最新公告（`/announcements`）。
  - 行动按钮：浏览物品、发布（锚点到 ItemsList 创建表单）。
- 列表页 ItemsList
  - 顶部过滤条：状态筛选、地点关键词；表格增加创建时间、发布人列（需后端返回 username？如无则显示 userId）。
  - 空态与错误提示统一使用 Element Empty 与 Result。
  - “快速发布”区域保持，按钮样式简化。
- 详情页 ItemDetail
  - 右侧侧栏显示物品基础信息，左侧显示认领时间线（按 `createdAt` 排序）。
  - 若是物品发布人/管理员，允许在详情页直接更新状态（调用 `/items/{id}/status`）。
- 我的中心 UserCenter
  - 列表增加分页/排序（前端可本地排序；若数据量大需后端分页）。
  - 我的认领增加状态 Tag 颜色统一。
- 管理员面板 AdminDashboard
  - Tab：用户/物品/认领/公告/Banner
  - 增补 Banner 管理（列表、上传图片、链接）；物品支持删除与状态修改；认领支持审批（已有）。
- 上传组件
  - 复用文件上传逻辑，提示上传中/失败；上传完成显示缩略图。

## 手工联调清单（不运行 Java 命令）
- 账号：准备一个管理员（role=ADMIN）与普通用户（role=USER）。
- 登录：用前端登录，确认 localStorage 缓存生效，接口返回 role 大写。
- 物品流程：创建 → 列表过滤（用户/管理员）→ 详情查看 → 更新状态 → 删除。
- 认领流程：A 发布物品，B 登录提交认领，A 查看 `/items/{id}/claims` 看到全部；管理员审批状态。
- 公告：管理员新增/删除公告，访客可见。
- Banner：管理员新增 Banner 后，首页轮播展示。
- 上传：上传图片后前端能直接访问返回的 `url`。
