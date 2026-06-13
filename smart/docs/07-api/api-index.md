# API 索引

> ⚠️ **范围声明**：本索引仅覆盖 **smart 仓自身** 提供的约 30 个管理类 / 基础设施类 API（用户/角色/菜单/部门/字典/路由/客户端/日志 + OAuth Token 端点）。
>
> 智慧家园 3.0 平台的 **业务 API 在姊妹仓 `smart-module`**，共 14 个微服务、合计约 **350+ 个 Controller**（smart-platform 189 / smart-data 81 / smart-app 63 / smart-algorithm 11 / …），由各微服务自身 Swagger 暴露。平台级 API 入口与跨仓路由见 [yuto-smart/docs/07-api/](../../../docs/) 与 [yuto-smart/docs/04-architecture/cross-repo-call-graph.md](../../../docs/04-architecture/cross-repo-call-graph.md)。
>
> 每个 API 的字段级详情以各服务的 Swagger UI 为准（详见本页底部"Swagger 在线文档"小节）。
>
> 所有接口经由网关 `:9990` 暴露，路径前缀为对应服务名（默认 `StripPrefix=1`，即 `/auth/...`→smart-auth、`/admin/...`→smart-upms-biz，具体以 `sys_route_conf` 配置为准）。下表以 Controller 中声明的相对路径表达。
>
> 统一返回结构：`Result<T> { code, msg, data }`，`code=0` 为成功，其他为业务错误（见 [error-codes.md](error-codes.md)）。
> 鉴权方式：`Authorization: Bearer <access_token>`；标注 `@Inner` 的为内部服务间调用，外部不可达。

## smart-auth（OAuth 2.0 端点）

| Method | Path | 说明 |
|--------|------|------|
| POST | `/oauth/token` | 标准 OAuth Token 端点（grant_type=password / authorization_code / client_credentials / refresh_token） |
| POST | `/oauth/check_token` | Token 校验 |
| POST | `/token/logout` | 主动登出（吊销 Token） |
| POST | `/mobile/token/sms` | 短信验证码登录 |
| POST | `/mobile/token/social` | 社交账号登录 |
| POST | `/ocr/token/...` | 人脸识别登录 |
| POST | `/wx/public/token` | 微信公众号登录 |
| POST | `/yht/token` | 友互通（YHT）登录 |

详细 grant_type 与请求参数见 [08-security/authentication.md](../08-security/authentication.md)。

## smart-upms-biz（业务 API）

按 Controller 分组：

### UserController `/user`

| Method | Path | 说明 | 鉴权 |
|--------|------|------|------|
| GET | `/info` | 当前登录用户信息（含菜单、角色） | Bearer |
| GET | `/info/{username}` | 按用户名查询 | Bearer |
| GET | `/simple/{userId}` | 简易用户信息 | Bearer |
| GET | `/page` | 分页查询用户 | `@pms.hasPermission('sys_user_view')` |
| POST | `/` | 新增用户 | `sys_user_add` |
| PUT | `/` | 修改用户 | `sys_user_edit` |
| DELETE | `/{userId}` | 删除（逻辑） | `sys_user_del` |
| PUT | `/password/update` | 修改密码（强密码刷新） | Bearer |
| GET | `/park/list/{userId}` | 查询用户的园区列表 | Bearer |

### RoleController `/role`

| Method | Path | 说明 | 鉴权 |
|--------|------|------|------|
| GET | `/{id}` | 角色详情 | Bearer |
| GET | `/list` | 角色列表 | Bearer |
| GET | `/page` | 分页 | `sys_role_view` |
| POST | `/` | 新增 | `sys_role_add` |
| PUT | `/` | 修改 | `sys_role_edit` |
| DELETE | `/{id}` | 删除 | `sys_role_del` |
| PUT | `/menu` | 角色↔菜单关系维护 | `sys_role_perm` |

### MenuController `/menu`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/tree` | 完整菜单树 |
| GET | `/tree/{roleId}` | 指定角色的菜单树 |
| GET | `/{id}` | 菜单详情 |
| POST | `/` | 新增 |
| PUT | `/` | 修改 |
| DELETE | `/{id}` | 删除 |

### DeptController `/dept`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/tree` | 部门树 |
| GET | `/user-tree` | 部门+用户混合树 |
| POST | `/` | 新增 |
| PUT | `/` | 修改 |
| DELETE | `/{id}` | 删除 |

### DictController `/dict`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/type/{type}` | 按类型查询字典 |
| GET | `/value` | 按 value 查询 |
| GET | `/{id}` | 详情 |
| POST | `/` | 新增 |
| PUT | `/` | 修改 |
| DELETE | `/{id}` | 删除 |

### LogController `/log`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/page` | 操作日志分页 |
| POST | `/` | 手工写入日志（@Inner 内部） |

### OauthClientDetailsController `/client`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/{id}` | 客户端详情 |
| GET | `/page` | 分页 |
| POST | `/` | 新增 OAuth Client |
| PUT | `/` | 修改 |
| DELETE | `/{id}` | 删除 |

### RouteConfController `/route`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/list` | 路由列表 |
| POST | `/` | 新增（同步 DB+Redis+Nacos event） |
| PUT | `/` | 修改 |
| DELETE | `/{id}` | 删除 |

### TokenController `/token`

| Method | Path | 说明 |
|--------|------|------|
| - | (`@Inner`) | 内部 Token 校验 / 撤销 |

### MobileController `/mobile`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/{mobile}` | 手机号查用户（社交/短信登录前置） |

### SocialDetailsController `/social`

| Method | Path | 说明 |
|--------|------|------|
| - | - | 第三方账号绑定 CRUD |

### MoveDataTaskController `/movetask`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/list` | 数据迁移任务列表 |

### UserApiController `/user-api`

| Method | Path | 说明 |
|--------|------|------|
| - | (`@Inner`) | 网关 / 其他服务远程调用入口 |

## Swagger 在线文档

各服务启动后访问 `http://<host>:<port>/swagger-ui.html`：

- smart-auth：`:3000/swagger-ui.html`
- smart-upms-biz：`:4000/swagger-ui.html`

通过网关访问需要 `sys_route_conf` 中暴露 `/v2/api-docs` 与 `/swagger-resources` 路径。
