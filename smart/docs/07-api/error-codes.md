# 错误码

> 业务层统一返回结构：`Result { code: int, msg: String, data: T }`，`code = 0` 表示成功。
> 网关层 / OAuth2 层另有 HTTP 状态码（401 未认证 / 403 无权限 / 500 服务异常等）。

## 统一返回示例

```json
{ "code": 0, "msg": "success", "data": { ... } }
{ "code": 1, "msg": "用户不存在", "data": null }
```

## 错误码约定

| code | 含义 | 触发场景 |
|------|------|---------|
| 0 | 成功 | 所有正常返回 |
| 1 | 通用业务失败 | `Result.failed(msg)` 默认使用 |
| 401 | 未认证 / Token 失效 | OAuth2 资源服务器校验失败 |
| 403 | 无权限 | `@PreAuthorize` 校验未通过 |
| 500 | 服务异常 | 未捕获异常 / SmartException |

> 当前代码主要使用 `code=1` 作为通用业务失败，**没有细粒度业务错误码体系**。建议规划：
>
> - `1xxx` 用户/认证
> - `2xxx` 权限/角色/菜单
> - `3xxx` 路由/客户端
> - `4xxx` 字典/日志
> - `5xxx` 外部依赖（数据库 / Redis / Nacos）
>
> 新增功能时按此分段，并回写本文档。

## OAuth2 标准错误

| OAuth2 error | HTTP | 触发场景 |
|--------------|------|---------|
| `invalid_grant` | 400 | 用户名/密码错、Refresh Token 过期 |
| `invalid_client` | 401 | client_id/client_secret 错 |
| `invalid_token` | 401 | access_token 失效 |
| `unauthorized` | 401 | 缺失 Authorization 头 |
| `access_denied` | 403 | 权限不足 |
| `invalid_request` | 400 | 缺少必填参数 |
