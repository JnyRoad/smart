# 系统上下文与架构

> 本图描述 **smart 仓在整个智慧家园 3.0 平台中的位置**。全平台上下文（含 smart-module 14 个业务微服务、smart-ui / smart-h5 前端）见 [yuto-smart/docs/04-architecture/system-context.md](../../../docs/04-architecture/system-context.md)。

## 上下文图（文字描述）

```
   ┌──────────────────────┐   ┌──────────────────────┐
   │      smart-ui        │   │      smart-h5        │   ← 姊妹仓（前端）
   │ Vue2 + ElementUI     │   │ Vue2 + CubeUI + 微信  │
   └──────────┬───────────┘   └──────────┬───────────┘
              │                          │
              └──────────┬───────────────┘
                         │ HTTPS
                         ▼
              ┌─────────────────────────────┐
              │     smart-gateway :9990     │  ← 本仓
              │ 入口 / 跨域 / 验证码 / 解密  │
              └──────┬───────────────┬──────┘
                     │               │
                     ▼               ▼
          ┌──────────────┐   ┌──────────────────────────┐
          │ smart-auth   │   │  smart-module (姊妹仓)    │
          │  :3000       │   │  14 个业务微服务:         │
          │ OAuth2 颁发  │   │   smart-app :6020         │
          │ 本仓         │   │   smart-platform :6030    │
          └──────┬───────┘   │   smart-data / -bridge /  │
                 │           │   -algorithm / -push /    │
                 │ Feign     │   -dispatcher / -schedule │
                 │           │   -transfer / -file ...   │
                 ▼           └────────┬─────────────────┘
          ┌─────────────────────────────────┐
          │   smart-upms-biz :4000          │  ← 本仓
          │  用户/角色/菜单/部门/字典/路由   │
          └──────┬─────────────────────┬────┘
                 │                     │
                 ▼                     ▼
       ┌─────────────────┐    ┌─────────────────┐
       │  MySQL 8.0.13   │    │     Redis       │
       │  (sys_* 表 +    │    │ Token / 路由缓存 │
       │  业务表共库)     │    └─────────────────┘
       └─────────────────┘
       (smart 与 smart-module 共享同一数据库实例)

                 ▲          (服务发现 + 配置)
                 │
         ┌─────────────────┐
         │     Nacos       │
         │ ns: eda914a9... │
         └─────────────────┘

                 ▲          (链路 / 健康)
                 │
         ┌─────────────────┐
         │ Zipkin + Admin  │
         └─────────────────┘
```

## 与姊妹仓的耦合点

| 维度 | 形式 | 备注 |
|------|------|------|
| 代码依赖 | Maven：smart-module 各模块 pom 直接依赖本仓的 `smart-common-core / -data / -security / -log / -swagger` 与 `smart-upms-api` | parent 是 `com.tce:smart:yuto-3.0-RELEASE` |
| 服务发现 | 共用同一 Nacos namespace `eda914a9-b100-427b-9d37-4d7da89b841f` | smart-module 通过 `@FeignClient` 远程调用 smart 的接口（如 token 校验） |
| 网关接入 | smart-module 各微服务的路由由 `sys_route_conf` 注入 smart-gateway，热加载 | 详见 [ADR-003](../14-decisions/ADR-003-dynamic-gateway-route.md) |
| 数据库 | 共用同一 MySQL 实例（DataSource 通过 Nacos `common.yml` 下发） | **无数据库隔离**，sys_* 表与业务表同库 |
| 认证 | smart-module 所有 Controller 通过 `smart-common-security` 解析 Token，使用 `@pms.hasPermission(...)` 鉴权 | 权限码统一在 sys_menu 表注册 |

## 关键架构决策（速览）

| 决策 | 选择 | ADR |
|------|------|-----|
| 注册中心 + 配置中心 | Nacos 单实例兼任 | [ADR-001](../14-decisions/ADR-001-nacos-over-eureka.md) |
| Token 存储 | Redis（RedisTokenStore, 前缀 `smart_oauth:`） | [ADR-002](../14-decisions/ADR-002-oauth2-redis-token.md) |
| 网关路由 | 动态路由（DB + Redis + Nacos 事件） | [ADR-003](../14-decisions/ADR-003-dynamic-gateway-route.md) |
| 鉴权方式 | OAuth 2.0 + Spring Security + 自定义 PermissionService | - |
| ORM | MyBatis-Plus（避免手写大量 mapper.xml） | - |
| 服务间调用 | OpenFeign | - |
| 断路器 | Hystrix（已 EOL，待替换 Resilience4j） | - |

## 边界与契约

- **对内**：smart-* 模块之间通过 Feign 调用，使用 `smart-upms-api` 中的 Entity/DTO 作为契约；内部调用通过 `@Inner` 注解放行鉴权。
- **对外**：园区业务子系统通过网关 `:9990` 接入，使用 OAuth 2.0 协议（grant_type 见 [authentication.md](../08-security/authentication.md)）。

## 数据流（典型登录）

1. 终端 → Gateway `/auth/oauth/token`，携带 client_id/secret + username/password + 验证码。
2. Gateway `ValidateCodeGatewayFilter` 校验验证码 → `PasswordDecoderFilter` 解密密码 → 转发到 smart-auth。
3. smart-auth `AuthorizationServerConfig` 完成认证 → TokenEnhancer 注入 `user_id / username / dept_id / parkList / license / isStrongPwd / salaryTypeName` → 写入 Redis (`smart_oauth:`)。
4. 终端拿到 access_token，后续请求带 `Authorization: Bearer xxx`。
5. 网关转发至业务服务，业务服务通过 `smart-common-security` 反序列化 Token → `@PreAuthorize("@pms.hasPermission(...)")` 进行细粒度鉴权。

## 数据流（动态路由刷新）

1. 管理员通过 `/route` API 修改 `sys_route_conf`。
2. smart-upms-biz 持久化 + 写 Redis + 发布 Nacos 事件。
3. smart-gateway 监听 Nacos 事件 → 由 `RedisRouteDefinitionWriter` 重新加载路由 → 即时生效。
